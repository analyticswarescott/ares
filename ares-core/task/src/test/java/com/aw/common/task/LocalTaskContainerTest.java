package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.SetTimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.google.common.util.concurrent.ListeningExecutorService;

public class LocalTaskContainerTest {

	TaskService service;
	PlatformMgr platformMgr;
	SetTimeSource time;
	DocumentMgr docMgr;
	RestCluster restCluster;

	Task mockTask;
	TaskService mockService;
	TaskDef mockTaskDef;

	@Before
	public void before() throws Exception {

		service = mock(TaskService.class);
		platformMgr = mock(PlatformMgr.class);
		time = new SetTimeSource(Instant.now());
		docMgr = mock(DocumentMgr.class);
		restCluster = mock(RestCluster.class);

		mockTask = mock(Task.class);
		mockTaskDef = mock(TaskDef.class);
		doReturn(Tenant.forId("1")).when(mockTaskDef).getTenant();
		doReturn(mockTask).when(mockTaskDef).newTask();
		doReturn(new TaskStatus()).when(mockTask).getStatus();
		mockService = mock(TaskService.class);

	}

	@Test
	public void testExecute() throws Exception {

		docMgr = mock(DocumentMgr.class);

		RunningTask runningTask = mock(RunningTask.class);
		doReturn(runningTask).when(mockService).getRunningTask(mockTaskDef);
		LocalTaskContainer container = new LocalTaskContainer(new SetTimeSource(Instant.now()), mock(PlatformMgr.class), mockService, docMgr, restCluster);
		ListeningExecutorService mockExecutor = mock(ListeningExecutorService.class);
 		container.setExecutor(mockExecutor);

		UUID guid = container.executeTask(mockTaskDef);

		//make sure we got a uuid
		assertNotNull(guid);

		//make sure we get the task back
		Task task = container.getTask(guid);

		//make sure the task is registered
		assertNotNull(task);

		assertSame(mockTask, task);

		//verify we submitted the task
		verify(mockExecutor).submit(any(TaskCallable.class));

	}

	@Test
	public void testTaskStatus() throws Exception {

		RunningTask runningTask = mock(RunningTask.class);
		doReturn(runningTask).when(mockService).getRunningTask(mockTaskDef);
		LocalTaskContainer container = new LocalTaskContainer(new SetTimeSource(Instant.now()), mock(PlatformMgr.class), mockService, docMgr, restCluster);

		container.executeTask(mockTaskDef);

		List<TaskStatus> taskStatusList = container.getTaskStatus();

		assertEquals(1, taskStatusList.size());

		TaskStatus status = taskStatusList.get(0);
		assertNotNull(status);

	}

	@Test
	public void testLeadership() throws Exception {

		LocalTaskContainer container = new LocalTaskContainer();

		TaskService mockService = mock(TaskService.class);
		TaskController controller = mock(TaskController.class);
		doReturn(controller).when(mockService).newController(container);

		container.executeLeadership(mockService);

		verify(mockService).newController(container);
		verify(controller).execute();

		container.stopLeadership(mockService);

		verify(controller).setRunning(false);

	}

	@Test
	public void test_onComplete() throws Exception {

		LocalTaskContainer container = spy(new LocalTaskContainer(time, platformMgr, service, docMgr, restCluster));

		TaskDef taskDef = new TaskDef();
		Task task = mock(Task.class);
		TaskResult result = new TaskResult(taskDef, task);

		doReturn(true).when(container).isJoined();

		container.onComplete(result);

		//make sure we relayed to the service and logged it
		verify(service).onComplete(result);
		verify(platformMgr).handleLog(any(), any());

	}

	@Test
	public void test_getStatus() throws Exception {

		LocalTaskContainer container = new LocalTaskContainer(time, platformMgr, service, docMgr, restCluster);
		ListeningExecutorService mockExecutor = mock(ListeningExecutorService.class);
		container.setExecutor(mockExecutor);

		TaskDef taskDef = new TaskDef();
		taskDef.setType(new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));
		taskDef.setTenant(Tenant.forId("1"));

		RunningTask runningTask = mock(RunningTask.class);
		doReturn(runningTask).when(service).getRunningTask(taskDef);

		UUID guid = container.executeTask(taskDef);

		TaskStatus status = container.getStatus(guid);

		//make sure we got a status back - content doesn't matter for this unit test
		assertNotNull(status);

	}

	@Test
	public void test_properties() throws Exception {

		LocalTaskContainer container = new LocalTaskContainer(time, platformMgr, service, docMgr, restCluster);
		assertEquals(time.now(), container.now());
		assertEquals(EnvironmentSettings.getHost(), container.getHost());
		assertSame(service, container.getService());

	}

}
