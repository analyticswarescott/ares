package com.aw.action;

/**
 * an action factory that can create actions from multiple child action factories given input data
 *
 *
 *
 */
public interface RootActionFactory extends ActionFactory {

	/**
	 * create an action from raw data
	 */
	public Action newAction(Object data) throws Exception;

	/**
	 * @return all child action factories for this root action factory
	 */
	public ActionFactory[] getFactories();

}
