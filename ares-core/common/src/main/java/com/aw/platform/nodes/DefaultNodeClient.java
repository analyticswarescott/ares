package com.aw.platform.nodes;

import com.aw.common.util.RestResponse;
import com.aw.platform.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.RestClient;
import com.aw.document.Document;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.util.Statics;

import javax.inject.Provider;
import java.time.Instant;
import java.util.List;

/**
 * REST calls for Node Service control
 */
public class DefaultNodeClient extends RestClient implements NodeClient {
 public static final Logger logger = LoggerFactory.getLogger(DefaultNodeClient.class);

    public DefaultNodeClient(PlatformNode remoteNode, Provider<Platform> platformProvider) {
        super(NodeRole.NODE, platformProvider);
		setSpecificNode(remoteNode);

    }

	@Override
	public List<NodeStatus> getNodeStatus(Instant timestamp) throws Exception {
		return getNodeStatusInternal(timestamp);
	}

	@Override
	public NodeStatus getNodeStatus() throws Exception {
		return getNodeStatusInternal(null).get(0);
	}

	private List<NodeStatus> getNodeStatusInternal(Instant timestamp) throws Exception {
		SecurityUtil.setThreadSystemAccess();

		RestResponse resp = null;

		int tries = 0;
		while (true) {
			try {

				long ts = -1;
				if (timestamp != null) {
					ts = timestamp.toEpochMilli();
				}

				resp = execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/node" + "/status/" + Long.toString(ts));

				break;
			} catch (ProcessingException pex) {
				logger.warn(" connection failed...will retry in 2s");
			}

			tries ++;
			if (tries > 5) {
				break;
			}
			Thread.sleep(2000);
	}

		if (resp == null) {
			throw new Exception(" node status failed after 5 2s interval tries");
		}
		String content = resp.payloadToString();

		if (resp.getStatusCode() != 200) {
			throw  new Exception("error response from getNodeStatus " + "on host: " + specificNode.getHost() + ": " + resp.getStatusCode());
		}

		//logger.debug("node status return: " + content);
		List<NodeStatus> nodeStatusList = JSONUtils.listFromString(content, NodeStatus.class);

		return nodeStatusList;

	}

	public void patchNode(PlatformUpgrade upgrade) throws Exception {
		SecurityUtil.setThreadSystemAccess();

		logger.warn("patching node with " + upgrade.toJSON().toString());
		RestResponse resp = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/node/patch", upgrade.toJSON().toString());

		logger.warn(" patch http response received " + resp.payloadToString());
		logger.debug("status was " + resp.getStatusCode());
		if (resp.getStatusCode() != 200) {
			throw  new Exception("error response from node patch request " + "on host: " + specificNode.getHost()
				+ ": " + + resp.getStatusCode());
		}


	}


    public NodeRoleStatus getRoleStatus(NodeRole role) throws Exception{
        SecurityUtil.setThreadSystemAccess();


        RestResponse resp = execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/node/" + role.toString() + "/status");


        if (resp.getStatusCode() != 200) {
            throw  new Exception("error response verifying node role start " + role.toString()+ "on host: " + specificNode.getHost() + ": " + resp);
        }

        NodeRoleStatus rs = JSONUtils.objectFromString(resp.payloadToString(), NodeRoleStatus.class);

        return rs;

/*        int tries = 0;
        while (true) {
            if (rs.getState() == RoleStatus.RoleState.RUNNING) {
                return true;
            }
            tries ++;
            if (tries >= timeout) {
                return false;
            }
        }*/

    }

    public NodeRoleStatus changeRoleState(NodeRole role, State targetState) throws Exception{
        SecurityUtil.setThreadSystemAccess();
        RestResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/" + role.toString() + "/" + targetState.toString());


        if (resp.getStatusCode() != 200) {
            throw  new Exception("error response for setting state to " + targetState +  " node role " + role.toString()+ "on host: " + specificNode.getHost() + ": " + resp);
        }

        NodeRoleStatus rs = JSONUtils.objectFromString(resp.payloadToString(), NodeRoleStatus.class);

        return rs;



    }

	public NodeStatus changeNodeState(NodeStatus.State targetState) throws Exception{

		SecurityUtil.setThreadSystemAccess();

		logger.warn(" about to request node state " + targetState);
		RestResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/" + targetState.toString());


		if (resp.getStatusCode() != 200) {
			throw new Exception("error response for setting state to " + targetState);
		}


		List<NodeStatus> ns = JSONUtils.listFromString( resp.payloadToString(), NodeStatus.class);

		return ns.get(0);



	}


    public void updateNode(PlatformNode node) throws Exception{
        SecurityUtil.setThreadSystemAccess();
        String s = JSONUtils.objectToString(node);
        JSONObject rawNode = new JSONObject(s);
        JSONObject send = new JSONObject();
        send.put("host", node.getHost());
        send.put("node", rawNode);

        RestResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node", send.toString());


            if (resp.getStatusCode() != 200) {
				throw new Exception("error updating node configuration " + "on host: "
					+ specificNode.getHost() + ": " + resp + " : " + resp.payloadToString());
			}


    }

	public void updatePlatformCache(Document platformDoc) throws Exception{
		SecurityUtil.setThreadSystemAccess();

		RestResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/platform", platformDoc.toJSON().toString());


			if (resp.getStatusCode() > 202) {
				throw new Exception("error updating platform cache " + "on host: " + specificNode.getHost() + ": " + resp);
			}


	}
}
