package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.JSONUtils;
import com.aw.platform.PlatformNode;
import com.aw.util.Statics;

public class RemoteTaskContainerTest {

	private HttpEntity mockEntity;
	private PlatformNode mockNode;
	private HttpResponse mockResponse;
	private RemoteTaskContainer container;
	private StatusLine mockStatus = mock(StatusLine.class);

	@Before
	public void before() throws Exception {

		//set up mocks
		mockStatus = mock(StatusLine.class);
		mockEntity = mock(HttpEntity.class);
		mockNode = mock(PlatformNode.class);
		mockResponse = mock(HttpResponse.class);
		container = spy(new RemoteTaskContainer(mockNode));
		doReturn(mockEntity).when(mockResponse).getEntity();
		doReturn(mockStatus).when(mockResponse).getStatusLine();

	}

	@Test
	public void test_executeTask() throws Exception {

		TaskDef taskDef = new TaskDef();
		UUID guid = UUID.randomUUID();
		doReturn(new ByteArrayInputStream(("\"" + guid.toString() + "\"").getBytes())).when(mockEntity).getContent();
		doReturn(mockResponse).when(container).execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/tasks", taskDef);
		doReturn(200).when(mockStatus).getStatusCode();

		UUID uuid = container.executeTask(taskDef);

		//make sure it is the same meaning the http call was made as expected
		assertEquals(guid, uuid);

	}

	@Test(expected=TaskException.class)
	public void test_executeTask_fail() throws Exception {

		TaskDef taskDef = new TaskDef();
		UUID guid = UUID.randomUUID();
		doReturn(new ByteArrayInputStream(("{}").getBytes())).when(mockEntity).getContent();
		doReturn(mockResponse).when(container).execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/tasks", taskDef);
		doReturn(500).when(mockStatus).getStatusCode();

		UUID uuid = container.executeTask(taskDef);

		//make sure it is the same meaning the http call was made as expected
		assertEquals(guid, uuid);

	}

	@Test
	public void test_getStatus() throws Exception {

		UUID guid = UUID.randomUUID();
		TaskStatus status = new TaskStatus();
		status.setProgress(.5);
		status.setState(State.RUNNING);
		status.getProperties().put("test_property", 100);
		doReturn(mockResponse).when(container).execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/tasks/" + guid);
		doReturn(new ByteArrayInputStream(JSONUtils.objectToString(status, false, false, false).getBytes())).when(mockEntity).getContent();
		doReturn(200).when(mockStatus).getStatusCode();

		TaskStatus retStatus = container.getStatus(guid);
		assertEquals(status.getProgress(), retStatus.getProgress(), 0.0);

	}

	@Test(expected=TaskException.class)
	public void test_getStatus_fail() throws Exception {

		UUID guid = UUID.randomUUID();
		TaskStatus status = new TaskStatus();
		status.setProgress(.5);
		status.setState(State.RUNNING);
		status.getProperties().put("test_property", 100);
		doReturn(mockResponse).when(container).execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/tasks/" + guid);
		doReturn(new ByteArrayInputStream(JSONUtils.objectToString(status, false, false, false).getBytes())).when(mockEntity).getContent();
		doReturn(500).when(mockStatus).getStatusCode();

		TaskStatus retStatus = container.getStatus(guid);
		assertEquals(status.getProgress(), retStatus.getProgress(), 0.0);

	}

}
