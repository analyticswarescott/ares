package com.aw.common.task;

import java.util.List;

import com.aw.common.exceptions.ConfigurationException;

/**
 * a configuration for a task
 *
 *
 *
 */
public interface TaskConfig {

	/**
	 * @param property get task config property as a list of objects
	 * @return the list of values
	 */
	public <T> List<T> getConfigVector(String property, Class<T> type) throws ConfigurationException;

	/**
	 * @param property name of a scalar config property
	 * @param type type of scalar property
	 * @return the scalar config property
	 */
	public <T> T getConfigScalar(String property, Class<T> type) throws ConfigurationException;

}
