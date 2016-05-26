package com.aw.rest.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.aw.common.util.JSONUtils;
import com.aw.platform.monitoring.os.OSPerfStats;
import com.aw.platform.restcluster.PlatformController;
import org.apache.log4j.Logger;

import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.monitoring.PlatformStatus;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.RestCluster;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.time.Instant;

/**
 * Error handling rest resource
 *
 *
 */
@Api
@Path(com.aw.util.Statics.REST_VERSION + "/platform")
public class PlatformResource {

	static final Logger logger = Logger.getLogger(PlatformResource.class);

	private Provider<RestCluster> restCluster;
	private Provider<DocumentHandler> docs;
	private Provider<PlatformMgr> platformMgr;
	private Provider<LocalRestMember> restMember;

	@Inject @com.google.inject.Inject
	public PlatformResource(Provider<RestCluster> restCluster, Provider<DocumentHandler> docs, Provider<PlatformMgr> platformMgr, Provider<LocalRestMember> restMember) {
		this.restCluster = restCluster;
		this.docs = docs;
		this.platformMgr = platformMgr;
		this.restMember = restMember;
	}

	/**
	 * Add an available node to the platform
	 *
	 * @param host The hostname of the node as it appears in the platform configuration
	 */
    @POST
    @Path("/node/{host}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Announce Node",
    	notes = "informs service cluster that a node is present in the platform",
    	response = String.class)
    public String nodePresent(@PathParam("host") String host) throws Exception {

    	logger.info("Node " + host + " has called in as available");

		//react to this report
		this.restMember.get().nodeReporting(host);

		//return the entire platform for caching by the node
		//TODO: switch all cache logic to only cache body?
		//Document current = getSysDocHandler().getDocument(DocumentType.PLATFORM, Platform.LOCAL);

		Document current = PlatformMgr.getCachedPlatformDoc();

		return current.toJSON().toString();

    }


	@POST
	@Path("/perfstats")
/*	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)*/
	public void addPerfStats(@Context HttpServletRequest hsr, String statsString) throws Exception{

		OSPerfStats stats = JSONUtils.objectFromString(statsString, OSPerfStats.class);
		//visibility handled by the doc handler for this user
		this.platformMgr.get().handlePerfStats(stats);

	}

    @POST
    @Path("/errors")
    public void addError(@Context HttpServletRequest hsr, String message, @PathParam("origin") @DefaultValue("rest") NodeRole origin) {

    	//visibility handled by the doc handler for this user
    	this.platformMgr.get().handleError(hsr.getRemoteHost(), message, origin);

    }

    @POST
    @Path("/logs")
    public void addLog(@Context HttpServletRequest hsr, String message) {

    	//visibility handled by the doc handler for this user
    	this.platformMgr.get().handleLog(hsr.getRemoteHost(), message, NodeRole.REST);

    }

    @GET
    @Path("/status/{timestamp}")
    @Produces(MediaType.APPLICATION_JSON)

    public PlatformStatus getStatus(@PathParam("timestamp") long statTime) throws Exception {

		return this.restMember.get().getStatus(this.docs.get(), Instant.ofEpochMilli(statTime));

	}

	@GET
	@Path("/state")
	@Produces(MediaType.APPLICATION_JSON)

	public PlatformController.PlatformState getState() throws Exception {
		try {
			return this.restMember.get().getPlatformState();
		}
		catch (Exception ex) {
			logger.warn(" error retrieving platform state: " + ex.getMessage(), ex);
			return PlatformController.PlatformState.UNKNOWN;
		}

	}

}
