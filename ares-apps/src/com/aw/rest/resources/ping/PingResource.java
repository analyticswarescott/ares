package com.aw.rest.resources.ping;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path(com.aw.util.Statics.REST_VERSION + "/ping")
public class PingResource {

	public static final String PATH = "/";

    @GET
    @Path("/")  //NOTE: case must match DG API
    public Response ping() throws Exception {
        return Response.ok().entity("pong").build();
    }
}
