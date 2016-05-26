package com.aw.action;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;

import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.auth.DefaultUser;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityAware;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.unity.UnityInstance;

/**
 * The incident manager for access to incident data
 *
 *
 *
 */
public class DefaultActionManager implements ActionManager, SecurityAware {

	private static Logger logger = Logger.getLogger(DefaultActionManager.class);

	Provider<Platform> platform;
	Provider<RestCluster> restCluster;
	Provider<PlatformMgr> platformMgr;
	Provider<UnityInstance> unity;

	RootActionFactory factory = null;

	@Inject
	public DefaultActionManager(Provider<Platform> platform, Provider<RestCluster> restCluster, Provider<PlatformMgr> platformMgr, Provider<UnityInstance> unity, RootActionFactory factory) {
		this.platform = platform;
		this.restCluster = restCluster;
		this.platformMgr = platformMgr;
		this.unity = unity;
		this.factory = factory;
	}

	/**
	 * have to use pre-jsr-330 guice because of conflict in spark
	 */
	@com.google.inject.Inject
	public DefaultActionManager(com.google.inject.Provider<Platform> platform, com.google.inject.Provider<RestCluster> restCluster, com.google.inject.Provider<PlatformMgr> platformMgr, com.google.inject.Provider<UnityInstance> unity, RootActionFactory factory) {
		this.platform = platform;
		this.restCluster = restCluster;
		this.platformMgr = platformMgr;
		this.unity = unity;
		this.factory = factory;
	}

	@Override
	public Action newAction(Object data) throws Exception {
		return factory.newAction(data);
	}

	@Override
	public void takeAction(Action action, ActionContext ctx) throws Exception {

		checkSystem();

		//prepare the action
		action.prepare(ctx);

		//set the user accordingly if it's wrong or not set TODO: use a user object for the user everywhere rather than a string
		if (action.getUser() == null || action.getUser().getUsername() == null ||  !action.getUser().getUsername().equals(getUserID())) {
			action.setUser(new DefaultUser(getUserID()));
		}

		//execute the action
		action.execute(ctx);

		logger.info("user" + getTenantID() + "/" + getUserID() + " took action " + action.getClass().getSimpleName() + " (" + action.getGuid() + ")");

		//if all went well, record the action in the system
		if (action.isPersistable()) {
			recordAction(action);
		}

	}

	@Override
	public void takeAction(Action action) throws Exception {
		takeAction(action, new DefaultActionContext(unity.get(), platformMgr));
	}

	@Override
	public void takeActions(Iterable<Action> actions) throws Exception {

		//create an empty action context for these actions
		ActionContext ctx = new DefaultActionContext(unity.get(), platformMgr);

		//take the action with an empty context object
		takeActions(actions, ctx);

	}

	@Override
	public void takeActions(Iterable<Action> actions, ActionContext ctx) throws Exception {

		checkSystem();

		//in case anything goes wrong
		ActionExecutionException exception = null;

		//process each action
		for (Action action : actions) {

			try {

				takeAction(action, ctx);

			} catch (Exception e) {

				//create our exception object if we don't have one yet
				if (exception == null) {
					exception = new ActionExecutionException(e);
				}

				//record the exception for this action
				exception.getExceptionMap().put(action, e);

				//continue on processing the rest of the actions - one failure shouldn't cancel all actions

			}

		}

		//throw as needed
		if (exception != null) {
			throw exception;
		}

	}

	/**
	 * Output the keyed message
	 *
	 * @param km The keyed message to output
	 */
	protected void recordAction(Action action) throws Exception {

		platformMgr.get().sendMessage(Topic.ACTION, action);

	}

	private void checkSystem() throws Exception {

		//wait until the platform is running to do anything
		restCluster.get().waitForRunning();

	}

}
