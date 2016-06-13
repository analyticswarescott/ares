package com.aw.rest.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.aw.unity.dg.CommonField;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.RestMgrBase;
import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;
import com.aw.tenant.TenantMgr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Provides api for external DG components to communicate with the platform.
 *
 *
 *
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/ares")
public class AresResource extends RestMgrBase {

	static Logger logger = Logger.getLogger(AresResource.class);

	protected  Provider<PlatformMgr> platformProvider;
	protected  Provider<LocalRestMember> restMember;
	protected  Provider<RestCluster> restCluster;
	protected  TenantMgr tenantMgr;

	@Inject
	public AresResource(Provider<PlatformMgr> platformProvider, Provider<LocalRestMember> restMember, Provider<RestCluster> restCluster, TenantMgr tenantMgr) {
		this.platformProvider = platformProvider;
		this.restMember = restMember;
		this.restCluster = restCluster;
		this.tenantMgr = tenantMgr;
	}


	@PUT
	@Path("/event/{siteId}/event")
/*	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)*/
	public Response putEvent( @PathParam("siteId") String siteId,
							  @Context HttpHeaders headers,
							  String jsonStr) throws WebApplicationException {
		try {


			//site is tenant
			//TODO: handle as array by adding type element to REST signature, for now unpack and add to topic


			JSONArray rawJsons = new JSONArray(jsonStr);

			logger.error("§§§§§§§§§§§§§§±±±±±±± processing JSON: " + rawJsons);


			String eventType = null;
			for (int i = 0; i< rawJsons.length(); i++) {


				JSONObject o = rawJsons.getJSONObject(i);


				eventType = o.getString(CommonField.EVENT_TYPE_FIELD);

				logger.error("§§§§§§§§§§§§§§±±±±±±± processing event of type: " + eventType);


				if (i == 0) {//Write to HDFS here to ensure raw storage is OK before processing anything to Kafka
					//write array to HDFS with a null topic for raw storage with no processing ticket

					String str = rawJsons.toString();
					InputStream is = new ByteArrayInputStream(str.getBytes());

					platformProvider.get().addFile(HadoopPurpose.EVENTS, null, Tenant.forId(siteId), eventType,
						"received_" + Instant.now().toEpochMilli(), UUID.randomUUID().toString(),is
					);

					System.out.println("§§§§§§§§§§§§§§±±±±±±± file added to HDFS ");
				}



				//TODO : replace switch with config
				if (eventType.equals("GameEvent")) {
					platformProvider.get().sendMessage(Topic.EVENTS_ES, Tenant.forId(siteId), o);
					platformProvider.get().sendMessage(Topic.EVENTS_JDBC, Tenant.forId(siteId), o);

					System.out.println("§§§§§§§§§§§§§§±±±±±±± Kafka messages sent ");
				}

			}



			return Response.status(Response.Status.OK).build();

		} catch (WebApplicationException we) {
			throw we;
		} catch (Exception e) {
			// Cleanup, log, etc
			logger.error("Error Processing  Event Request:" , e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}





	/**
     * Provision a tenant
     *
     * @param tenantData
     * @return
     * @throws Exception
     */
    @POST
    @Path("/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Tenant",
    notes = "Creates a new tenant from the provided JSON object")
    public Response createTenant(@ApiParam(value = "The new tenant data", required = true)String tenantData) throws Exception {

    	Tenant tenant = new Tenant(new JSONObject(tenantData));

    	this.restMember.get().provision(tenant);

        return Response.ok("provisioned").build();

    }

    @DELETE
    @Path("tenants/{tenant_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete Tenant",
    notes = "Deletes the tenant with the provided tenant ID")
    public Response deleteTenant(
            @ApiParam(value = "The tenant ID to be deleted", required = true) @PathParam("tenant_id") String tenantID
    ) throws Exception {

        tenantMgr.unProvisionTenant(tenantID);
        return Response.ok().build();
    }












	//TODO: remove

	/**
	 * Add a bundle
	 */
	@POST
	@Path("/{tenantID}/{machineID}/bundles/{bundleID}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("*/*")
	@ApiOperation(value = "Add Bundle",
		notes = "Adds a new bundle with the specified parameters")
	public Response addBundle(
		@ApiParam(value = "the tenant ID to which this bundle belongs", required = true) @PathParam("tenantID") String tenantID,
		@ApiParam(value = "the machine ID to which this bundle belongs", required = true) @PathParam("machineID") String machineID,
		@ApiParam(value = "the bundle ID", required = true) @PathParam("bundleID") String bundleID,
		@ApiParam(value = "the bundle to be added", required = true) InputStream bundle
	) throws Exception {

		//add the bundle
		//this.platformProvider.get().addFile(HadoopPurpose.BUNDLE, Topic.BUNDLE_REF, new Tenant(tenantID), machineID, bundleID, bundleID, bundle);
		return Response.status(HttpStatus.ACCEPTED_202).build();

	}

	/**
	 * Add an EDR scan
	 */
	@POST
	@Path("/{tenantID}/{machineID}/edr_scans/{scanID}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("*/*")
	@ApiOperation(value = "Add EDR Scan without sequence number")
	public Response addEDRScan(
		@ApiParam(value = "the tenant ID to which this EDR scan belongs", required = true) @PathParam("tenantID") String tenantID,
		@ApiParam(value = "the machine ID to which this EDR scan belongs", required = true) @PathParam("machineID") String machineID,
		@ApiParam(value = "the scan ID", required = true) @PathParam("scanID") String scanID,
		@ApiParam(value = "the scan data", required = true) InputStream scanData
	) throws Exception {

		//add the file
		//this.platformProvider.get().addFile(HadoopPurpose.EDR, Topic.SCAN_REF, new Tenant(tenantID), machineID, scanID, scanID, scanData);
		return Response.status(HttpStatus.ACCEPTED_202).build();

	}

	/**
	 * Add an EDR scan
	 */
	@POST
	@Path("/{tenantID}/{machineID}/edr_scans/{scanID}/{sequenceNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("*/*")
	@ApiOperation(value = "Add EDR Scan",
		notes = "Adds an EDR scan in parts")
	public Response addEDRScan(
		@ApiParam(value = "the tenant ID to which this EDR scan belongs", required = true) @PathParam("tenantID") String tenantID,
		@ApiParam(value = "the machine ID to which this EDR scan belongs", required = true) @PathParam("machineID") String machineID,
		@ApiParam(value = "the scan ID", required = true) @PathParam("scanID") String scanID,
		@ApiParam(value = "the sequence number")@PathParam("sequenceNumber") int sequenceNumber,
		@ApiParam(value = "the scan data", required = true) InputStream scanData
	) throws Exception {

		//add the partial file
		//this.platformProvider.get().addFilePart(HadoopPurpose.EDR, new Tenant(tenantID), machineID, scanID, scanID, scanData, sequenceNumber);
		return Response.status(HttpStatus.ACCEPTED_202).build();

	}

	/**
	 * Complete an EDR scan that was sent in parts
	 */
	@POST
	@Path("/{tenantID}/{machineID}/edr_scans/{scanID}/complete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("*/*")
	@ApiOperation(value = "Complete Scan",
		notes = "Complete an EDR scan that was sent in parts")
	public Response completeScan(
		@ApiParam(value = "the tenant ID to which this EDR scan belongs", required = true) @PathParam("tenantID") String tenantID,
		@ApiParam(value = "the machine ID to which this EDR scan belongs", required = true) @PathParam("machineID") String machineID,
		@ApiParam(value = "the scan ID", required = true) @PathParam("scanID") String scanID
	) throws Exception {

		//complete the file, telling the platform about it on the edr scan topic
		//this.platformProvider.get().completeFile(HadoopPurpose.EDR, Topic.SCAN_REF, new Tenant(tenantID), machineID, scanID, scanID);
		return Response.status(HttpStatus.ACCEPTED_202).build();

	}



}
