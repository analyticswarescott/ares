package com.aw.compute.streams.processor.referencedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import com.aw.common.inject.TestProvider;
import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.RestClient;
import com.aw.compute.referencedata.AbstractPlatformRestData;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.roles.Rest;

public class AbstractPlatformRestDataTest {

	@Test
	public void check() throws Exception {

		final AtomicInteger refreshes = new AtomicInteger(0);
		Platform platform = new TestPlatform();

		AbstractPlatformRestData data = new AbstractPlatformRestData(NodeRole.REST, Rest.PORT, new TestProvider<Platform>(platform)) {

			@Override
			protected boolean shouldRefresh(String checkPayload) throws ProcessingException {
				return refreshes.get() == 0; //only refresh once
			}

			@Override
			protected void refresh(String pullPayload) throws ProcessingException {
				refreshes.incrementAndGet();
			}

			@Override
			protected String getPullPath() {
				return "pull";
			}

			@Override
			protected String getCheckPath() {
				return "check";
			}

			@Override
			protected RestClient getClient() {
				return new RestClient(new TestProvider<>(platform)) {
					@Override
					public String getString(String path) throws Exception {
						return "data";
					}
				};
			}

		};

		data.check();

		assertEquals("refresh should have been called", 1, refreshes.get());
		assertTrue("age should be less than ttl", data.getAge().compareTo(data.getTTL()) < 0);

	}

}
