package com.aw.rest.provider;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.aw.common.exceptions.InvalidRequestException;
import com.aw.platform.PlatformMgr;

/**
 * Invalid request exceptions, meaning the caller made a mistake
 *
 *
 *
 */
@Provider
public class InvalidRequestExceptionMapper extends AbstractExceptionResponseMapper<InvalidRequestException> {

	@Inject
	public InvalidRequestExceptionMapper(javax.inject.Provider<PlatformMgr> platformMgr) {
		super(platformMgr);
	}

	@Override
	protected Status getResponseStatus(InvalidRequestException exception) {
		return Status.BAD_REQUEST;
	}

	@Override
	protected String getErrorMessage(InvalidRequestException exception) {
		return exception.getMessage();
	}

}
