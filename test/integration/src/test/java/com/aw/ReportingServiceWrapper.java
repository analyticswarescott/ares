package com.aw;

import com.aw.common.system.EnvironmentSettings;
import com.aw.ops.RestRoleServer;
import com.aw.util.RestServer;
import com.aw.util.Statics;
import org.apache.hadoop.fs.Stat;

/**
 * Service wrapper for primary REST server in the platform
 *
 * @author jlehmann
 *
 */
public class ReportingServiceWrapper extends RestServiceWrapper {

	public ReportingServiceWrapper(String path, int port) {
		super(path, port);
	}

	@Override
	protected RestServer newServer(String basePath, int port) {
	    return new RestRoleServer(basePath, port);
	}

    public static ReportingServiceWrapper create( int port ) {
        return new ReportingServiceWrapper(EnvironmentSettings.getDgReporting(), port);
    }

    @Override
    protected String getCheckPath() {
    	return Statics.VERSIONED_REST_PREFIX + "/ping";
    }

}
