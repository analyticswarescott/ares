package com.aw.rest;

import javax.ws.rs.ApplicationPath;

import com.aw.util.Statics;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.Binder;

import com.aw.platform.NodeBinder;

@ApplicationPath("rest")
public class NodeResourceConfig extends AbstractResourceConfig {

	public static final Logger LOGGER = Logger.getLogger(NodeResourceConfig.class);

	public NodeResourceConfig() throws Exception {
		super();

		System.out.println("\n\n\n\nin node resource config before packages\n\n\n\n");

		try {

			//node service resource providers
			packages(
				"io.swagger.jaxrs.listing",
				"com.fasterxml.jackson.jaxrs.json",
				"com.aw.platform",
				"com.aw.rest.resources.ping",
				"com.aw.auth.filter",
				"com.aw.unity.security"
			);

			for (Class cls : getClasses()) {
				System.out.println("class: " + cls);
			}

		} catch (Throwable t) {
			LOGGER.error("IN NODE RESOURCE CONFIG", t);
		}

	}

	@Override
	protected String getSwaggerResourcePackage() {
		return "com.aw.platform";
	}

	@Override
	protected Binder getDefaultBindings() {
		return new NodeBinder();
	}

	@Override
	protected String getBasePath() {
		return Statics.VERSIONED_REST_PREFIX;
	}

}
