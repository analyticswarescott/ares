package com.aw.document.jdbc;

import javax.sql.DataSource;

import com.aw.platform.Platform;
import org.flywaydb.core.Flyway;

import com.aw.common.tenant.Tenant;

/**
 * base class for providing common jdbc functionality for jdbc providers
 *
 *
 *
 */
public abstract class AbstractJDBCProvider implements JDBCProvider {

	/**
	 * creates a database using flyway migration
	 */
	@Override
	public void initDB(Platform platform, DataSource dataSource, Tenant tenant, boolean clean) throws Exception {

		// Start DB migrations
		final Flyway flyway = new Flyway();
		flyway.setBaselineOnMigrate(true);

		//flyway.setSchemas("public");

		flyway.setDataSource(dataSource);

		//get the migrations for this db type
		//TODO: split out custom migrations

		flyway.setLocations(getMigrationPackages());

		// Clean up schema first, if needed
		if (clean) {
			flyway.clean();
		}

		// Perform the migrations
		flyway.migrate();

		flyway.validate();

	}

	/**
	 * @return the java package that defines flyway migrations for this database
	 */
	protected abstract String getMigrationPackages();

	/**
	 * default empty username
	 */
	@Override
	public String getUsername() { return ""; }

	/**
	 * default empty password
	 */
	@Override
	public String getPassword() { return ""; }

}
