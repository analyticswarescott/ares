package com.aw.rest;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.EnvironmentSettings.Setting;
import com.aw.util.Statics;

import io.swagger.jaxrs.config.BeanConfig;

/**
 * Base resource config for DG resources
 *
 *
 *
 */
public abstract class AbstractResourceConfig extends ResourceConfig {

	public AbstractResourceConfig() throws Exception {

		//set up swagger
		BeanConfig beanConfig = new BeanConfig();
	    beanConfig.setVersion(Statics.REST_VERSION);
	    beanConfig.setSchemes(Statics.SCHEMES);
	    beanConfig.setHost(EnvironmentSettings.getHost());
	    beanConfig.setBasePath(Statics.VERSIONED_REST_PREFIX);
	    beanConfig.setResourcePackage(getSwaggerResourcePackage());
	    beanConfig.setScan(true);

	    registerBindings();

	}

	protected void registerBindings() throws Exception {

		//check for custom di bindings
		String strDiCls = EnvironmentSettings.fetch(Setting.DEPENDENCY_INJECTOR);

		Object bindings = null;
		if (strDiCls != null) {
			bindings = Class.forName(strDiCls).newInstance();
		}

		else {
			bindings = getDefaultBindings();
		}

		//bind our custom DI providers
		register(bindings);
		register(MultiPartFeature.class);
		//register(new LoggingFilter(Logger.getLogger("test"), true));
	}

	/**
	 * @return swagger documented resource package(s)
	 */
	protected abstract String getSwaggerResourcePackage();

	/**
	 * @return default dependency injection bindings if environment override has not been set
	 */
	protected abstract Binder getDefaultBindings();

}
