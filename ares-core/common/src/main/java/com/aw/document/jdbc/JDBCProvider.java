package com.aw.document.jdbc;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.aw.common.tenant.Tenant;
import com.aw.platform.Platform;

/**
 * Provides access to a specific jdbc based database type within the platform
 *
 *
 *
 */
public interface JDBCProvider {

	/**
	 * @param tenant the tenant whose database is being checked
	 * @return whether the tenant's configuration database exists
	 */
	public boolean dbExists(Platform platform,  Tenant tenant) throws SQLException;



	/**
	 * get the jdbc url for the given host:port and tenant
	 *
	 * @param host
	 * @param port
	 * @param tenant
	 * @return a jdbc url for the given host/port/tenant
	 */
	public String getJDBCURL(Map<String , String> dbConfig, Tenant tenant);

	/**
	 * get the jdbc url for the given host:port and tenant
	 *
	 * @param host
	 * @param port
	 * @param tenant
	 * @return a jdbc url for the given host/port/tenant
	 */
	public String getJDBCURL(Map<String , String> dbConfig);

	/**
	 * get the jdbc url for the given host:port and tenant
	 *
	 * @param host
	 * @param port
	 * @param tenant
	 * @return a jdbc url for the given host/port/tenant
	 */
	public String getJDBCURL(Platform platform, Tenant tenant);

	/**
	 * TODO: store this somewhere else?
	 *
	 * @return database username
	 */
	public String getUsername();

	/**
	 * TODO: store this somewhere else?
	 *
	 * @return database password
	 */
	public String getPassword();

	/**
	 * @return the JDBC driver class name for this JDBC provider
	 */
	public String getJDBCDriver();
	/**
	 * perform process-wide shutdown of database resources
	 */
	public void shutdown() throws Exception;

	/**
	 * drop the database for the given tenant
	 *
	 * @param tenant the tenant for which the existing database will be dropped
	 */
	public void dropDB(Platform platform,Tenant tenant) throws Exception;

	/**
	 * create the database itself - no schemas are applied, just create the database
	 *
	 * @param tenant the tenant whose database is to be created
	 * @throws Exception
	 */
	public void createDB(Platform platform, Tenant tenant) throws Exception;

	/**
	 * create a database for the given tenant within the given data source
	 * @param dataSource
	 * @throws Exception
	 */
	public void initDB(Platform platform, DataSource dataSource, Tenant tenant, boolean clean) throws Exception;


	public String getConflictClause(String keyField) throws Exception;

	/**
	 * @return the schema for this provider, or null if no schema needs to be set on the JDBC connections
	 */
	public String getSchema();

}