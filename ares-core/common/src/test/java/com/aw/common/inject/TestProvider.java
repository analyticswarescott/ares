package com.aw.common.inject;

import javax.inject.Provider;

/**
 * Test provider
 *
 *
 *
 * @param <T>
 */
public class TestProvider<T> implements Provider<T> {

	T wrapped;

	public TestProvider(T wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public T get() {
		return wrapped;
	}

}
