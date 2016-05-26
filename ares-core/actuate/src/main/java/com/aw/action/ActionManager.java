package com.aw.action;

/**
 * Interface for interacting with incidents
 *
 *
 *
 * @param <T> The type of data used to create actions
 */
public interface ActionManager {

	/**
	 * Perform an incident action
	 *
	 * @param action The action to take
	 */
	public void takeAction(Action action, ActionContext ctx) throws Exception;

	/**
	 * Perform a single action. Should not be called if other actions are executed within the same
	 * context. For that, call takeActions()
	 *
	 * @param action The action to execute
	 * @throws Exception If anything goes wrong
	 */
	public void takeAction(Action action) throws Exception;

	/**
	 * Performs all of the actions. If any exceptions occur during processing, a single exception will be rolled up detailing which
	 * actions had errors.
	 *
	 * @param actions
	 * @throws Exception
	 */
	public void takeActions(Iterable<Action> actions) throws Exception;

	/**
	 *
	 * @param actions
	 * @throws Exception
	 */
	public void takeActions(Iterable<Action> actions, ActionContext ctx) throws Exception;

	/**
	 * create a new action given the data
	 *
	 * @param actionName The name of the action
	 * @return an uninitialized action of the given type
	 */
	public Action newAction(Object data) throws Exception;

}
