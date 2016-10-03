package com.aw.common.rdbms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import com.aw.platform.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.AbstractDocumentHandler;
import com.aw.document.jdbc.JDBCProvider;
import com.google.common.base.Preconditions;

import javax.inject.Provider;


public class DBMgr {

	public static final Logger logger = LoggerFactory.getLogger(DBMgr.class);

	//for databases that map their large string types to varchar such as postgres TEXT type, we need a cutoff to treat is as a clob
	//TODO: use document bean info to determine if it's a json object rather than clob
	private static final int VARCHAR_CLOB_CUTOFF = 1000000;

    protected HashMap<String, ConnectionPool> pools = new HashMap<String, ConnectionPool>();
    protected HashMap<String, DataSource> datasources = new HashMap<String, DataSource>();


	public JDBCProvider getJDBCProvider() {
		return provider;
	}
    //the provider for jdbc connectivity
    JDBCProvider provider;
	Provider<Platform> platform;

    public DBMgr(Provider<Platform> platform, JDBCProvider provider) {

		this.provider = provider;
		this.platform = platform;
	}

	public void close() throws Exception {

        for (ConnectionPool p : pools.values()) {
            p.purge();

        }

		for (DataSource ds : datasources.values()) {
			ds.close();
		}

		provider.shutdown();

	}

	/**
	 * used for unProvisioning
	 */
	public void destroyDB(String tenantID) throws Exception{

		closePool(tenantID);

		Tenant tenant = new Tenant(tenantID);

		provider.dropDB(platform.get(), tenant);

	}

	public void initDB(Tenant tenant) throws Exception {
		initDB(tenant, true);
	}

	public void initDB(Tenant tenant, boolean attemptInit) throws Exception {

		//get the jdbc url
        String url = provider.getJDBCURL(platform.get(), tenant);

        //get the jdbc driver class name
        String className = provider.getJDBCDriver();

        if (!provider.dbExists(platform.get(), tenant)) {

        	//create the database
        	provider.createDB(platform.get(), tenant);
			attemptInit = true; //we needed to create, so we need to init

        }

        //set up the data source and connection pool for this tenant
		setupPool(tenant.getTenantID(), url, className);

		//initialize the schema
		if (attemptInit) {
			provider.initDB(platform.get(), datasources.get(tenant.getTenantID()), tenant, EnvironmentSettings.isClearDocsOnStartup());
		}

	}

	protected void closePool(String tenantID) {

		ConnectionPool pool = pools.remove(tenantID);
		DataSource ds = datasources.remove(tenantID);

		pool.purge();
		ds.close();

	}

	protected void setupPool(String tenantID, String jdbcURL, String driverClass) throws SQLException {

    //TODO: need to manage authentication for the databases
	/*	DataSource ds = new DataSource();
		ds.setDriverClassName(driverClass);
		ds.setUrl(jdbcURL);
		ds.setUsername("");

		ds.setPassword("");
		ds.setInitialSize(5);
		ds.setMaxActive(5);
		ds.setMaxIdle(2);
		ds.setMinIdle(1);

		_pool = ds;

		ds.createPool();
		ds.*/

		PoolProperties p = new PoolProperties();
		p.setUrl(jdbcURL);
		p.setDriverClassName(driverClass);
		p.setUsername(provider.getUsername());
		p.setPassword(provider.getPassword());
		// p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		// p.setTestOnBorrow(true);
		// p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(5000);
		p.setTimeBetweenEvictionRunsMillis(10000);
		p.setMaxActive(100);
		p.setInitialSize(10);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(10000);
		p.setMinIdle(10);
		//  p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                        "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
		DataSource datasource = new DataSource();
		datasource.setPoolProperties(p);
		ConnectionPool pool = datasource.createPool();

		//cache the data source and connection pool
		datasources.put(tenantID, datasource);
        pools.put(tenantID, pool);

	}

	public JSONArray getPoolStatus () {
		return new JSONArray();
	}




	public Connection getConnection(Tenant tenant) throws SQLException {

		Preconditions.checkNotNull(tenant, "tenant cannot be null when getting database connection");

		ConnectionPool pool = pools.get(tenant.getTenantID());

		if (pool == null) {
			try {
				//init pool but do not attemptInit by default
				initDB(tenant, false);
				pool = pools.get(tenant.getTenantID());
			} catch (Exception e) {
				throw new RuntimeException("error initializing DB when pool is null for tenant " + tenant.getTenantID(), e);
			}
		}

        Connection conn = pool.getConnection();

        //set the schema if applicable
        if (provider.getSchema() != null) {
			conn.setSchema(provider.getSchema());
		}

		return conn;
	}

	public DataSource getDataSource(Tenant tenant) {
		return datasources.get(tenant.getTenantID());
	}


	public static JSONArray list2JSON(ResultSet rs, String RetrievalType) throws Exception {
		if (RetrievalType.equalsIgnoreCase(AbstractDocumentHandler.RETRIEVE_HEADER)) {
			return list2JSON(rs, true);
		}
		else if (RetrievalType.equalsIgnoreCase(AbstractDocumentHandler.RETRIEVE_ALL)) {
			return list2JSON(rs, false);
		}
		else {throw new Exception("unsupported retrieval type " + RetrievalType);}
	}

	public static JSONArray list2JSON(ResultSet rs, boolean skipLOB) throws Exception {
		JSONArray ret = new JSONArray();

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {
			JSONObject r = new JSONObject();
			for (int i = 1; i <= meta.getColumnCount(); i++) {

				int type = meta.getColumnType(i);

				//TODO: decide case default...post-RSA lower is current default
				String name = meta.getColumnName(i).toLowerCase();

				int precision = meta.getPrecision(i);

                Object value = rs.getObject(i);
				if (value != null && value.toString().equals("{}")) {
					r.put(name, new JSONObject());
					continue;
				}

                //TODO: improve null handling in Jackson derivitive objects
                if (value == null) {
                    value = "";
                }

                if (type == Types.VARCHAR && precision < VARCHAR_CLOB_CUTOFF) {
                    if (value != null && value.toString().equals("{}")) {
                        r.put(name, new JSONObject());
                        continue;
                    }
                    r.put(name, value);
                }
                else if (type == Types.SMALLINT ||  type == Types.INTEGER || type == Types.BIGINT || type == Types.BIT
						|| type == Types.DECIMAL || type == Types.DOUBLE) {
                    r.put(name, value);
                }
                else if (type == Types.TIMESTAMP) {
                	//format date properly from db
                	r.put(name,  JSONUtils.formatTimestamp(new java.util.Date(((Timestamp)value).getTime()).toInstant()));
                }
                else if (type == Types.CLOB || (type == Types.VARCHAR && precision >= VARCHAR_CLOB_CUTOFF)) {
                    if (skipLOB) {
                        continue;
                    }

                    JSONObject parsed = null;
                    if (value instanceof String) {
                    	parsed = new JSONObject((String)value);
                    } else {
                    	parsed = clobToJson((Clob)value);
                    }
                    r.put(name, parsed != null ? parsed : new JSONObject());
                }
                else  {

                    r.put(name, value.toString());
                }
        }
			ret.put(r);
		}

		return ret;
	}

	public void execUpdate(Tenant tenant, String sql) throws Exception {
		Statement st = null;
		Connection conn = null;

		try {
			conn = getConnection(tenant);
			st = conn.createStatement();
			st.execute(sql);
		} catch (Exception ex) {
			throw ex;
		} finally {
			st.close();
			conn.close();

		}
	}



	public int execUpdateWithCount(Tenant tenant, String sql) throws Exception {
		Statement st = null;
		Connection conn = null;

		try {
			conn = getConnection(tenant);
			st = conn.createStatement();
			//st.execute(sql);
			return st.executeUpdate(sql);
		} catch (Exception ex) {
			throw ex;
		} finally {
			st.close();
			conn.close();

		}
	}


	public static String clobToString(Clob data) {
		StringBuilder sb = new StringBuilder();
		try {
			Reader reader = data.getCharacterStream();
			BufferedReader br = new BufferedReader(reader);

			String line;
			while (null != (line = br.readLine())) {
				sb.append(line);
			}
			br.close();
		} catch (SQLException e) {
			// handle this exception
		} catch (IOException e) {
			// handle this exception
		}
		return sb.toString();
	}

	private static JSONObject clobToJson(Clob data) throws Exception {
		try {
			Reader reader = data.getCharacterStream();
			return new JSONObject(IOUtils.toString(reader));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Connection getConnection(String url, String user, String pass) throws  Exception {

		Connection conn = DriverManager.getConnection(url,user, pass);
		return  conn;


	}

	public long executeScalarCountSelect(Tenant tenant, String sql) throws Exception {

		try (Connection conn = getConnection(tenant)) {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			rs.next();
			return rs.getLong(1);

		} catch (Exception ex) {
			throw ex;
		}
	}

	public long executeScalarCountSelect(Map<String, String> dbConfig, String sql) throws Exception {

		try (Connection conn = DBMgr.getConnection(getJDBCProvider().getJDBCURL(dbConfig),
			dbConfig.get(DBConfig.DB_USER), dbConfig.get(DBConfig.DB_PASS))) {

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			rs.next();
			return rs.getLong(1);

		} catch (Exception ex) {
			throw ex;
		}
	}


	public static Connection getConnection(Map<String, String> dbConfig) throws  Exception {
		boolean dbPerTenant = false; //TODO add as config

		JDBCProvider provider = (JDBCProvider) Class.forName(dbConfig.get(DBConfig.DB_PROVIDER)).newInstance();

		String jdbcUrl = provider.getJDBCURL(dbConfig);
		logger.debug("JDBC connecting to: " + jdbcUrl);

		Connection conn = DBMgr.getConnection(jdbcUrl, dbConfig.get(DBConfig.DB_USER),
			dbConfig.get(DBConfig.DB_PASS));

		return conn;

	}

}