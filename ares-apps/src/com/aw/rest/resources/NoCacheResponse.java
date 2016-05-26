package com.aw.rest.resources;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

public class NoCacheResponse {
	public Response.ResponseBuilder getNoCacheResponseBuilder( Response.Status status ) {
        CacheControl cc = new CacheControl();
        cc.setNoCache( true );
        cc.setMaxAge( -1 );
        cc.setMustRevalidate( true );

        return Response.status( status ).cacheControl( cc );
    }
}