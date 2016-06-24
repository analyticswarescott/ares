package com.aw.compute.inject;

import javax.inject.Provider;

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
import com.aw.common.cluster.ClusterException;
import com.aw.common.inject.*;
import com.aw.common.inject.unity.UnityProvider;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.TimeSource;
import com.aw.compute.referencedata.geo.DefaultGeoLocationSource;
import com.aw.compute.referencedata.geo.GeoLocationSource;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.document.jdbc.mysql.MySQLJDBCProvider;
import com.aw.document.jdbc.postgres.PostgresJDBCProvider;
import com.aw.incident.action.IncidentAction;
import com.aw.incident.action.IncidentActionType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;
import com.aw.unity.UnityInstance;
import com.google.inject.AbstractModule;

/**
 * Production compute di module
 *
 *
 *
 */
public class ComputeModule extends AbstractModule {

	//all resource providers
	protected Provider<DocumentHandler> docs;
	protected Provider<DocumentMgr> docMgr;
	protected Provider<Platform> platform;
	protected Provider<PlatformMgr> platformMgr;
	protected Provider<UnityInstance> unity;
	protected Provider<RestCluster> restCluster;
	protected Provider<LocalRestMember> restMember;
	protected Provider<TenantMgr> tenantMgr;
	protected Provider<TimeSource> timeSource;
	protected Provider<ActionManager> actionManager;
	protected RootActionFactory actionFactory;
	protected AlarmManager alarmManager;
	protected Provider<DBMgr> dbMgr;

	protected static final ActionFactory[] ACTION_FACTORIES = {

		//incident actions
		new DefaultActionFactory(IncidentAction.class, IncidentActionType.values()),

		//alarm actions
		new DefaultActionFactory(AlarmAction.class, AlarmActionType.values())

	};


	protected DocumentJDBCProvider getDBProvider() {

		return new MySQLJDBCProvider(); //TODO: allow driver setting to drive this

/*
		if (EnvironmentSettings.getDBVendor().equals(EnvironmentSettings.POSTGRES)) {
			return new PostgresJDBCProvider();
		}
		else if (EnvironmentSettings.getDBVendor().equals(EnvironmentSettings.MYSQL)) {
			return new MySQLJDBCProvider();
		}
		else {
			throw new RuntimeException(" unsupported DB vendor " + EnvironmentSettings.getDBVendor());
		}
*/

	}

	@Override
	protected void configure() {

		//create / wire up our dependencies
		actionFactory = new DefaultRootActionFactory(ACTION_FACTORIES);
		platformMgr = new PlatformMgrProvider();



		//create our factories
		actionFactory = new DefaultRootActionFactory(ACTION_FACTORIES);
		platformMgr = new PlatformMgrProvider();
		platform = new PlatformFactory(platformMgr);

		dbMgr = new DatabaseProvider(platform, getDBProvider());

		restCluster = new RestClusterFactory(platform);
		docMgr = new DocumentMgrFactory(restCluster, platformMgr.get(), null); //no jdbc config db in spark (only rest)
		docs = new DocumentProvider(docMgr);
		unity = new UnityProvider(docs, platform, new ActionModule(actionFactory));
		tenantMgr = new TenantMgrProvider(docs, platform, docMgr, platformMgr, restCluster);
		restMember = new RestMemberProvider(restCluster.get(), tenantMgr.get(), platformMgr.get(), docs, docMgr);
		timeSource = new TimeSourceProvider();
		actionManager = new ActionManagerProvider(platform, restCluster, platformMgr, unity, actionFactory);
		alarmManager = new DefaultAlarmManager(platform);

		try {

			//initialize in spark before dependencies are requested
			restCluster.get().init();

		} catch (ClusterException e) {
			//this is fatal
			throw new RuntimeException(e);
		}




		bind(DBMgr.class).toProvider(new ProviderWrapper<DBMgr>(dbMgr));
		bind(DocumentHandler.class).toProvider(new ProviderWrapper<>(docs));
		bind(DocumentMgr.class).toProvider(new ProviderWrapper<>(docMgr));
		bind(UnityInstance.class).toProvider(new ProviderWrapper<>(unity));
		bind(PlatformMgr.class).toProvider(new ProviderWrapper<>(platformMgr));
		bind(LocalRestMember.class).toProvider(new ProviderWrapper<>(restMember));
		bind(RestCluster.class).toProvider(new ProviderWrapper<>(restCluster));
		bind(Platform.class).toProvider(new ProviderWrapper<>(platform));
		bind(TenantMgr.class).toProvider(new ProviderWrapper<>(tenantMgr));
		bind(TimeSource.class).toProvider(new ProviderWrapper<>(timeSource));
		bind(ActionManager.class).toProvider(new ProviderWrapper<>(actionManager));

		//use standard geo location source
		bind(GeoLocationSource.class).to(DefaultGeoLocationSource.class);
		bind(AlarmManager.class).toInstance(alarmManager);

	}

}
