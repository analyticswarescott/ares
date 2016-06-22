package com.aw;

import javax.inject.Provider;

import com.aw.action.ActionManager;
import com.aw.action.RootActionFactory;
import com.aw.common.inject.DocumentProvider;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.TaskService;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.document.jdbc.derby.DerbyJDBCProvider;
import com.aw.incident.IncidentManager;
import com.aw.incident.UnityIncidentManager;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.RestMember;
import com.aw.rest.inject.DGBinder;
import com.aw.rest.inject.ResourceFactoryWrapper;
import com.aw.tenant.TenantMgr;
import com.aw.unity.UnityInstance;
import com.aw.user.DgmcDbUserManager;
import com.aw.user.UserManager;

/**
 * A test binder that gives the integration framework direct access to all factories - this should never be used
 * in production.
 *
 * @author jlehmann
 *
 */
public class TestDependencies extends DGBinder {

	//test property to use the config db we use in production
	public static final String USE_DERBY = "useDerby";

	@Override
	protected void configure() {

		//bind global (non-tenant-scoped) dependencies
		bind(jdbc).to(DocumentJDBCProvider.class);
		bind(actionFactory).to(RootActionFactory.class);

		//bind the singleton factories we use in test here
		bindFactory(new ResourceFactoryWrapper<DocumentHandler>(binder.docs)).to(DocumentHandler.class);
		bindFactory(new ResourceFactoryWrapper<DocumentMgr>(binder.docMgr)).to(DocumentMgr.class);
		bindFactory(new ResourceFactoryWrapper<UnityInstance>(binder.unity)).to(UnityInstance.class);
		bindFactory(new ResourceFactoryWrapper<PlatformMgr>(binder.platformMgr)).to(PlatformMgr.class);
		bindFactory(new ResourceFactoryWrapper<LocalRestMember>(binder.restMember)).to(LocalRestMember.class);
		bindFactory(new ResourceFactoryWrapper<LocalRestMember>(binder.restMember)).to(RestMember.class);
		bindFactory(new ResourceFactoryWrapper<RestCluster>(binder.restCluster)).to(RestCluster.class);
		bindFactory(new ResourceFactoryWrapper<Platform>(binder.platformMgr.get())).to(Platform.class);
		bindFactory(new ResourceFactoryWrapper<TenantMgr>(binder.tenantMgr)).to(TenantMgr.class);
		bindFactory(new ResourceFactoryWrapper<TimeSource>(binder.timeSource)).to(TimeSource.class);
		bindFactory(new ResourceFactoryWrapper<LocalTaskContainer>(binder.taskContainer)).to(LocalTaskContainer.class);
		bindFactory(new ResourceFactoryWrapper<TaskService>(binder.taskService)).to(TaskService.class);
		bindFactory(new ResourceFactoryWrapper<ActionManager>(binder.actionManager)).to(ActionManager.class);
		bind(UnityIncidentManager.class).to(IncidentManager.class);
		bind(DgmcDbUserManager.class).to(UserManager.class);
		bind(binder.actionFactory).to(RootActionFactory.class);

	}

	@Override
	protected DocumentJDBCProvider getDBProvider() {
		String useDerby = System.getProperty(USE_DERBY);
		if (useDerby != null) {
			return new DerbyJDBCProvider(); //use derby for integration tests for now
		} else {
			return super.getDBProvider();
		}
	}

	/**
	 * reset document db state in test
	 */
	public static void resetDocs() throws Exception {
    	TestDependencies.getDocMgr().get().reset();
    	((DocumentProvider)getDocs()).reset();
	}

	public static Provider<DBMgr> getDBMgr() { return get().dbMgr; }

	public static Provider<DocumentHandler> getDocs() { return get().docs;  }
	public static Provider<DocumentMgr> getDocMgr() { return get().docMgr;  }
	public static Provider<Platform> getPlatform() { return get().platformMgr.get();  }
	public static Provider<PlatformMgr> getPlatformMgr() { return get().platformMgr;  }
	public static Provider<UnityInstance> getUnity() { return get().unity;  }
	public static Provider<RestCluster> getRestCluster() { return get().restCluster;  }
	public static Provider<LocalRestMember> getRestMember() { return get().restMember;  }
	public static Provider<TenantMgr> getTenantMgr() { return get().tenantMgr;  }
	public static Provider<TimeSource> getTimeSource() { return get().timeSource;  }
	public static Provider<TaskService> getTaskService() { return get().taskService;  }
	public static Provider<LocalTaskContainer> getTaskContainer() { return get().taskContainer;  }
	public static Provider<ActionManager> getActionManager() { return get().actionManager;  }
	public static DocumentJDBCProvider getJdbcProvider() { return get().jdbc; }

	private static TestDependencies get() {
		synchronized (TestDependencies.class) {

			if (binder == null) {
				binder = new TestDependencies();
				TestDependencies.class.notifyAll();
			}

		}

		return binder;
	}
	private static TestDependencies binder;

}
