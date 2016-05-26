package com.aw.compute.inject;

import javax.inject.Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * The DI singleton for spark. For now we're using a singleton pattern as a result of the fact that
 * instances are dynamically serialized around the cluster.
 *
 *
 *
 */
public class ComputeInjector {

	private Injector guice;

	/**
	 * The DGInjector is initially created with the default set of dependencies for production use
	 */
	public ComputeInjector() {
		guice = Guice.createInjector(new ComputeModule());
	}

	public ComputeInjector(Module... modules) {
		guice = Guice.createInjector(modules);
	}

	public static synchronized ComputeInjector get() {

		if (injector == null) {
			injector = new ComputeInjector();
		}

		return injector;

	}
	private static ComputeInjector injector;

	public static void init(Module... modules) {

		injector = new ComputeInjector(modules);

	}

	/**
	 * Get an instance from the injector
	 *
	 * @param cls The type to get
	 * @return An instance of the requested type from the di framework
	 */
	public <T> T getInstance(Class<T> cls) {
		return guice.getInstance(cls);
	}

	public <T> Provider<T> getProvider(Class<T> cls) {
		return guice.getProvider(cls);
	}

}
