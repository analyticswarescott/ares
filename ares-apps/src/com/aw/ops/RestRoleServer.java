package com.aw.ops;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.roles.Rest;
import com.aw.util.RestServer;

/**
 * The primary rest server providing the external REST apis for other components and the UI to interact with.
 *
 * <p>In order to run this standalone outside of a DG platform vm, you need to set the following environment variables:</p>
 *
 * <li>FIRST_NODE=true - tells the process that it is the first node in the cluster
 * <li>PLATFORM_PATH=<path_to_platform_json> - path to a platform.json document
 *
 * this class should generally NOT be run manually, it is run as part of the platform framework.
 *
 *
 *
 */
public class RestRoleServer extends RestServer {

	static Logger logger = Logger.getLogger(RestRoleServer.class);

    /**
     * Create a jetty launcher root at the given base path
     *
     * @param path The base path to the web content used to drive this jetty instance
     */
    public RestRoleServer(String path, int port) {
    	super(path, NodeRole.REST, port);
	}

    /**
     * Defaults to the standard REST reporting base path
     */
    public RestRoleServer(Platform platform) {
    	this(EnvironmentSettings.getDgReporting(), -1);
    	setPort(platform.getNode(NodeRole.REST).getSettingInt(Rest.PORT));
    }

    public static void main(String[] args) throws Exception {

    	//set up logging
		PropertyConfigurator.configure(EnvironmentSettings.getLog4JConfigFile());
		logger.warn(" properties file for log4j: " + EnvironmentSettings.getLog4JConfigFile());



		Platform platform = PlatformMgr.getCachedPlatform();


        RestRoleServer launcher;
        if (args.length == 0) {

            launcher = new RestRoleServer(platform);
        }
        else {

            launcher = new RestRoleServer(args[0], Integer.parseInt(args[1]));
		}
		
        launcher.init(platform).start();
    }

    protected void postStart() {
		//set up logging
		PropertyConfigurator.configure(EnvironmentSettings.getLog4JConfigFile());

	}

}
