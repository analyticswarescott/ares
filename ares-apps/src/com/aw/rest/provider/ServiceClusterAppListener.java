package com.aw.rest.provider;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.TaskService;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;

/**
 * Initialize service cluster components on startup
 *
 *
 *
 */
@Provider
public class ServiceClusterAppListener implements ApplicationEventListener {

	private static final Logger LOGGER = Logger.getLogger(ServiceClusterAppListener.class);

	private LocalRestMember serviceMember;
	private LocalTaskContainer container;
	private PlatformMgr platformMgr;
	private RestCluster restCluster;
	private TaskService taskService;
	private boolean initialized = false;

	@Inject @com.google.inject.Inject
	public ServiceClusterAppListener(LocalRestMember serviceMember, LocalTaskContainer container, PlatformMgr platformMgr, RestCluster restCluster, TaskService taskService) {

		this.serviceMember = serviceMember;
		this.container = container;
		this.platformMgr = platformMgr;
		this.restCluster = restCluster;
		this.taskService = taskService;

	}

	@Override
	public void onEvent(ApplicationEvent event) {

		try {

			//System.out.println(" -------====================== EVENT: " + event.getType().toString());

			SecurityUtil.setThreadSystemAccess();

			//on app initialization, initialize our services
			if (!initialized && event.getType() == Type.INITIALIZATION_FINISHED) {

				//perform initializationO
				LOGGER.info("initializing");

				//on rest startup for non-first node, we can assume things are good to go so initialize rest cluster
				if (!EnvironmentSettings.isFirstNode()) {
					restCluster.init();
				}


				//initialize and try to join service cluster
				serviceMember.initialize();
				serviceMember.tryToJoin();

				//try to join task cluster
				container.tryToJoin();

				initialized = true;

			}

			else if (initialized && event.getType() == Type.DESTROY_FINISHED) {

				if (serviceMember != null) {
					serviceMember.leave();
				}

				if (container != null) {
					container.shuttingDown();
					container.leave();
				}

				if (restCluster != null) {
					restCluster.cleanup();
				}

				if (taskService != null) {
					taskService.cleanup();
				}

			}


		} catch (Exception e) {

			LOGGER.error("error during webapp initialization", e);

			platformMgr.handleException(e, NodeRole.REST);

		} finally {

			SecurityUtil.unsetThreadSystemAccess();

		}

	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {
		return null;
	}

}
