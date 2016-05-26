package com.aw.rest.resources;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;

/**
 * Legacy rest resource base, may be removed in future build unless we figure out a reason for its existence
 */
public class RestResourceBase {

	/**
	 * Standard check for consistent handling of missing resources
	 *
	 * @param resource The resource to check
	 * @throws WebApplicationException If the resource is missing
	 */
	protected void checkMissing(Object resource) throws WebApplicationException {
		if (resource == null) {
			throw new WebApplicationException(HttpStatus.NOT_FOUND_404);
		}
	}

    //reads a guid, throws an appropriate exception if it can't be parsed
    protected UUID readGuid(String strGuid) throws Exception {

    	//make sure we have a valid UUID
    	UUID guid = null;
    	try {
        	guid = UUID.fromString(strGuid);
    	} catch (Exception e) {
    		throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity("invalid guid: " + guid).build());
    	}

    	return guid;

    }

}
