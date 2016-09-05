package com.aw.common.task;

import java.util.UUID;

import com.aw.common.util.*;
import com.aw.platform.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.cluster.Member;
import com.aw.common.task.exceptions.TaskException;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.util.Statics;

import javax.inject.Provider;

/**
 * provides access to a remote task container, usually from the controller
 *
 *
 *
 */
public class RemoteTaskContainer extends RestClient implements Member, TaskContainer {

	public RemoteTaskContainer(PlatformNode node, Provider<Platform> platformProvider) {
		super(node, NodeRole.REST, platformProvider );
	}

	@Override
	public void cleanup() throws Exception {
	}

	@Override
	public UUID executeTask(TaskDef taskDef) throws Exception {

		RestResponse response = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/tasks", taskDef);

		String strResponse = response.payloadToString();

		if (HttpStatusUtils.isSuccessful(response.getStatusCode())) {
			return JSONUtils.objectFromString(strResponse, UUID.class, false, false);
		} else {
			throw new TaskException("error executing task : " + new JSONObject(strResponse).optString(Statics.ERROR_MESSAGE));
		}

	}

	@Override
	public TaskStatus getStatus(UUID guid) throws TaskException {

		try {

			RestResponse response = execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/tasks/" + guid);

			String strResponse = response.payloadToString();
			if (HttpStatusUtils.isSuccessful(response.getStatusCode())) {
				return JSONUtils.objectFromString(strResponse, TaskStatus.class, false, false);
			}

			else {
				throw new TaskException("error getting status for " + guid + " : " + response.getStatusCode() + " : " + strResponse);
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
