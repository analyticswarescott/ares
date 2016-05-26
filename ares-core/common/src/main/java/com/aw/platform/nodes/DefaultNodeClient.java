package com.aw.platform.nodes;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import java.time.Instant;
import java.util.List;

/**
 * REST calls for Node Service control
 */
public class DefaultNodeClient extends RestClient implements NodeClient {
 public static final Logger logger = LoggerFactory.getLogger(DefaultNodeClient.class);

    public DefaultNodeClient(PlatformNode remoteNode) {
        super(remoteNode, NodeRole.NODE);

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

		HttpResponse resp = null;

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
		String content = EntityUtils.toString(resp.getEntity());

		if (resp.getStatusLine().getStatusCode() != 200) {
			throw  new Exception("error response from getNodeStatus " + "on host: " + specificNode.getHost() + ": " + resp.getStatusLine().getStatusCode());
		}

		//logger.debug("node status return: " + content);
		List<NodeStatus> nodeStatusList = JSONUtils.listFromString(content, NodeStatus.class);

		return nodeStatusList;

	}

	public void patchNode(PlatformUpgrade upgrade) throws Exception {
		SecurityUtil.setThreadSystemAccess();

		logger.warn("patching node with " + upgrade.toJSON().toString());
		HttpResponse resp = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/node/patch", upgrade.toJSON().toString());

		logger.warn(" patch http response received " + EntityUtils.toString(resp.getEntity()));
		logger.debug("status was " + resp.getStatusLine().getStatusCode());
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw  new Exception("error response from node patch request " + "on host: " + specificNode.getHost()
				+ ": " + + resp.getStatusLine().getStatusCode());
		}

		EntityUtils.consume(resp.getEntity());
	}


    public NodeRoleStatus getRoleStatus(NodeRole role) throws Exception{
        SecurityUtil.setThreadSystemAccess();


        HttpResponse resp = execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/node/" + role.toString() + "/status");


        if (resp.getStatusLine().getStatusCode() != 200) {
            throw  new Exception("error response verifying node role start " + role.toString()+ "on host: " + specificNode.getHost() + ": " + resp);
        }

        HttpEntity en = resp.getEntity();
        NodeRoleStatus rs = JSONUtils.objectFromString(EntityUtils.toString(en), NodeRoleStatus.class);

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
        HttpResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/" + role.toString() + "/" + targetState.toString());


        if (resp.getStatusLine().getStatusCode() != 200) {
            throw  new Exception("error response for setting state to " + targetState +  " node role " + role.toString()+ "on host: " + specificNode.getHost() + ": " + resp);
        }

        HttpEntity en = resp.getEntity();
        NodeRoleStatus rs = JSONUtils.objectFromString( EntityUtils.toString(en), NodeRoleStatus.class);

        return rs;



    }

	public NodeStatus changeNodeState(NodeStatus.State targetState) throws Exception{

		SecurityUtil.setThreadSystemAccess();

		logger.warn(" about to request node state " + targetState);
		HttpResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/"  + targetState.toString());


		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new Exception("error response for setting state to " + targetState);
		}

		HttpEntity en = resp.getEntity();
		List<NodeStatus> ns = JSONUtils.listFromString( EntityUtils.toString(en), NodeStatus.class);

		return ns.get(0);



	}


    public void updateNode(PlatformNode node) throws Exception{
        SecurityUtil.setThreadSystemAccess();
        String s = JSONUtils.objectToString(node);
        JSONObject rawNode = new JSONObject(s);
        JSONObject send = new JSONObject();
        send.put("host", node.getHost());
        send.put("node", rawNode);

        HttpResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node", send.toString());

        try {
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new Exception("error updating node configuration " + "on host: " + specificNode.getHost() + ": " + resp + " : " + IOUtils.toString(resp.getEntity().getContent()));
            }
        }
        finally {
            EntityUtils.consume(resp.getEntity());
        }

    }

	public void updatePlatformCache(Document platformDoc) throws Exception{
		SecurityUtil.setThreadSystemAccess();

		HttpResponse resp = execute(HttpMethod.PUT, Statics.VERSIONED_REST_PREFIX + "/node/platform", platformDoc.toJSON().toString());


		logger.debug(" platform cache response status " + resp.getStatusLine().getStatusCode());
		try {
			if (resp.getStatusLine().getStatusCode() > 202) {
				throw new Exception("error updating platform cache " + "on host: " + specificNode.getHost() + ": " + resp);
			}
		}
		finally {
			EntityUtils.consume(resp.getEntity());
		}

	}
}
