package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Provider;

import com.aw.common.system.scope.ResourceScope;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.SetTimeSource;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;

public class TaskServiceTest {

	TaskService service = null;
	Provider<Platform> platform = null;
	Provider<DocumentHandler> docs = null;
	SetTimeSource time;

	TaskContainer mockContainer1;
	TaskContainer mockContainer2;

	DefaultZkAccessor mockZkAccessor = mock(DefaultZkAccessor.class);

	PlatformMgr platformMgr;

	@Before
	public void before() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		mockContainer1 = mock(TaskContainer.class);
		doReturn("host1").when(mockContainer1).getHost();
		doReturn(mock(TaskStatus.class)).when(mockContainer1).getStatus(any(UUID.class));
		mockContainer2 = mock(TaskContainer.class);
		doReturn("host2").when(mockContainer2).getHost();
		doReturn(mock(TaskStatus.class)).when(mockContainer2).getStatus(any(UUID.class));

		platformMgr = mock(PlatformMgr.class);

		//set up test service with mocks
		platform = new TestProvider<>(mock(Platform.class));
		docs = new TestProvider<DocumentHandler>(mock(DocumentHandler.class));
		time = new SetTimeSource(Instant.now());

		service = spy(new TaskService(platformMgr, platform, docs, time));
		doReturn(mockZkAccessor).when(service).getTenantZkAccessor();

		PlatformNode mockNode1 = mock(PlatformNode.class);
		doReturn("host1").when(mockNode1).getHost();
		PlatformNode mockNode2 = mock(PlatformNode.class);
		doReturn("host2").when(mockNode2).getHost();

		doReturn(Arrays.asList(mockContainer1, mockContainer2)).when(service).getContainers();

	}

	@Test
	public void testGetTasks() throws Exception {


		TaskDef taskDef = new TaskDef();
		taskDef.setTenant(Tenant.forId("1"));
		taskDef.setName("test_task");
		taskDef.setScope(ResourceScope.TENANT);
		Document tenantDoc = mock(Document.class);
		doReturn(new JSONObject(JSONUtils.objectToString(Tenant.forId("1")))).when(tenantDoc).getBody();
		doReturn(Collections.singletonList(tenantDoc)).when(docs.get()).getAllTenants();
		doReturn(Collections.singletonList(taskDef)).when(docs.get()).getBodiesAsObjects(DocumentType.TASK_DEF, TaskDef.class);

		Set<TaskDef> defs = service.getTasks();
		assertEquals(1, defs.size());
		assertTrue(defs.contains(taskDef)); //tenant 0 taskDef

	}

	@Test
	public void testGetTaskStatus() throws Exception {

		RunningTask runningTask1 = mock(RunningTask.class);
		doReturn("host1").when(runningTask1).getContainer();
		RunningTask runningTask2 = mock(RunningTask.class);
		doReturn("host3").when(runningTask2).getContainer();

		doReturn(Arrays.asList(runningTask1, runningTask2)).when(service).getRunningTasks();
		doReturn(UUID.randomUUID()).when(runningTask1).getGuid();

		Map<TaskDef, TaskStatus> status = service.getTaskStatus();

		//only found 1
		assertEquals(1, status.size());

		//container 1's task status is the only one in the value set
		assertTrue(status.values().contains(mockContainer1.getStatus(UUID.randomUUID())));

	}

	@Test
	public void testOnComplete_failure() throws Exception {

		TaskDef def = new TaskDef();
		def.setTenant(Tenant.forId("tenant"));
		def.setType(new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));
		def.setName("name");

		Task mockTask = mock(Task.class);

		TaskResult result = new TaskResult(def, mockTask, new Exception("test failure"));

		RunningTask mockRunningTask = new RunningTask();
		mockRunningTask.setState(State.SUCCESSFUL);


		doReturn(mockRunningTask).when(mockZkAccessor).get(any(ZkPurpose.class), anyString(), any(Class.class));
		doNothing().when(mockZkAccessor).put(any(), any(), any());
		doNothing().when(mockZkAccessor).delete(any(), any());

		service.onComplete(result);

		//make sure state was set to failed
		assertEquals(State.FAILED, mockRunningTask.getState());

		String key = service.getKey(def);

		//make sure the running task was put back on failure
		verify(mockZkAccessor).put(ZkPurpose.RUNNING_TASK, key, mockRunningTask);

	}


	@Test
	public void testOnComplete_success() throws Exception {

		TaskDef def = new TaskDef();
		def.setTenant(Tenant.forId("tenant"));
		def.setType(new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));
		def.setName("name");

		Task mockTask = mock(Task.class);

		TaskResult result = new TaskResult(def, mockTask);

		RunningTask mockRunningTask = new RunningTask();
		mockRunningTask.setState(State.SUCCESSFUL);
		doReturn(mockRunningTask).when(mockZkAccessor).get(any(ZkPurpose.class), anyString(), any(Class.class));

		doNothing().when(mockZkAccessor).put(any(), any(), any());
		doNothing().when(mockZkAccessor).delete(any(), any());


		service.onComplete(result);

		//make sure state was set to failed
		assertEquals(State.SUCCESSFUL, mockRunningTask.getState());

		String key = service.getKey(def);

		//make sure the running task was put back on failure
		verify(mockZkAccessor).delete(ZkPurpose.RUNNING_TASK, key);

	}


}
