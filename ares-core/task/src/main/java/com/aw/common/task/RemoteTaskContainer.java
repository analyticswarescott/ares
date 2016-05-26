package com.aw.common.task;

import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.cluster.Member;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.RestClient;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.util.Statics;

/**
 * provides access to a remote task container, usually from the controller
 *
 *
 *
 */
public class RemoteTaskContainer extends RestClient implements Member, TaskContainer {

	public RemoteTaskContainer(PlatformNode node) {
		super(node, NodeRole.REST);
	}

	@Override
	public void cleanup() throws Exception {
	}

	@Override
	public UUID executeTask(TaskDef taskDef) throws Exception {

		HttpResponse response = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/tasks", taskDef);

		String strResponse = IOUtils.toString(response.getEntity().getContent());

		if (HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
			return JSONUtils.objectFromString(strResponse, UUID.class, false, false);
		} else {
			throw new TaskException("error executing task : " + new JSONObject(strResponse).optString(Statics.ERROR_MESSAGE));
		}

	}

	@Override
	public TaskStatus getStatus(UUID guid) throws TaskException {

		try {

			HttpResponse response = execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/tasks/" + guid);

			String strResponse = IOUtils.toString(response.getEntity().getContent());
			if (HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
				return JSONUtils.objectFromString(strResponse, TaskStatus.class, false, false);
			}

			else {
				throw new TaskException("error getting status for " + guid + " : " + response.getStatusLine() + " : " + strResponse);
			}

		} catch (TaskException e) {
			throw e;
		} catch (Exception e) {
			throw new TaskException("error getting tasks status", e);
		}


	}

	@Override
	public String getHost() {
		return specificNode.getHost();
	}

}
