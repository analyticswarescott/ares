package com.aw.compute.inject;

import javax.inject.Provider;

/**
 * allows classes that are dependent on others and not injectable via constructor injection to get dependencies within spark
 *
 *
 *
 */
public interface Dependent {

	/**
	 * Get a dependency within the spark compute framework
	 *
	 * @param type the type of dependency to get
	 * @return the dependency
	 */
	default public <T> T getDependency(Class<T> type) {

		return ComputeInjector.get().getInstance(type);

	}

	/**
	 * Get a dependency within the spark compute framework
	 *
	 * @param type the type of dependency to get
	 * @return the dependency
	 */
	default public <T> Provider<T> getProviderDependency(Class<T> type) {

		return ComputeInjector.get().getProvider(type);

	}
}
