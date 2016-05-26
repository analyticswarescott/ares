package com.aw.action;

import static com.aw.common.util.JSONUtils.updateFromString;

import com.aw.action.exceptions.ActionCreationException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * a type of action
 *
 *
 *
 */
@JsonSerialize(using=ToStringSerializer.class, as=String.class)
public interface ActionType {

	/**
	 * @return the string form of this type
	 */
	public String name();

	/**
	 * @return custom toString used for serialization - value of this will be passed back to the action factory to get the ActionType instance again
	 */
	@Override
	public String toString();

	/**
	 * @return a new action given the configuration data
	 */
	default public Action newAction(Object data) throws ActionCreationException {

		try {

			//create a new instance
			Action ret = getClassType().newInstance();

			//initialize the action properties
			updateFromString(String.valueOf(data), ret);

			//return the initialized action
			return ret;

		} catch (Exception e) {
			throw new ActionCreationException("error creating action", e);
		}

	}

	/**
	 * @return the class type for action instances created by this action type
	 */
	public Class<? extends Action> getClassType();

}
