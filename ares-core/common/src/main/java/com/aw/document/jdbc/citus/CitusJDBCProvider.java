/*
package com.aw.document.jdbc.citus;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.jdbc.JDBCProvider;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.ConfigDbMaster;
import com.aw.platform.roles.ConfigDbWorker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.postgresql.Driver;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.util.HashSet;

*/
/**
 * a sql provider for postgres
 *
 *
 *
 *//*

public class CitusJDBCProvider extends CitusDocumentJDBCProvider implements JDBCProvider, CitusProvider {

	private static final Logger LOGGER = Logger.getLogger(CitusJDBCProvider.class);

	public static final String DEFAULT_USERNAME = "postgres";
	static final String SYSTEM_DATABASE = "postgres";

	@Override
	public void shutdown() {

		//nothing to do here

	}

	@Override
	public boolean dbExists(Platform platform, Tenant tenant) throws SQLException {

		boolean exists;
		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
			try (PreparedStatement stmt = conn.prepareStatement("select count(*) from pg_database where datname='" + getDatabaseName(tenant) + "'")) {
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					exists = rs.getInt(1) > 0;

					if (!exists) {
						return false;
					}
					else { //verify citus
						verifyCitus(platform, platform.getNode(NodeRole.CONFIG_DB_MASTER), NodeRole.CONFIG_DB_MASTER, getDatabaseName(tenant));
					}
				}
			}
		}


		//

		//now ensure DB exists on all workers
		for (PlatformNode worker : platform.getNodes(NodeRole.CONFIG_DB_WORKER)){
			try (Connection conn = DriverManager.getConnection(getWorkerJDBCUrl(worker, SYSTEM_DATABASE), getUsername(), getPassword())) {
				try (PreparedStatement stmt = conn.prepareStatement("select count(*) from pg_database where datname='" + getDatabaseName(tenant) + "'")) {
					try (ResultSet rs = stmt.executeQuery()) {
						rs.next();
						exists = rs.getInt(1) > 0;
						if (!exists) {
							return false;
						}
						else { //verify citus
							verifyCitus(platform, worker, NodeRole.CONFIG_DB_WORKER,  getDatabaseName(tenant));
						}
					}
				}
			}
		}

		return true;

	}


	@Override
	public void createDB(Platform platform, Tenant tenant) throws Exception {

		LOGGER.info("creating database for " + tenant.getTenantID() + " : " + getDatabaseName(tenant));

		// ensure DB is created on all workers
		//if (!SystemUtils.IS_OS_MAC) {

		for (PlatformNode worker : platform.getNodes(NodeRole.CONFIG_DB_WORKER)) {
			try (Connection conn = DriverManager.getConnection(getWorkerJDBCUrl(worker, SYSTEM_DATABASE), getUsername(), getPassword())) {
				try (PreparedStatement stmt = conn.prepareStatement("create database  " + getDatabaseName(tenant))) {
					//execute the create
					stmt.executeUpdate();
					//create citus
					verifyCitus(platform, worker, NodeRole.CONFIG_DB_WORKER,  getDatabaseName(tenant));
				}
			}
			//}


			if (isSingleNodeSingleDB(platform)) {
				LOGGER.info(" createDB: detected worker and master are same DB...skipping further DB creation");
				return; //worker IS master
			}

			//connect to master database and create a database
			try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
				try (PreparedStatement stmt = conn.prepareStatement("create database  " + getDatabaseName(tenant))) {

					//execute the create
					stmt.executeUpdate();
					//create citus
					verifyCitus(platform, null, NodeRole.CONFIG_DB_MASTER, getDatabaseName(tenant));


				}
			}

		}
	}


	private void verifyCitus(Platform platform, PlatformNode node, NodeRole role,  String dbName) {


		if (isSingleNodeSingleDB(platform)) {
			LOGGER.warn(" verifyCitus for DB: " + dbName +  ": detected worker and master are same DB...citus extension will not be verified or created");
			return; //worker IS master
		}

		PreparedStatement stmt2 = null;
		try {

			Connection conn;
			String url = null;
			if (role == NodeRole.CONFIG_DB_MASTER) {
				 url = getJDBCURL(platform, dbName);
				conn = DriverManager.getConnection(url, getUsername(), getPassword());
			}
			else {
				url = getWorkerJDBCUrl( node, dbName);
				conn = DriverManager.getConnection(url, getUsername(), getPassword());
			}

			LOGGER.warn(" creating CITUS at URL " + url);
			stmt2 = conn.prepareStatement(" CREATE EXTENSION IF NOT EXISTS citus;");
			stmt2.executeUpdate();

			conn.close();

			LOGGER.warn(" citus verified for " + url);
		} catch (Exception e) {
			e.printStackTrace();
			throw  new RuntimeException(" unexpected citus config exception creating statement", e);
		}





	}

	@Override
	public void initDB(Platform platform, DataSource dataSource, Tenant tenant, boolean clean) throws Exception{
		super.initDB(platform, dataSource, tenant, clean);

		if (isSingleNodeSingleDB(platform)) {
			LOGGER.info(" INITDB: worker and master are same DB/port...skipping distributed table maintenance");
			return; //worker IS master
		}



		//now ensure all tables are defined as distributed
		String dbName = getDatabaseName(tenant);
		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, dbName), getUsername(), getPassword())) {

			HashSet<String> distTables = new HashSet<>();
			//get all currently distributed tables
			try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT relname from pg_dist_partition p join pg_class c on p.logicalrelid = c.relfilenode")) {

				//execute the create
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					String tblName = rs.getString("relname");

					LOGGER.info(" distributed table " + tblName + " exists in db: " + dbName );
					distTables.add(tblName);
				}


				//get defined dist tables with relevant settings
				String citus_settings = FileUtils.readFileToString(new File(EnvironmentSettings.getConfDirectory() + File.separatorChar +
					"config_db" + File.separatorChar + "table_shard_settings.json"));

				CitusShardSettings css = JSONUtils.objectFromString(citus_settings, CitusShardSettings.class);

				//for each defined dist table, if it does not exist, define it
				for (CitusShardSettings.ShardedTable ss : css.getShardedTables()) {

					//if table is not distributed, define it
					if (!distTables.contains(ss.getTableName())) {

						LOGGER.warn(" distributed table " + ss.getTableName() + " not found CREATING -- in db: " + dbName );

						//create dist table TODO: figure out how to type the arguments to the function
						PreparedStatement ps = conn.prepareStatement(
							"SELECT master_create_distributed_table('" + ss.getTableName() + "', '"+
								ss.getShardColumn()+"', '"+
								ss.getShardMethod()+"')");
						//ps.setObject(1, ss.getTableName());
						//ps.setObject(2, ss.getShardColumn());
						//ps.setObject(3, ss.getShardMethod());

						ps.executeQuery();


						//get platform info
						int targetReplication = platform.getSettings(NodeRole.CONFIG_DB_MASTER).getSettingInt(ConfigDbMaster.TARGET_REPLICATION);
						int workerNodeCount = platform.getNodes(NodeRole.CONFIG_DB_WORKER).size();
						int sf = ss.getShardsPerWorker();

						int shards = workerNodeCount * sf;

						if (targetReplication > workerNodeCount) {
							targetReplication = workerNodeCount;
						}

						LOGGER.warn(" creating " + shards + " shards and " + targetReplication + " replicas for table " + ss.getTableName() );

						ps = conn.prepareStatement("SELECT master_create_worker_shards('"+
							ss.getTableName()	+ "', " +
							shards + " ," +
							+ targetReplication + " )");
						//ps.setString(1, ss.getTableName());
						//ps.setInt(2, shards);
						//ps.setInt(3, targetReplication);

						ps.executeQuery();


					}
					else {
						//TODO: examine shard configuration and re-balance (manually for open-source citus, using EE functions with enterprise
					}
				}
			}
			finally {
				conn.close();
			}



		*/
/*	try (PreparedStatement stmt = conn.prepareStatement("SELECT table_name FROM information_schema.tables where table_schema = 'public'")) {

				//execute the create
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					String tblName = rs.getString(0);

					//table, shard column, shard method = 'hash'
					PreparedStatement ps = conn.prepareStatement("SELECT master_create_distributed_table(?, ?, ?)");

				}

			}*//*

		}



	}

	@Override
	public String getConflictClause(String keyField) throws Exception {
		return null;
	}


	private boolean isSingleNodeSingleDB(Platform platform) {
	   return PlatformUtils.isSingleNodeSingleDB(platform);
   }

	@Override
	public void dropDB(Platform platform, Tenant tenant) throws Exception {

		LOGGER.info("dropping database for " + tenant.getTenantID() + " : " + getDatabaseName(tenant));

		//remove DB from all workers
		for (PlatformNode worker : platform.getNodes(NodeRole.CONFIG_DB_WORKER)){
			try (Connection conn = DriverManager.getConnection(getWorkerJDBCUrl(worker, SYSTEM_DATABASE), getUsername(), getPassword())) {
				try (PreparedStatement stmt = conn.prepareStatement("drop database " + getDatabaseName(tenant))) {
					//execute the drop
					stmt.executeUpdate();
					LOGGER.warn(" DROPPED DB for " + getTenantID());
				}
			}

		}

		if (isSingleNodeSingleDB(platform)) {
			LOGGER.warn(" dropDB: detected worker and master are same DB...this is a test configuration and citus is not required");
			return; //worker IS master
		}


		//create temporary connection to drop the db from the master
		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
			try (PreparedStatement stmt = conn.prepareStatement("drop database " + getDatabaseName(tenant))) {

				//execute the drop
				stmt.executeUpdate();
				LOGGER.warn(" DROPPED WORKER DB for " + getTenantID());

			}
		}

	}

	private String getDatabaseName(Tenant tenant) {
		return "tenant_" + tenant.getTenantID();
	}

    @Override
    public String getJDBCURL(Platform platform, Tenant tenant) {
    	return getJDBCURL(platform, getDatabaseName(tenant));
    }

    String getJDBCURL(Platform platform, String databaseName) {

		PlatformNode master = platform.getNode(NodeRole.CONFIG_DB_MASTER);
		int port = master.getSettings(NodeRole.CONFIG_DB_MASTER).getSettingInt(ConfigDbMaster.MASTER_DB_PORT);

		//build the jdbc url:
		String url = "jdbc:postgresql://"+master.getHost()+":" + port + "/" + databaseName;

		return url;

    }

	protected String getWorkerJDBCUrl(PlatformNode node, String databaseName) {


		int port = node.getSettings(NodeRole.CONFIG_DB_WORKER).getSettingInt(ConfigDbWorker.WORKER_DB_PORT);



		//build the jdbc url:
		String url = "jdbc:postgresql://"+node.getHost()+":" + port + "/" + databaseName;

		return url;

	}

    @Override
	public String getJDBCDriver() {
		return Driver.class.getName();
	}

	@Override
	protected String getMigrationPackages() {
		return "com.dawcommon.rdbms.migrations.postgres";
	}

	@Override
	public String getUsername() {
		return DEFAULT_USERNAME;
	}

	@Override
	public String getSchema() {
		//no schema for this database type for now
		return null;
	}

}
*/
