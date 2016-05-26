package com.aw.common.inject;

import com.google.inject.Provider;

/**
 * Google guice provider to wrap JSR-330 providers. This is currently necessary as
 * spark's guice (which is shades in its own jar) doesn't support JSR-330.
 *
 *
 *
 * @param <T>
 */
public class ProviderWrapper<T> implements Provider<T> {

	private javax.inject.Provider<T> wrapped;

	public ProviderWrapper(javax.inject.Provider<T> wrapped) {

		this.wrapped = wrapped;
	}

	@Override
	public T get() {

		return wrapped.get();

	}

}
