package com.aw.rest.resources;

import java.io.File;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.auth.AccessTokenUtil;
import com.aw.auth.AuthHTTPHeaderNames;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.AWFileUtils;
import com.aw.dgui.DGUIMgr;
import com.aw.document.DocumentHandler;
import com.aw.platform.PlatformMgr;
import com.aw.util.Statics;

import io.swagger.annotations.Api;

/**
 * @deprecated This is old legacy code that will need to be ripped out
 */
@Api
@Singleton
@Path(Statics.REST_VERSION)
public class DGUIResource extends RestResourceBase  {
private static final Logger logger = LoggerFactory.getLogger(DGUIResource.class);

	private DGUIMgr _mgr;

	private Provider<DocumentHandler> docs;
	private Provider<PlatformMgr> platformMgr;

	@Inject
	public DGUIResource(Provider<DocumentHandler> docProvider, Provider<PlatformMgr> platformMgr) {
		this.docs = docProvider;
		this.platformMgr = platformMgr;
	}

	private  DGUIMgr getMGR() throws Exception {
		if (_mgr == null) {
			_mgr = new DGUIMgr(this.docs, this.platformMgr);
		}
		return _mgr;
	}

    @POST
    @Path( "/Account/Login" ) //NOTE: case must match DG API
    @PermitAll
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(
        @Context HttpHeaders httpHeaders,
        String strRequest) throws JSONException {

        AccessTokenUtil userAuthenticator = AccessTokenUtil.getInstance();

        JSONObject request = new JSONObject(strRequest);

    	//TODO: check these values aren't empty?
		String userID= (String)request.optString("userID", null);
        String userName = (String)request.get("userName");
        String tenantKey = (String)request.get("tenantKey");

        try {
        	String authToken = userAuthenticator.login(userID, userName, tenantKey);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put(AuthHTTPHeaderNames.accessToken.name(), authToken );

			NoCacheResponse ncr = new NoCacheResponse();
            return ncr.getNoCacheResponseBuilder(Response.Status.OK).entity( jsonObj ).build();

        } catch ( final LoginException ex ) {
        	logger.info("LoginException : "+ex.getMessage());
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("message", "Problem matching badge id, username and password" );

            NoCacheResponse ncr = new NoCacheResponse();
            return ncr.getNoCacheResponseBuilder( Response.Status.UNAUTHORIZED).entity( jsonObj ).build();

        } catch (Exception ex){
        	logger.error("Login in error : "+ ex.getMessage());
        	JSONObject jsonObj = new JSONObject();
        	return Response.status(Status.UNAUTHORIZED).entity(jsonObj).build();
        }
    }

	@GET
	@Path("/PreLoadedGlobalResources")  //NOTE: case must match DG API
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPreloadedGlobalResources(@Context HttpHeaders httpHeaders) throws Exception {

		String locale = httpHeaders.getRequestHeaders().getFirst("Accept-Language");
		String lang = "";
		if (locale.startsWith("en-US")) {
			lang = "en-US";  //default
		}
		else
		{
			//UK style english
			lang = "en";
		}
		JSONArray o =  AWFileUtils.getFileToJSONArray(EnvironmentSettings.getConfDirectory() + File.separatorChar + "dgui_data", "preLoadedGlobalResources-" + lang + ".json");

		return Response.status(200).entity(o).build();

	}

	@GET
	@Path("/Security/Portfolio")  //NOTE: case must match DG API
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSecurityPortfolio() throws Exception {

		JSONObject o =  AWFileUtils.getFileToJSONObject(EnvironmentSettings.getConfDirectory() + File.separatorChar + "dgui_data", "securityPortfolio.json");

		return Response.status(200).entity(o).build();

	}

	// TRAYS and DOCS ----------------------------------------------------------------------------------------------------

	@GET
	@Path("/reporting/trays/{tray_id}/{obj_type}/{obj_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response GetDocFromTray(
			@PathParam("tray_id") String tray_id,
			@PathParam("obj_type") String obj_type,
			@PathParam("obj_id") String obj_id
	) throws Exception {

		throw new WebApplicationException(Response.status(401).entity("no longer implemented").build());

	}

}
