package com.aw.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.utilities.Binder;

import com.aw.rest.inject.DGBinder;

/**
 * The webapp configuration for a rest cluster member
 */
@ApplicationPath("rest")
public class AWRestPackageConfig extends AbstractResourceConfig {

	public AWRestPackageConfig() throws Exception {
		super();

		//resource providers
		packages(
			"io.swagger.jaxrs.listing",
			"com.fasterxml.jackson.jaxrs.json",
			"com.aw.rest",
			"com.aw.auth.filter",
			"com.aw.unity.security",
			"com.aw.common.rest.resources",
			"com.aw.unity.rest",
			"com.hg.custom.rest"
		);

	}

	@Override
	protected String getSwaggerResourcePackage() {
		return "com.aw.rest.resources";
	}

	@Override
	protected Binder getDefaultBindings() {
		return new DGBinder();
	}

}

