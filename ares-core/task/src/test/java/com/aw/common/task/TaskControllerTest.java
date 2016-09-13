package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.cluster.Member;
import com.aw.common.inject.TestProvider;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.task.TaskSchedule.Type;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskInitializationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.SetTimeSource;
import com.aw.common.util.TimeSource;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.PlatformController.PlatformState;

public class TaskControllerTest {

	private DocumentHandler testDocs;
	private Provider<DocumentHandler> testDocProvider;
	private TaskDef task1;
	private TaskDef task2;
	LocalTaskContainer mockContainer;
	TaskService mockService;
	SetTimeSource setTime;
	PlatformMgr mockPlatformMgr;
	RestCluster mockCluster;

	@Before
	public void before() throws Exception {

		mockCluster = mock(RestCluster.class);

		mockContainer = mock(LocalTaskContainer.class);
		mockService = mock(TaskService.class);
		doReturn(mockService).when(mockContainer).getService();
		doReturn(mockCluster).when(mockContainer).getRestCluster();
		doReturn(Collections.singletonList(mockContainer)).when(mockService).getContainers();

		doReturn(PlatformController.PlatformState.RUNNING).when(mockCluster).getState();

		setTime = new SetTimeSource(Instant.now());

		testDocs = mock(DocumentHandler.class);
		testDocProvider = new TestProvider<DocumentHandler>(testDocs);
		mockPlatformMgr = mock(PlatformMgr.class);

		doReturn(mockPlatformMgr).when(mockContainer).getPlatformMgr();


		doReturn(PlatformState.RUNNING).when(mockCluster).getPlatformState();
		doReturn(PlatformState.RUNNING).when(mockCluster).getState();

		doReturn(mockCluster).when(mockContainer).getRestCluster();

		//set up test sources
		task1 = mock(TaskDef.class);
		task2 = mock(TaskDef.class);
		doReturn(Tenant.forId("1")).when(task1).getTenant();
		doReturn(Tenant.forId("1")).when(task2).getTenant();
		doReturn("task1").when(task1).getName();
		doReturn("task2").when(task2).getName();
		doReturn(TaskSchedule.PERPETUAL).when(task1).getSchedule();
		doReturn(TaskSchedule.PERPETUAL).when(task2).getSchedule();
		doReturn(Instant.MIN).when(task1).getNextExecutionTime(any(TimeSource.class));

		Document task1Doc = mock(Document.class);
		doReturn("task1").when(task1Doc).getName();
		doReturn(task1).when(task1Doc).getBodyAsObject(TaskDef.class);

		Document tenantDoc = mock(Document.class);
		doReturn(new JSONObject(JSONUtils.objectToString(Tenant.forId("1")))).when(tenantDoc).getBody();

		//return a tenant
		doReturn(Collections.singletonList(tenantDoc)).when(testDocs).getAllTenants();

		//return the mock task sources from the mock document handler
		doReturn(Arrays.asList(task1, task2)).when(testDocs).getBodiesAsObjects(DocumentType.TASK_DEF, TaskDef.class);
		doReturn(task1Doc).when(testDocs).getDocument(DocumentType.TASK_DEF, "task1");

		doReturn(new HashSet<>(Arrays.asList(task1, task2))).when(mockService).getTasks();

		//make sure we're set up as system
		SecurityUtil.setThreadSystemAccess();

	}

	@Test
	public void testExecute() throws Exception {

		TestTask task = new TestTask(task1);
		doReturn(task).when(task1).newTask();

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);
		controller.execute(task1);

		verify(mockContainer).executeTask(task1);

	}

	@Test
	public void testTaskLoop() throws Exception {

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);
		controller.setMaxWaitTime(Duration.ofMillis(20L));
		controller.taskLoop();

		//verify the tasks were executed, they should have been based on their next execution time
		verify(mockContainer).executeTask(task1);
		verify(mockContainer).executeTask(task2);

	}

	@Test
	public void testTaskLoop_noExecuteScheduled() throws Exception {

		doReturn(Instant.MAX).when(task1).getNextExecutionTime(any(TimeSource.class));
		doReturn(new TaskSchedule(Type.RECURRING, "0 0 14-6 ? * FRI-MON")).when(task1).getSchedule();
		doReturn(Instant.MIN).when(task2).getNextExecutionTime(any(TimeSource.class));

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);
		controller.setMaxWaitTime(Duration.ofMillis(20L));
		controller.taskLoop();

		//verify the tasks were executed, they should have been based on their next execution time
		verify(mockContainer, times(0)).executeTask(task1);
		verify(mockContainer).executeTask(task2);

	}

	@Test
	public void testTaskLoop_executeScheduled() throws Exception {

		doReturn(setTime.now()).when(task1).getNextExecutionTime(any(TimeSource.class));
		doReturn(new TaskSchedule(Type.RECURRING, "0 0 14-6 ? * FRI-MON")).when(task1).getSchedule();
		doReturn(Instant.MIN).when(task2).getNextExecutionTime(any(TimeSource.class));

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);
		controller.setMaxWaitTime(Duration.ofMillis(20L));
		controller.taskLoop();

		//verify the tasks were executed, they should have been based on their next execution time
		verify(mockContainer, times(1)).executeTask(task1);
		verify(mockContainer).executeTask(task2);

	}

	@Test
	public void testFindContainer() throws Exception {

		Member mockLeader = mock(Member.class);
		doReturn("host3").when(mockLeader).getHost();

		LocalTaskContainer mockContainer1 = mock(LocalTaskContainer.class);
		doReturn(Instant.ofEpochMilli(1000L)).when(mockContainer1).now();
		doReturn("host1").when(mockContainer1).getHost();

		TaskService mockService = mock(TaskService.class);
		doReturn(mockService).when(mockContainer1).getService();

		LocalTaskContainer mockContainer2 = mock(LocalTaskContainer.class);
		doReturn(Arrays.asList(mockContainer)).when(mockService).getContainers();

		TaskController controller = new TaskController(mockContainer, new TestProvider<>(testDocs), setTime);

		TaskContainer container = controller.findContainer(task1);

		assertSame(mockContainer, container);

	}

	@Test
	public void testDocChange() throws Exception {

		TaskController controller = new TaskController();

		Document mockDoc1 = mock(Document.class);
		doReturn(DocumentType.TASK_DEF).when(mockDoc1).getDocumentType();

		controller.onDocumentCreated(mockDoc1);
		controller.onDocumentDeleted(mockDoc1);
		controller.onDocumentUpdated(mockDoc1);

		verify(mockDoc1, times(3)).getDocumentType();

	}

	@Test
	public void testHandleFailed() throws Exception {

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);

		RunningTask failed = new RunningTask("task1", Tenant.forId("1"), mockContainer, new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));
		failed.setState(State.FAILED);

		doReturn(Arrays.asList(failed)).when(mockService).getRunningTasks();

		Map<TaskDef, TaskStatus> mockStatus = new HashMap<TaskDef, TaskStatus>();
		TaskStatus status = new TaskStatus();
		status.setProgress(.5);
		status.setState(State.FAILED);
		status.setTaskDef(task1);
		mockStatus.put(task1, new TaskStatus());

		controller.handleFailed(mockStatus);

		//make sure the failed task was re-executed
		verify(mockContainer).executeTask(task1);

		//make sure the other task wasn't
		verify(mockContainer, times(0)).executeTask(task2);

	}

	@Test
	public void testRemoveRunning() throws Exception {

		TaskController controller = new TaskController(mockContainer, new TestProvider<DocumentHandler>(testDocs), setTime);

		Map<TaskDef, TaskStatus> mockStatus = new HashMap<TaskDef, TaskStatus>();
		TaskStatus status = new TaskStatus();
		status.setProgress(.5);
		status.setState(State.RUNNING);
		status.setTaskDef(task1);
		mockStatus.put(task1, new TaskStatus());

		Set<TaskDef> tasks = new HashSet<>(Arrays.asList(task1, task2));
		controller.removeRunning(tasks, mockStatus);

		assertFalse(tasks.contains(task1));
		assertTrue(tasks.contains(task2));
		assertEquals(1, tasks.size());

	}

	@Test
	public void testRunning() throws Exception {

		TaskController controller = new TaskController();

		controller.setRunning(false);
		assertFalse(controller.isRunning());

		controller.setRunning(true);
		assertTrue(controller.isRunning());

	}

	@Test
	public void test_execute() throws Exception {

		TaskController controller = spy(new TaskController());
		controller.setRunning(false);
		doNothing().when(controller).taskLoop();

		controller.execute();

		verify(controller).taskLoop();

	}

	@Test
	public void test_execute_nullTasks() throws Exception {

		TaskController controller = spy(new TaskController());

		controller.execute((List<TaskDef>)null);

		verify(controller, times(0)).execute(any(TaskDef.class));

	}

	private class TestTask extends AbstractTask {

		private TaskDef taskDef;
		private boolean executed = false;

		public TestTask(TaskDef taskDef) {
			this.taskDef = taskDef;
		}

		@Override
		public TaskStatus getStatus() throws TaskException {

			TaskStatus ret = new TaskStatus();

			ret.setProgress(.5);
			ret.setState(State.RUNNING);
			ret.setStatusMessage("testing 123");
			ret.setTaskDef(taskDef);

			return ret;

		}


		@Override
		public void initialize(TaskContext ctx) throws TaskInitializationException {
		}

		@Override
		public synchronized void execute() throws Exception {
			executed = true;
			notifyAll();
		}

		@Override
		public void stop() {
		}

		@Override
		public void shuttingDown() {
		}

	}

}
