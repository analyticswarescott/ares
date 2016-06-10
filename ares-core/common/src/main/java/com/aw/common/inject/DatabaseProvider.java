package com.aw.common.inject;

import com.aw.common.rdbms.DBMgr;
import com.aw.document.jdbc.JDBCProvider;
import com.aw.platform.Platform;

import javax.inject.Provider;

/**
 * Created by scott on 10/06/16.
 */
public class DatabaseProvider implements Provider<DBMgr> {

	@Override
	public DBMgr get() {
		return dbMgr;
	}

	DBMgr dbMgr;

	public DatabaseProvider (Provider<Platform> platform, JDBCProvider provider) {
			dbMgr = new DBMgr(platform, provider);
	}

}
