package com.aw.rest.provider;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.aw.platform.PlatformMgr;

/**
 * Default mapper for any unspecified exceptions thrown by the system.
 */
@Provider
public class DefaultExceptionResponseMapper extends AbstractExceptionResponseMapper<Exception> {

	@Inject
	public DefaultExceptionResponseMapper(javax.inject.Provider<PlatformMgr> platformMgr) {
		super(platformMgr);
	}

	@Override
	protected Response.Status getResponseStatus(Exception e) {
		return Response.Status.INTERNAL_SERVER_ERROR;
	}

}
