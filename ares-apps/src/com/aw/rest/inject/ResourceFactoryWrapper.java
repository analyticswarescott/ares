package com.aw.rest.inject;

import javax.inject.Provider;

import org.glassfish.hk2.api.Factory;

/**
 * A tenant-aware dependency factory for jersey, wrapping our generic dependency providers
 */
public class ResourceFactoryWrapper<T> implements Factory<T> {

	public ResourceFactoryWrapper(Provider<T> provider) {
		this.provider = provider;
	}

	@Override
	public T provide() {
		return provider.get();
	}

	@Override
	public void dispose(T instance) {
	}

	private Provider<T> provider;

}
