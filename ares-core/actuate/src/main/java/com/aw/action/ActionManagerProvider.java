package com.aw.action;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.unity.UnityInstance;

/**
 * ActionManager provider
 *
 *
 *
 */
public class ActionManagerProvider implements Provider<ActionManager> {

	private ActionManager actionManager;

	@Inject
	public ActionManagerProvider(Provider<Platform> platform, Provider<RestCluster> restCluster, Provider<PlatformMgr> platformMgr, Provider<UnityInstance> unity, RootActionFactory factory) {
		actionManager = new DefaultActionManager(platform, restCluster, platformMgr, unity, factory);
	}

	@com.google.inject.Inject
	public ActionManagerProvider(com.google.inject.Provider<Platform> platform, com.google.inject.Provider<RestCluster> restCluster, com.google.inject.Provider<PlatformMgr> platformMgr, com.google.inject.Provider<UnityInstance> unity, RootActionFactory factory) {
		actionManager = new DefaultActionManager(platform, restCluster, platformMgr, unity, factory);
	}

	@Override
	public ActionManager get() {
		return actionManager;
	}

}
