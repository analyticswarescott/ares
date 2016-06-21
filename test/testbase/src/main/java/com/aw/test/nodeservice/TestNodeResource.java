package com.aw.test.nodeservice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.aw.platform.monitoring.NodeStatus;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.util.JSONUtils;
import com.aw.document.DocumentHandler;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.NodeManager;
import com.aw.platform.nodes.managers.DefaultRoleManager;
import com.aw.util.Statics;

/**
 * Test node service resource that just responds OK to everything
 *
 * @author jlehmann
 *
 */
@Path(Statics.REST_VERSION + "/node")
public class TestNodeResource {

	private PlatformMgr platformMgr;
	private NodeManager nodeManager;
	private Provider<DocumentHandler> docs;

	@Inject @com.google.inject.Inject
	public TestNodeResource(PlatformMgr platform, NodeManager nodeManager, Provider<DocumentHandler> docs) {
		this.platformMgr = platform;
		this.nodeManager = nodeManager;
		this.docs = docs;
	}

    /**
     * Update a node's configuration
     *
     */
    @PUT
    public String updateNode(String data) throws Exception {

		//logger.debug(data);
        JSONObject j = new JSONObject(data);
        JSONObject rawNode = j.getJSONObject("node");

        Map<String, Map<String, Object>> map = new HashMap<>();
        JSONUtils.updateFromString(rawNode.toString(), map);


        DefaultPlatformNode node = new DefaultPlatformNode();

        node.initialize(platformMgr.getPlatform(), map);

        node.setHost(j.getString("host"));

    	//set up as system scope
    	SecurityUtil.setThreadSystemAccess();


       // logger.trace(" about to get node manager ");

        for (NodeRole role : node.getRoles()) {
        	nodeManager.getRoleManagers().put(role, new TestRoleManager(platformMgr, docs));
        }

    	//update the node
    	nodeManager.updateNode(node);

    	return "ok";

    }

    /**
     * Change the state of a role on a node.  Only RoleStates of RUNNING and STOPPED can be requested
     */
    @PUT
    @Path("/{role}/{state}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public NodeRoleStatus requestNodeState(@PathParam("role") NodeRole role, @PathParam("state") State state) throws Exception {

    	return new NodeRoleStatus(State.RUNNING, "ok");

    }

    /**
     * Get the status of a particular Role configured on this Node
     */
    @GET
    @Path("/{role}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public NodeRoleStatus getStatus(@PathParam("role") NodeRole role) throws Exception {

    	return new NodeRoleStatus(State.RUNNING, "ok");

    }


	/**
	 * Get a collection of node statuses including performance info since the timestamp
	 */
	@GET
	@Path("/status/{ts}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.TEXT_PLAIN)
	public String getStatus(@PathParam("ts") long ts) throws Exception {
		try {
			//set up as system scope
			SecurityUtil.setThreadSystemAccess();
			List<NodeStatus> ns = nodeManager.getStatus(Instant.ofEpochMilli(ts));

			return JSONUtils.objectToString(ns);
		}
		catch (Exception ex) {
			platformMgr.handleException(ex, NodeRole.NODE);
			throw ex;
		}
	}



	/**
     * Get the status of all roles configured on this node.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getStatus() throws Exception {

    	//set up as system scope
    	SecurityUtil.setThreadSystemAccess();

    	//build the return map
    	Map<NodeRole, NodeRoleStatus> status = new HashMap<NodeRole, NodeRoleStatus>();

    	for (NodeRole role : NodeRole.values()) {
            status.put(role, new NodeRoleStatus(State.RUNNING, "ok"));
    	}

        return Response.status(200).entity(status).build();

    }

    public static class TestRoleManager extends DefaultRoleManager {

    	public TestRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
    		super(platform, docs);
		}

    }

	/**
	 * Notify Node of a platform update so it can update its local cache
	 */
	@PUT
	@Path("/platform")
	public Response updatePlatform(String platform) throws Exception {

		//no-op in test

		return Response.ok().build();

	}
}
