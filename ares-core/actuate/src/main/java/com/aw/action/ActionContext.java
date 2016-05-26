package com.aw.action;

import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.unity.UnityInstance;

/**
 * <p>Action contexts are passed into actions that are executed, and retain state between actions,
 * allowing for data to be passed between them in the form of variables. Each action type should
 * document the variables it registers in the action context.</p>
 *
 * <p>When a variable path is needed, a dot separated naming convention should be used. For example, if I
 * register a POJO as a variable obect names "myObject" and it has a property called "name", then
 * ${myObject.name} would resolve to that value.</p>
 *
 * <p>Variable resolution is done in the following order:</p>
 * <li> check annotated unity fields on a pojo (For example incident.dg_guid
 * <li> check unity data field - will resolve to a Field object if possible and call Data.getValue(Field)
 * <li> use reflection on the pojo
 *
 * <p>If none of these methods resolve the variable, an exception will be thrown.</p>
 *
 *
 *
 */
public interface ActionContext {

	public static final char VARIABLE_SEPARATOR = '.';

	/**
	 * Register a variable by name - will overwrite previously registered variables
	 *
	 * @param name
	 * @param value
	 */
	public void registerVariable(String name, Object value);

	/**
	 * Get an object given the variable name
	 *
	 * @param variable
	 * @return
	 */
	public <T> T getVariableObject(String variable) throws Exception;

	/**
	 * Get the value for an action variable
	 *
	 * @param variable The variable name
	 * @return The value for the variable
	 */
	public String getVariableString(String variable) throws Exception;

	/**
	 * @return the unity instance for this context
	 */
	public UnityInstance getUnity();

	/**
	 * @return the platform
	 */
	public PlatformMgr getPlatformMgr();

	/**
	 * @return the platform
	 */
	public Platform getPlatform();
}
