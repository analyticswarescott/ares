package com.aw.test.nodeservice;

import org.glassfish.hk2.utilities.Binder;

import com.aw.rest.AbstractResourceConfig;

public class TestNodeResourceConfig extends AbstractResourceConfig {

	public TestNodeResourceConfig() throws Exception {
		super();

		//node service resource providers
		packages(
			"io.swagger.jaxrs.listing",
			"com.fasterxml.jackson.jaxrs.json",
			"com.aw.test.nodeservice",
			"com.aw.rest.resources.ping",
			"com.aw.auth.filter",
			"com.aw.unity.security"
		);

	}

	@Override
	protected String getSwaggerResourcePackage() {
		return "com.aw.test.nodeservice";
	}

	@Override
	protected Binder getDefaultBindings() {
		return new TestNodeBinder();
	}

}
