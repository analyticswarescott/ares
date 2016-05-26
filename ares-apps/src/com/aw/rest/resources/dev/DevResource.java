package com.aw.rest.resources.dev;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.system.EnvironmentSettings;
import com.aw.rest.resources.RestResourceBase;
import com.aw.tenant.TenantMgr;

/**
 * TODO: remove this?
 */
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/dev")
public class DevResource extends RestResourceBase {

    @Context
    ServletContext _context; //get startup info from web.xml

    private TenantMgr tenantMgr;

    @Inject @com.google.inject.Inject
    public DevResource(TenantMgr tenantMgr) {
    	this.tenantMgr = tenantMgr;
	}

    @GET
    @Path("/tenant/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTenant() throws Exception {
        devModeValidation();
        return Response.ok().entity(tenantMgr.getAllTenantIDs()).build();
    }

    private void devModeValidation() throws Exception {
        if (! (EnvironmentSettings.isDevelopmentMode() || EnvironmentSettings.isTestMode()) ) {
            throw new Exception("Only legal to do this operation in dev or test mode");
        }
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() throws Exception {
        devModeValidation();

        JSONArray allTenantIDs = tenantMgr.getAllTenantIDs();

        // todo
        JSONArray users = new JSONArray();
        for (int x=0; x<allTenantIDs.length(); x++) {
            JSONObject user = new JSONObject();
            user.put("user_name", "bob-" + allTenantIDs.get(x));
            user.put("tenant_id", allTenantIDs.get(x));
            users.put( user );
        }
        for (int x=0; x<allTenantIDs.length(); x++) {
            JSONObject user = new JSONObject();
            user.put("user_name", "joe-" + allTenantIDs.get(x));
            user.put("tenant_id", allTenantIDs.get(x));
            users.put( user );
        }
        for (int x=0; x<allTenantIDs.length(); x++) {
            JSONObject user = new JSONObject();
            user.put("user_name", "jane-" + allTenantIDs.get(x));
            user.put("tenant_id", allTenantIDs.get(x));
            users.put( user );
        }
        for (int x=0; x<allTenantIDs.length(); x++) {
            JSONObject user = new JSONObject();
            user.put("user_name", "jack-" + allTenantIDs.get(x));
            user.put("tenant_id", allTenantIDs.get(x));
            users.put( user );
        }
        return Response.ok().entity(users).build();
    }

}
