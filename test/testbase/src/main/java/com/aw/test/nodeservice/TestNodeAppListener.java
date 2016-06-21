package com.aw.test.nodeservice;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import com.aw.platform.NodeAppListener;
import com.aw.platform.nodes.NodeManager;

@Provider
public class TestNodeAppListener extends NodeAppListener {

	@Inject
	public TestNodeAppListener(NodeManager nodeManager) {
		super(nodeManager);
	}

	public void onEvent(org.glassfish.jersey.server.monitoring.ApplicationEvent event) {
		super.onEvent(event);
	}

}
