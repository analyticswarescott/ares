package com.aw.document.jdbc.derby;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.aw.platform.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.aw.common.rest.security.SecurityAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.EnvironmentSettings.Setting;
import com.aw.common.tenant.Tenant;
import com.aw.document.jdbc.AbstractSequencedDocumentJDBCProvider;
import com.aw.document.jdbc.SequencedDocumentJDBCProvider;

/**
 * Provides prepared statements to execute document persistence and retrieval
 */
public class SequencedDerbyJDBCProvider extends AbstractSequencedDocumentJDBCProvider implements SequencedDocumentJDBCProvider, SecurityAware {

	private static final Logger LOGGER = Logger.getLogger(SequencedDerbyJDBCProvider.class);

	@Override
	public boolean dbExists(Platform platform, Tenant tenant) {
		return new File(getDerbyDir(tenant.getTenantID())).exists();
	}

	@Override
	public void createDB(Platform platform, Tenant tenant) throws Exception {
		//nothing to do in derby
	}

	@Override
	public void shutdown() {

		/* In embedded mode, an application should shut down Derby.
        Shutdown throws the XJ015 exception to confirm success. */
		boolean gotSQLExc = false;
		Exception exception = null;
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {
			if ( se.getSQLState().equals("XJ015") ) {
				gotSQLExc = true;
			}
			exception = se;
		}
		if (!gotSQLExc) {
			LOGGER.error("Database did not shut down normally", exception);
		} else {
			LOGGER.info("Database shut down normally");
		}

		// force garbage collection to unload the EmbeddedDriver
		//  so Derby can be restarted
		System.gc();

	}

    String getDerbyDir(String tenantID) {
        return EnvironmentSettings.getDgData() + File.separatorChar + "config" + File.separatorChar + "derby" + File.separatorChar + tenantID;
    }


	@Override
	public void dropDB(Platform platform, Tenant tenant) throws Exception {

		if (dbExists(platform, tenant)) {
			File f = new File(getDerbyDir(tenant.getTenantID()));
			FileUtils.forceDelete(f);
		}

	}

    @Override
    public String getJDBCURL(Platform platform, Tenant tenant) {

    	//build the jdbc url
    	String url = "jdbc:derby:" +
    			EnvironmentSettings.fetch(Setting.CONFIG_DB_ROOT) +
    			"/" + tenant.getTenantID() + ";create=true";

    	return url;

    }

	@Override
	public String getJDBCDriver() {
		return "org.apache.derby.jdbc.EmbeddedDriver";
	}

	@Override
	protected String getMigrationPackage() {
		return "com.aw.common.rdbms.migrations.derby";
	}

	@Override
	public String getSchema() {
		return "APP";
	}

}
