package com.aw;

import com.aw.platform.NodeRoleServer;
import com.aw.test.nodeservice.TestNodeBinder;
import com.aw.util.RestServer;
import com.aw.util.Statics;

/**
 * Node service wrapper
 *
 * @author jlehmann
 *
 */
public class NodeServiceWrapper extends RestServiceWrapper {

	public NodeServiceWrapper(int port) {
		super("./node_service", port, TestNodeBinder.class.getName());
	}

	public NodeServiceWrapper(String path, int port) {
		super(path, port, TestNodeBinder.class.getName());
	}

	@Override
	protected RestServer newServer(String basePath, int port) {
		return new NodeRoleServer(basePath, port);
	}

	@Override
	protected String getCheckPath() {
		return "/rest/" + Statics.REST_VERSION + "/ping";
	}

}
