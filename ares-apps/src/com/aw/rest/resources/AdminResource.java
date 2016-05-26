package com.aw.rest.resources;


import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.hadoop.exceptions.FileWriteException;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;
import com.aw.platform.restcluster.PlatformController.PlatformState;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/admin")
public  class AdminResource extends RestResourceBase {

	public static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

	Provider<DocumentHandler> docs;
	Provider<Platform> platform;
	Provider<PlatformMgr> platformMgr;
	Provider<LocalRestMember> restMember;
	Provider<RestCluster> restCluster;

	@Inject
	public AdminResource(Provider<DocumentHandler> docs, Provider<PlatformMgr> platformMgr, Provider<Platform> platform, Provider<LocalRestMember> restMember, Provider<RestCluster> restCluster) {
		this.docs = docs;
		this.platform = platform;
		this.platformMgr = platformMgr;
		this.restMember = restMember;
		this.restCluster = restCluster;
	}

	@GET
	@Path("platform/")
	@ApiOperation(value = "Get Platform",
	notes = "Returns the current platform",
	response = Platform.class)
	public Response getPlatform () throws Exception {
		try {

			return Response.status(200).entity(this.platform.get().toString()).build();

		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			throw new WebApplicationException(e);
		}
	}


	/**
	 * Apply a patch; effectively setting a version that all nodes must conform to
	 */
	@POST
	@Path("platform/patch/{version_id}")
	//@Produces(MediaType.APPLICATION_JSON)
	//@Consumes("*/*")
	@ApiOperation(value = "Apply patch",
	notes = "Apply a patch; effectively setting a version that all nodes must conform to",
	response = HttpStatus.class)
	public Response applyPatch(
		//@PathParam("tenantID") String tenantID, @PathParam("machineID") String machineID, @PathParam("scanID") String scanID,
		@ApiParam(value = "version that all nodes must conform to", required = true) @PathParam("version_id") String versionID,
		@ApiParam(value = "the patch file", required = true)InputStream patchFile) throws Exception {

		//TODO: do we need anything besides the patch file (i.e. will it contain metadata)

		try {
			logger.warn("upgrade request received to version " + versionID);

			//save to HDFS
			PlatformUpgrade upgrade = upgradePlatform(versionID, patchFile);

			this.restMember.get().upgrade(upgrade);


			return Response.status(HttpStatus.ACCEPTED_202).build();
		}
		catch (Exception ex) {
			logger.error(" exception receiving patch " + ex.getMessage());
			throw ex;
		}

	}

	public PlatformUpgrade upgradePlatform(String versionID, InputStream patchFile) throws Exception {
		return new PlatformUpgrade(platformMgr.get(), versionID, patchFile);
	}



	@PUT
	@Path("platform/")
	@ApiOperation(value = "Update Platform",
	notes = "Updates the platform to the specified platform document")
	public Response updatePlatform (@ApiParam(value = "the updated platform document", required = true) String platformDoc
	) throws Exception {
		try {

			logger.debug(" platform update request received ");
			SecurityUtil.setThreadSystemAccess();

			Document doc = new Document(platformDoc);

			try {
				this.docs.get().updateDocument(doc);
			}
			catch (FileWriteException fwe) {
				if (this.restCluster.get().getPlatformState() != PlatformController.PlatformState.RUNNING) {
					logger.warn(" ignoring FileWrite exception when platform is not RUNNING");
				}
			}

			return Response.status(200).entity("ok").build();

		} catch (WebApplicationException we) {
			throw we;
		} catch (JSONException je) {
			logger.error("Invalid Platform JSON Provided");
			return Response.status(HttpStatus.BAD_REQUEST_400).entity("Not a valid platform document").build();
		} catch (Exception e) {
			// Cleanup, log, etc
			e.printStackTrace();
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(e);
		}
	}


	@PUT
	@Path("platform/{state}")
	@ApiOperation(value = "Change platform state",
	notes = "Changes the platform state to the newly specified state")
	public Response platformControl (@ApiParam(value = "the new platform state", required = true) @PathParam("state") PlatformState state
	) throws Exception {
		try {

			logger.debug(" platform state change to " + state + "  request received ");

			this.restMember.get().requestPlatformState(state);

			return Response.status(200).entity("ok").build();

		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			e.printStackTrace();
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(e);
		}
	}






	@POST
	@Path("register/")
	@ApiOperation(value = "Register driver",
	notes = "Registers the newly specified driver",
	response = DriverRegistrationResponse.class)
	public DriverRegistrationResponse registerDriver (@ApiParam(value = "the new driver information", required = true) String driverInfo
	) throws Exception {
		try {

			JSONObject driver = new JSONObject(driverInfo);
			DriverRegistrationResponse ret = this.restMember.get().registerDriver(driver);
			return ret;

		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			e.printStackTrace();
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(e);
		}
	}


	@POST
	@Path("register/{driver_name}/")
	@ApiOperation(value = "Register Processor for driver",
	notes = "Registers a newly specified processor for a driver")
	public Response registerProc (@ApiParam(value = "the name of the driver", required = true) @PathParam("driver_name") String driverName,
			@ApiParam(value = "the name of the processor", required = true) String processorName)
	 throws Exception {
		try {
			this.restMember.get().registerProcessor(driverName, processorName);
			return Response.status(201).entity("registered").build();

		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			e.printStackTrace();
			logger.error("Error Processing Server Status Request", e);
			throw new WebApplicationException(e);
		}
	}
}
