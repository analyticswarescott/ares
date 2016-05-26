package com.aw.rest.resources;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.AWFileUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api
@Path(com.aw.util.Statics.REST_VERSION + "/dgrest")
public class SpoofREST {
private static final Logger logger = LoggerFactory.getLogger(SpoofREST.class);
	// Error Codes
	private static final int HTTP_ERROR_CLIENT = 450;
	private static final int HTTP_ERROR_SERVER = 550;

	@GET
	@Path("/ApplicationLookupAttribute/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Application Lookup Attribute",
	notes = "Get the application lookup attribute for the specified tenant",
	response = JSONObject.class)
	public Response getApplicationLookupAttribute(
			@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID
			) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "application_lookup_attribute");
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/ApplicationUser/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Application User",
	notes = "Get the application user for the specified tenant",
	response = JSONObject.class)
	public Response getApplicationUser(
			@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID
	) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "application_user");
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/ApplicationPolicy/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Application User",
	notes = "Get the application user for the specified tenant",
	response = JSONObject.class)
	public Response getApplicationPolicy(
			@PathParam("tenant_id") String tenantID
	) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "application_policy");
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}
	@GET
	@Path("/ApplicationRule/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Application Rule",
	notes = "Get the application rule for the specified tenant",
	response = JSONObject.class)
	public Response getApplicationRule(
			@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID
	) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "application_rule");
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/ApplicationTag/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Application Tag",
	notes = "Get the application tag for the specified tenant",
	response = JSONObject.class)
	public Response getApplicationTag(
			@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID
	) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "application_tag");
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/WkrFedUserGroup/{tenant_id}/{user_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get WKR Fed",
	notes = "Get the WKR Fed for the specified tenant",
	response = JSONObject.class)
	public Response getWkrFed(
			@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID, @PathParam("tenant_id") String userID
	) throws WebApplicationException {
		try {
			JSONObject ret = fetchTenantMetadataResource(tenantID, "wkr_fed" + userID);
			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Returns Status of EventProc Server.
	 * @return
	 * @throws WebApplicationException
	 */
	@GET
	@Path("footable")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Sever Status",
	notes = "Returns Status of EventProc Server",
	response = JSONObject.class)
	public Response getServerStatus( ) throws WebApplicationException {
		try {
			JSONObject ret = new JSONObject();

			JSONArray entities = new JSONArray();
			JSONObject ent = new JSONObject();
			ent.put("foo", "bar");
			entities.put(ent);
			ret.put("entities", entities);

			DateTime d = new DateTime();
			ret.put("currentTick", d.getMillis());

			return Response.status(200).entity(ret).build();
		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	private JSONObject fetchTenantMetadataResource(String tenantID, String resource) throws Exception {
		String confDirectory = EnvironmentSettings.getConfDirectory() + System.getProperty("file.separator") + "/test";
		String metadataFilename = "tenant_" + tenantID + "_reference_" + resource + ".json";
		File metadata = new File(confDirectory, metadataFilename);
		DateTime d = new DateTime();
		JSONObject ret = AWFileUtils.getFileToJSONObject(metadata);
		ret.put("currentTick", d.getMillis());
		return ret;
	}
}
