package com.aw.rest.provider;

import com.aw.common.util.EnumDeserializationException;
import com.aw.platform.PlatformMgr;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author jhaight
 */
@Provider
public class EnumDeserializationExceptionMapper extends AbstractExceptionResponseMapper<EnumDeserializationException> {

	@Inject
	public EnumDeserializationExceptionMapper(javax.inject.Provider<PlatformMgr> platformMgr) {
		super(platformMgr);
	}

	@Override
	protected Response.Status getResponseStatus(EnumDeserializationException exception) {
		return Response.Status.BAD_REQUEST;
	}

	@Override
	protected String getErrorMessage(EnumDeserializationException exception) {
		return "Could not deserialize enum " + exception.getUnparsableEnum();
	}
}
