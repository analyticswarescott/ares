package com.aw.rest.resources;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Test;

import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.Task;
import com.aw.common.task.TaskDef;
import com.aw.common.task.TaskStatus;
import com.aw.common.tenant.Tenant;

public class TaskResourceTest {

	@Test
	public void testGetStatus() throws Exception {

		LocalTaskContainer mockContainer = mock(LocalTaskContainer.class);
		TaskResource resource = new TaskResource(mockContainer);

		resource.getStatus();

		verify(mockContainer).getTaskStatus();

	}

	@Test
	public void testGetStatusGuidException() throws Exception {

		LocalTaskContainer mockContainer = mock(LocalTaskContainer.class);
		TaskResource resource = new TaskResource(mockContainer);

		UUID guid = UUID.randomUUID();
		resource.getStatus(guid);

		verify(mockContainer).getStatus(guid);
	}

	@Test
	public void testGetStatusGuid() throws Exception {

		LocalTaskContainer mockContainer = mock(LocalTaskContainer.class);
		UUID guid = UUID.randomUUID();
		TaskStatus mockStatus = mock(TaskStatus.class);
		doReturn(mockStatus).when(mockContainer).getStatus(guid);
		TaskResource resource = new TaskResource(mockContainer);

		TaskStatus taskStatus = resource.getStatus(guid);

		assertSame(mockStatus, taskStatus);

	}

	@Test
	public void testExecute() throws Exception {

		TaskDef def = new TaskDef();

		LocalTaskContainer mockContainer = mock(LocalTaskContainer.class);
		Task task = mock(Task.class);
		UUID guid = UUID.randomUUID();
		doReturn(task).when(mockContainer).getTask(guid);
		TaskStatus mockStatus = mock(TaskStatus.class);
		doReturn(mockStatus).when(task).getStatus();
		TaskResource resource = new TaskResource(mockContainer);
		Tenant tenant = Tenant.forId("1");

		resource.executeTask(def);

		verify(mockContainer).executeTask(def);

	}
}
