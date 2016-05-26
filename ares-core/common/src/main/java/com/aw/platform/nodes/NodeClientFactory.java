package com.aw.platform.nodes;

import com.aw.platform.PlatformNode;

/**
 * A factory for node clients
 *
 *
 *
 */
public interface NodeClientFactory {

	public NodeClient newNodeClient(PlatformNode node);

}
