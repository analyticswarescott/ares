package com.aw.platform;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.util.JSONUtils;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.NodeManager;
import com.aw.util.Statics;

import io.swagger.annotations.Api;

/**
 * The node service api
 *
 *
 *
 */
@Path(Statics.REST_VERSION + "/node")
@Api
public class NodeResource {

    public static final Logger logger = LoggerFactory.getLogger(NodeResource.class);

    private PlatformMgr platformMgr;
    private NodeManager nodeManager;

    @Inject @com.google.inject.Inject
    public NodeResource(PlatformMgr platformMgr, NodeManager nodeManager) {
    	this.platformMgr = platformMgr;
    	this.nodeManager = nodeManager;
	}

    /**
     * Update a node's configuration
     *
     */
    @PUT
   // @Produces(MediaType.APPLICATION_JSON)
   // @Consumes(MediaType.APPLICATION_JSON)
    public String updateNode(String data) throws Exception {

        //TODO: this is a workaround for the fact that in a localhost deployment, host cannot be self-set

        logger.debug(" node update request received ");

		//logger.debug(data);
        JSONObject j = new JSONObject(data);
        JSONObject rawNode = j.getJSONObject("node");

        Map<String, Map<String, Object>> map = new HashMap<>();
        JSONUtils.updateFromString(rawNode.toString(), map);


        DefaultPlatformNode node = new DefaultPlatformNode();

		platformMgr.reset();//need to clear this JVMs platform
		Platform p = platformMgr.getPlatform();

		logger.debug(JSONUtils.objectToString(p));

        node.initialize(p, map);

        node.setHost(j.getString("host"));
    	//set up as system scope
    	SecurityUtil.setThreadSystemAccess();


       // logger.trace(" about to get node manager ");

        logger.warn(nodeManager.toString());

    	//update the node
    	nodeManager.updateNode(node);
        return "ok";

    }

	/**
	 * Change the state of a role on a node.  Only RoleStates of RUNNING and STOPPED can be requested
	 */
	@PUT
	@Path("/{state}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<NodeStatus> requestNodeState(@PathParam("state") NodeStatus.State state) throws Exception {

		logger.warn("state change request received for node to state " + state);

		try {

			//set up as system scope
			SecurityUtil.setThreadSystemAccess();

			nodeManager.updateNodeState(state);
			logger.warn(" Node state updated ");
			return nodeManager.getStatus(Instant.ofEpochMilli(0)); //return a basic Node status as opposed to a list of perf stats

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

    /**
     * Change the state of a role on a node.  Only RoleStates of RUNNING and STOPPED can be requested
     */
    @PUT
    @Path("/{role}/{state}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public NodeRoleStatus requestNodeRoleState(@PathParam("role") NodeRole role, @PathParam("state") State state) throws Exception {

        logger.debug("state change request received for role " + role + " to state " + state);

        try {

            //set up as system scope
        	SecurityUtil.setThreadSystemAccess();

        	nodeManager.updateRoleState(role, state);

            return nodeManager.getRoleStatus(role);

        } catch (Exception e) {
        	e.printStackTrace();
        	throw e;
        }

    }

    /**
     * Get the status of a particular Role configured on this Node
     */
    @GET
    @Path("/{role}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public NodeRoleStatus getStatus(@PathParam("role") NodeRole role) throws Exception {

        logger.debug("status request received for role " + role );
    	//set up as system scope
    	SecurityUtil.setThreadSystemAccess();

    	return nodeManager.getRoleStatus(role);

    }


	/**
	 * Get a current node status -- primarily to check code version for upgrades
	 */
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getStatusNoStats() throws Exception {


		try {
			logger.warn("status request received for node ");
			//set up as system scope
			SecurityUtil.setThreadSystemAccess();

			List<NodeStatus> ns = nodeManager.getStatus(Instant.ofEpochMilli(0));
			//logger.warn(" got node status " + JSONUtils.objectToString(ns));
			return JSONUtils.objectToString(ns);

		}
		catch (Exception ex) {
			platformMgr.handleException(ex, NodeRole.NODE);
			throw ex;
		}
	}



    /**
     * Get a collection of node statuses including performance info since the timestamp
	 * Goes back one interval so supplied timestamp of Now can be used to get the latest perf
     */
	@GET
	@Path("/status/{ts}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.TEXT_PLAIN)
	public String getStatus(@PathParam("ts") long ts) throws Exception {
		try {
			//set up as system scope

			if (nodeManager.getState() == State.UPGRADING) {
				NodeStatus upg = new NodeStatus();
				upg.setState(State.UPGRADING);
				ArrayList<NodeStatus> upga = new ArrayList<>();
				upga.add(upg);
				return JSONUtils.objectToString(upga);
			}

			SecurityUtil.setThreadSystemAccess();
			List<NodeStatus> ns = nodeManager.getStatus(Instant.ofEpochMilli(ts));

			return JSONUtils.objectToString(ns);
		}
		catch (Exception ex) {
			platformMgr.handleException(ex, NodeRole.NODE);
			throw ex;
		}
	}

	@POST
	@Path("/patch")
	public Response patchNode(String upgrade) throws Exception {

		logger.debug(" patch node request received : " + upgrade);

		PlatformUpgrade u = JSONUtils.objectFromString(upgrade, PlatformUpgrade.class);



		nodeManager.patchNode(u);
		return Response.ok().build();

	}

	/**
	 * Notify Node of a platform update so it can update its local cache
	 */
	@PUT
	@Path("/platform")
	public Response updatePlatform(String platform) throws Exception {

		logger.warn(" caching platform update ");


		//platformMgr.updateCurrent(new Document(platform));

		 nodeManager.cachePlatform(platform);
		//force reset
		platformMgr.setPlatform(null);

		return Response.ok().build();

	}

}
