package com.aw.platform;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.NodeManager;

/**
 * Initializes the node service itself once the web application initialization is complete. The Heartbeater was
 * refactored into a @Provider to use the standard mechanisms for initialization.
 *
 * TODO: The Heartbeater class itself should probably be cleaned up a bit
 *
 *
 *
 */
@Provider
public class NodeAppListener implements ApplicationEventListener {

	private static Logger LOGGER = Logger.getLogger(NodeAppListener.class);

	//TODO: common thread pool? this was brought over from NodeRoleServer during the refactoring for now
	ExecutorService hbService = Executors.newSingleThreadExecutor();

	NodeManager nodeManager;

	private boolean m_lastHeartbeatGood = false;

	@Inject @com.google.inject.Inject
	public NodeAppListener(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}


	@Override
	public void onEvent(ApplicationEvent event) {

		if (event.getType() == Type.INITIALIZATION_APP_FINISHED) {

			//announce our presence now that we're up
			LOGGER.info("starting Heartbeater to announce node presence");
			Heartbeater hb = new Heartbeater();

			hbService.submit(hb);

		}

	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {
		return null;
	}

	private class Heartbeater implements Runnable {

		@Override
		public void run() {

			try {

				LOGGER.warn(" Heartbeater thread starting " + Thread.currentThread().getName());

				if (EnvironmentSettings.isFirstNode()) {
					LOGGER.warn(" @ FIRST_NODE detected as true, starting REST to serve as installation master ");

					//update local platform node from localhost to host name
					File f = new File(EnvironmentSettings.getConfDirectory() + File.separatorChar + "defaults"
							+ File.separatorChar + "platform" + File.separatorChar + Platform.LOCAL + ".json");

					LOGGER.warn(" reading platform file: " + f.getAbsolutePath());

					String s = FileUtils.readFileToString(f);
					LOGGER.trace(" platform is:  " + s);

					//TODO: 1-FEB-16 - recent tests indicate this is no longer needed -- perhaps a side-effect of Hadoop HA changes
					/*String updated = s.replace("localhost", EnvironmentSettings.getHost());

					LOGGER.warn(" modifying platform to update localhost to $DG_HOST: " + EnvironmentSettings.getHost());
					FileWriter fw = new FileWriter(f);
					fw.write(updated);
					fw.close();*/

					LOGGER.warn(" obtaining REST role manager");
					RoleManager rm = nodeManager.getRoleManager(NodeRole.REST);
					LOGGER.warn("role manager: " + rm);

					LOGGER.warn(" starting REST ");
					rm.start();
					NodeRoleStatus rs = rm.getStatus();

					if (rs.getState() != State.RUNNING) {
						throw new PlatformStateException(" unable to start REST after FIRST_NODE detected");
					}
					LOGGER.warn(" FIRST_NODE REST started ");

				}

				//TODO: refine heartbeating system
				while (true) {

					boolean hbGood = false;

					int tryNo = 1;
					while (!hbGood) {

					  try {
						  hbGood = nodeManager.announcePresence();

					  }
					  catch (Exception ex) {
						  LOGGER.error(" error announcing presence in Heartbeater " + ex.getMessage());
					  }

						if (!hbGood) {
							m_lastHeartbeatGood = false;
							if (tryNo < 3) {
								LOGGER.warn(" REST server could not be contacted...waiting to retry (this log will stop although still trying)");
							}
							Thread.sleep(3000);
						}
						tryNo++;
					}

					if (!m_lastHeartbeatGood) {
						LOGGER.warn(" heartbeat succeeded to REST server: changing interval to 15 secs"); //TODO: configurable interval

					}

					m_lastHeartbeatGood = true;
					Thread.sleep(15000);
				}


			}
			catch (Error error) {
				throw error;
			}
			catch (Exception e) {
				throw new RuntimeException(" Node Service heartbeat exception ", e);
			}

		}
	}


}
