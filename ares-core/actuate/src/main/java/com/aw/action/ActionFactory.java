package com.aw.action;

import java.util.List;

/**
 * A factory for action types
 *
 *
 *
 */
public interface ActionFactory {

	/**
	 * @return all supported types by this type factory
	 */
	public List<ActionType> getAllTypes();

	/**
	 * @param strType string form of a type
	 * @return the action type
	 */
	public boolean hasType(String strType);

	/**
	 * get an action type given the type string
	 *
	 * @param strType the type string describing the action type
	 * @return the action type object for this type, is null if not found
	 */
	public ActionType toType(String strType);

	/**
	 * @return the common base type for all actions from this action factory
	 */
	public Class<? extends Action> getBaseType();

}
