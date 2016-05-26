package com.aw.rest.provider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.aw.platform.PlatformMgr;

/**
 * Exception mapper that handles {@link WebApplicationException}s.
 */
@Provider
public class WebApplicationExceptionResponseMapper extends AbstractExceptionResponseMapper<WebApplicationException> {

	@Inject
	HttpServletRequest req;

	@Inject @com.google.inject.Inject
	public WebApplicationExceptionResponseMapper(javax.inject.Provider<PlatformMgr> platformMgr) {
		super(platformMgr);
	}

	@Override
	protected Response.Status getResponseStatus(WebApplicationException wae) {

		int status = wae.getResponse().getStatus();
		Response.Status ret = Response.Status.fromStatusCode(status);

		//for now just use the generic code
		if (ret == null) {
			status -= status % 100;
		}
		ret = Response.Status.fromStatusCode(status);

		return ret;

	}

}
