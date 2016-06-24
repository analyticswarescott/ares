package com.aw.document.jdbc.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.ConfigDbMaster;
import com.aw.platform.roles.ConfigDbWorker;
import org.apache.log4j.Logger;
import org.postgresql.Driver;

import com.aw.common.tenant.Tenant;
import com.aw.document.jdbc.AbstractDocumentJDBCProvider;

/**
 * a sql provider for postgres
 *
 *
 *
 */
public class PostgresJDBCProvider extends AbstractDocumentJDBCProvider {

	private static final Logger LOGGER = Logger.getLogger(PostgresJDBCProvider.class);

	public static final String DEFAULT_USERNAME = "postgres";
	static final String SYSTEM_DATABASE = "postgres";

	@Override
	public void shutdown() {

		//nothing to do here

	}

	@Override
	public boolean dbExists(Platform platform, Tenant tenant) throws SQLException {

		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
			try (PreparedStatement stmt = conn.prepareStatement("select count(*) from pg_database where datname='" + getDatabaseName(tenant) + "'")) {
				try (ResultSet rs = stmt.executeQuery()) {
					rs.next();
					return rs.getInt(1) > 0;
				}
			}
		}

	}

	@Override
	public void createDB(Platform platform, Tenant tenant) throws Exception {

		LOGGER.info("creating database for " + tenant.getTenantID() + " : " + getDatabaseName(tenant));

		//connect to postgres database and create a database
		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
			try (PreparedStatement stmt = conn.prepareStatement("create database " + getDatabaseName(tenant))) {

				//execute the create
				stmt.executeUpdate();

			}
		}

	}

	@Override
	public String getConflictClause(String keyField) throws Exception {
		return " ON CONFLICT (" + keyField +  ") DO NOTHING ";
	}

	@Override
	public void dropDB(Platform platform, Tenant tenant) throws Exception {

		LOGGER.info("dropping database for " + tenant.getTenantID() + " : " + getDatabaseName(tenant));

		//create temporary connection to drop the db
		try (Connection conn = DriverManager.getConnection(getJDBCURL(platform, SYSTEM_DATABASE), getUsername(), getPassword())) {
			try (PreparedStatement stmt = conn.prepareStatement("drop database " + getDatabaseName(tenant))) {

				//execute the drop
				stmt.executeUpdate();

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
		return "com.aw.common.rdbms.migrations.postgres";
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
