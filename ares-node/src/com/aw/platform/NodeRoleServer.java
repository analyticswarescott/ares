package com.aw.platform;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.EnvironmentSettings;
import com.aw.util.RestServer;

/**
 * REST server for the node operational role (node service)
 *
 *
 *
 */
public class NodeRoleServer extends RestServer {

	static final Logger logger = Logger.getLogger(NodeRoleServer.class);

	public NodeRoleServer(String basePath, int port) {
		super(basePath, NodeRole.NODE, port);
	}

	public static void main(String[] args) throws Exception {

		SecurityUtil.setThreadSystemAccess();
		NodeRoleServer launcher;
		launcher = new NodeRoleServer(args[0], Integer.parseInt(args[1]));
		launcher.init(PlatformMgr.getCachedPlatform()).start();

	}



	@Override
	public void run() {

		super.run();

	}

	@Override
	protected void postStart() throws Exception {

		//TODO: remove this and use log4j.properties
		String log4j = EnvironmentSettings.getLog4JConfigFile();

		PropertyConfigurator.configure(log4j);
		//logger.error("NODESERVICE: §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§ properties file for log4j: " +log4j);

	}


}
