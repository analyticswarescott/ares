package com.aw.action;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;

import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.UnityInstance;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default implementation of action context, providing variable resolution
 *
 *
 *
 */
public class DefaultActionContext implements ActionContext {

	private UnityInstance unity;
	private Provider<PlatformMgr> platformMgr;

	public DefaultActionContext(UnityInstance unity, Provider<PlatformMgr> platformMgr) {
		this.unity = unity;
		this.platformMgr = platformMgr;
	}

	@Override
	public <T> T getVariableObject(String variable) throws Exception {
		return (T)resolve(variable);
	}

	@Override
	public String getVariableString(String variable) throws Exception {

		Object ret = resolve(variable);
		if (ret != null) {
			ret = String.valueOf(ret);
		}
		return (String)ret;
	}

	@Override
	public void registerVariable(String name, Object value) {
		m_variables.put(name, value);
	}

	/**
	 * Resolve a variable name
	 * @param strPath The path to the variable
	 * @return The object, or null if not found
	 */
	private Object resolve(String strPath) throws Exception {

		//split the path up first
		String[] path = StringUtils.split(strPath, VARIABLE_SEPARATOR);

		//resolve each element
		int curPath = 0;
		Object curObj = m_variables.get(path[curPath++]);
		while (curPath < path.length) {

			//if we're null, stop here - we return null
			if (curObj == null) {
				break;
			}

			//resolve the next object in the path
			curObj = resolve(curObj, path[curPath]);

			//next piece
			curPath++;

		}

		//return whatever we have
		return curObj;

	}

	/**
	 * Resolve the next element in the path
	 *
	 * @param obj The current object
	 * @param name The next element in the path
	 * @return
	 */
	private Object resolve(Object obj, String name) throws Exception {

		//first look for a bean property match using reflection
		Object ret = checkBean(obj, name);

		//if no bean match, check data
		if (ret == null) {
			ret = checkData(obj, name);
		}

		//worst case match against known annotations
		if (ret == null) {
			ret = checkAnnotations(obj, name);
		}

		return ret;

	}

	private Object checkAnnotations(Object obj, String name) throws Exception {

		Object ret = null;

		//look for annotations
		for (Method method : obj.getClass().getMethods()) {

			JsonProperty prop = method.getAnnotation(JsonProperty.class);
			if (prop != null) {

				//invoke the getter if it's a match
				if (name.equals(prop.value())) {
					ret = method.invoke(obj);
					break;
				}

			}

		}

		//return what we found, if anything
		return ret;

	}

	//see if we can resolve a property using a unity data field
	private Object checkData(Object obj, String property) throws Exception {

		Object ret = null;

		//if unity data, get the field with the given name and return that, if it's there
		if (obj instanceof Data) {

			Data data = (Data)obj;
			DataType type = data.getType();

			if (type.hasField(property)) {
				Field field = type.getField(property);
				ret = data.getValue(field);
			}

		}

		return ret;

	}

	private Object checkBean(Object obj, String name) throws Exception {

		Object ret = null;

		//look for the property
		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		for (PropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
			if (name.equals(desc.getName()) && desc.getReadMethod() != null) {
				desc.getReadMethod().setAccessible(true);
				ret = desc.getReadMethod().invoke(obj);
				break;
			}

		}

		//return what we found, if anything
		return ret;

	}

	//registered variables
	public Map<String, Object> getVariables() { return m_variables; }
	private Map<String, Object> m_variables = new HashMap<String, Object>();

	@Override
	public UnityInstance getUnity() {
		return unity;
	}

	public Platform getPlatform() {
		return platformMgr.get().getPlatform();
	}

	public PlatformMgr getPlatformMgr() {
		return platformMgr.get();
	}

}
