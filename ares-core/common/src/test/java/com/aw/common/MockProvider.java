package com.aw.common;

import javax.inject.Provider;
import static org.mockito.Mockito.*;

/**
 * Mock service provider
 *
 *
 *
 * @param <T>
 */
public class MockProvider<T> implements Provider<T> {

	private T object;

	public MockProvider(Class<T> type) {
		this.object = mock(type);
	}

	@Override
	public T get() {
		return this.object;
	}

}
