package com.aw.rest.inject;

import javax.inject.Provider;

import com.aw.common.inject.*;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.jdbc.mysql.MySQLJDBCProvider;
import com.aw.user.DgmcDbUserManager;
import com.aw.user.UserManager;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.aw.action.ActionFactory;
import com.aw.action.ActionManager;
import com.aw.action.ActionManagerProvider;
import com.aw.action.DefaultActionFactory;
import com.aw.action.DefaultRootActionFactory;
import com.aw.action.RootActionFactory;
import com.aw.action.json.ActionModule;
import com.aw.alarm.AlarmManager;
import com.aw.alarm.DefaultAlarmManager;
import com.aw.alarm.action.AlarmAction;
import com.aw.alarm.action.AlarmActionType;
import com.aw.common.inject.task.TaskContainerProvider;
import com.aw.common.inject.task.TaskServiceProvider;
import com.aw.common.inject.unity.UnityProvider;
import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.TaskService;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.document.jdbc.postgres.PostgresJDBCProvider;
import com.aw.incident.IncidentManager;
import com.aw.incident.UnityIncidentManager;
import com.aw.incident.action.IncidentAction;
import com.aw.incident.action.IncidentActionType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.RestMember;
import com.aw.tenant.TenantMgr;
import com.aw.unity.UnityInstance;

/**
 * Configure injection bindings for REST services
 */
public class DGBinder extends AbstractBinder {

	//all resource providers
	protected Provider<DocumentHandler> docs;
	protected Provider<DocumentMgr> docMgr;
	protected Provider<PlatformMgr> platformMgr;
	protected Provider<UnityInstance> unity;
	protected Provider<RestCluster> restCluster;
	protected Provider<LocalRestMember> restMember;
	protected Provider<TenantMgr> tenantMgr;
	protected Provider<TimeSource> timeSource;
	protected Provider<TaskService> taskService;
	protected Provider<LocalTaskContainer> taskContainer;
	protected Provider<ActionManager> actionManager;
	protected Provider<Platform> platform;
	protected DocumentJDBCProvider jdbc;
	protected RootActionFactory actionFactory;
	protected AlarmManager alarmManager;

	protected Provider<DBMgr> dbMgr;

	protected static final ActionFactory[] ACTION_FACTORIES = {

		//incident actions
		new DefaultActionFactory(IncidentAction.class, IncidentActionType.values()),

		//alarm actions
		new DefaultActionFactory(AlarmAction.class, AlarmActionType.values())

	};

	public DGBinder() {

		//set our config db jdbc provider
		jdbc = getDBProvider();

		//create / wire up our dependencies
		actionFactory = new DefaultRootActionFactory(ACTION_FACTORIES);
		platformMgr = new PlatformMgrProvider();

		dbMgr = new DatabaseProvider(platformMgr.get(), getDBProvider());

		restCluster = new RestClusterFactory(platformMgr.get());
		docMgr = new DocumentMgrFactory(restCluster, platformMgr.get(), jdbc);
		docs = new DocumentProvider(docMgr);
		unity = new UnityProvider(docs, platformMgr.get(), new ActionModule(actionFactory));
		tenantMgr = new TenantMgrProvider(docs, platformMgr.get(), docMgr, platformMgr, restCluster);
		restMember = new RestMemberProvider(restCluster.get(), tenantMgr.get(), platformMgr.get(), docs, docMgr);
		timeSource = new TimeSourceProvider();
		taskService = new TaskServiceProvider(platformMgr, platformMgr.get(), docs, timeSource.get());
		taskContainer = new TaskContainerProvider(timeSource, platformMgr, taskService, docMgr.get(), restCluster.get());
		actionManager = new ActionManagerProvider(platformMgr.get(), restCluster, platformMgr, unity, actionFactory);
		alarmManager = new DefaultAlarmManager(platformMgr.get());

	}

	protected DocumentJDBCProvider getDBProvider()  {
		if (EnvironmentSettings.getDBVendor().equals(EnvironmentSettings.POSTGRES)) {
			return new PostgresJDBCProvider();
		}
		else if (EnvironmentSettings.getDBVendor().equals(EnvironmentSettings.MYSQL)) {
			return new MySQLJDBCProvider();
		}
		else {
			throw new RuntimeException(" unsupported DB vendor " + EnvironmentSettings.getDBVendor());
		}


	}

	@Override
	protected void configure() {

		//bind our dependencies using a jersey compatible factory wrapper

		//bind global (non-tenant-scoped) dependencies
		bind(jdbc).to(DocumentJDBCProvider.class);
		bind(actionFactory).to(RootActionFactory.class);

		bindFactory(new ResourceFactoryWrapper<DBMgr>(dbMgr)).to(DBMgr.class);
		//bind tenant scoped dependencies
		bindFactory(new ResourceFactoryWrapper<DocumentHandler>(docs)).to(DocumentHandler.class);
		bindFactory(new ResourceFactoryWrapper<DocumentMgr>(docMgr)).to(DocumentMgr.class);
		bindFactory(new ResourceFactoryWrapper<UnityInstance>(unity)).to(UnityInstance.class);
		bindFactory(new ResourceFactoryWrapper<PlatformMgr>(platformMgr)).to(PlatformMgr.class);
		bindFactory(new ResourceFactoryWrapper<LocalRestMember>(restMember)).to(LocalRestMember.class);
		bindFactory(new ResourceFactoryWrapper<LocalRestMember>(restMember)).to(RestMember.class);
		bindFactory(new ResourceFactoryWrapper<RestCluster>(restCluster)).to(RestCluster.class);
		bindFactory(new ResourceFactoryWrapper<Platform>(platformMgr.get())).to(Platform.class);
		bindFactory(new ResourceFactoryWrapper<TenantMgr>(tenantMgr)).to(TenantMgr.class);
		bindFactory(new ResourceFactoryWrapper<TimeSource>(timeSource)).to(TimeSource.class);
		bindFactory(new ResourceFactoryWrapper<LocalTaskContainer>(taskContainer)).to(LocalTaskContainer.class);
		bindFactory(new ResourceFactoryWrapper<TaskService>(taskService)).to(TaskService.class);
		bindFactory(new ResourceFactoryWrapper<ActionManager>(actionManager)).to(ActionManager.class);
		bind(UnityIncidentManager.class).to(IncidentManager.class);
		bind(DgmcDbUserManager.class).to(UserManager.class);
		bind(alarmManager).to(AlarmManager.class);

	}

}
