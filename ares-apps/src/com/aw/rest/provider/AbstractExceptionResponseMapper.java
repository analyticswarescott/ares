package com.aw.rest.provider;

import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONUtils;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.util.Statics;

/**
 * Base class for all exception mapper instances.  All subclasses should be annotated with
 * the {@link javax.ws.rs.ext.Provider} annotation.
 */
abstract class AbstractExceptionResponseMapper<T extends Throwable> implements ExceptionMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionResponseMapper.class);

	private Provider<PlatformMgr> platformMgr;

	public AbstractExceptionResponseMapper(Provider<PlatformMgr> platformMgr) {
		this.platformMgr = platformMgr;
	}

	@Override
	public final Response toResponse(T exception) {

		final Response.Status responseStatus = getResponseStatus(exception);

		if (responseStatus == null) {
			throw new IllegalArgumentException("A valid response status is required!");
		}

		//log unsuccessful requests
		if (!HttpStatusUtils.isSuccessful(responseStatus.getStatusCode())) {
			LOGGER.warn("exception detected", exception);

			//TODO: this needs to be done away with in favor of an injected security context
			if (ThreadLocalStore.get() == null) {
				SecurityUtil.setThreadSystemAccess();
			}

			LOGGER.warn("handling exception in platform..");
			this.platformMgr.get().handleException(exception, NodeRole.REST);
			LOGGER.warn("done handling exception in platform.");
		}

		// Add the JSON data for this exception
		final JSONObject jsonObject = new JSONObject();
		try {
			String errorMessage = getErrorMessage(exception);
			if (errorMessage == null) {
				errorMessage = responseStatus.getReasonPhrase();
			}
			jsonObject.put(Statics.ERROR_MESSAGE, errorMessage)
				.put("data", getDataPayload(exception));
			LOGGER.info("returning response: " + jsonObject.toString(4));
		} catch (JSONException e) {
			LOGGER.warn("Unable to generate error response", e);
		}

		// Return a JSON-encoded response with the exception data
		return Response
			.status(responseStatus)
			.header("Content-Type", MediaType.APPLICATION_JSON)
			.entity(JSONUtils.objectToString(jsonObject))
			.build();
	}

	/**
	 * Each exception must specify the HTTP status code that will be returned to the client.
	 * @param exception The exception that was thrown.
	 * @return The HTTP status code that corresponds to the error.
     */
	protected abstract Response.Status getResponseStatus(T exception);

	/**
	 * A message to display to the client that describes the error.
	 * @param exception TODO
	 * @return A String containing the error message.
     */
	protected String getErrorMessage(T exception) {
		// The default behavior is to use the reason phrase from the response status
		return exception.getMessage();
	}

	/**
	 * Allows adding exception-specific data.
	 * @param exception The exception that was thrown.
	 * @return A JSONObject with data, or null.
     */
	protected JSONObject getDataPayload(T exception) {
		// By default, no data payload is added
		return null;
	}

}
