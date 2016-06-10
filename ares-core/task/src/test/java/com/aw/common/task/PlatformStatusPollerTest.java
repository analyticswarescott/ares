package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import java.util.List;
import java.util.Map;

import com.aw.platform.restcluster.PlatformController;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.messaging.Topic;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.SetTimeSource;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import com.aw.platform.monitoring.DefaultPlatformStatus;
import com.aw.platform.monitoring.StreamStatus;
import com.aw.platform.monitoring.TenantStatus;
import com.aw.platform.monitoring.TopicPartitionStatus;

public class PlatformStatusPollerTest {

	@Before
	public void before() throws Exception {

		mockTaskContext = mock(TaskContext.class);
		mockPlatformMgr = mock(PlatformMgr.class);
		mockClient = mock(PlatformClient.class);
		doReturn(mockClient).when(mockPlatformMgr).newClient();
		setTime = new SetTimeSource(Instant.now());
		doReturn(setTime).when(mockTaskContext).getTimeSource();
		LocalTaskContainer mockContainer = mock(LocalTaskContainer.class);
		doReturn(mockPlatformMgr).when(mockTaskContext).getPlatformMgr();
		doReturn(mockContainer).when(mockTaskContext).getContainer();
		TaskService service = mock(TaskService.class);
		doReturn(service).when(mockTaskContext).getTaskService();

		task = new PlatformStatusPoller();
		task.initialize(mockTaskContext);

	}

	@Test
	public void testPoll() throws Exception {

		//mock platform status
		DefaultPlatformStatus mockStatus = mock(DefaultPlatformStatus.class);
		PlatformController.PlatformState mockState = PlatformController.PlatformState.RUNNING;
		TenantStatus mockTenantStatus = mock(TenantStatus.class);
		StreamStatus mockStreamStatus = mock(StreamStatus.class);
		TopicPartitionStatus mockTopicStatus = mock(TopicPartitionStatus.class);
		Map<Topic, List<TopicPartitionStatus>> map = Collections.singletonMap(Topic.EVENTS_ES, Arrays.asList(mockTopicStatus));
		doReturn(map).when(mockStreamStatus).getTopicStatus();
		doReturn(Collections.singletonList(mockTenantStatus)).when(mockStatus).getTenantStatus();
		doReturn(Collections.singletonList(mockStreamStatus)).when(mockTenantStatus).getStreamStatus();
		doReturn(mockStatus).when(mockClient).getPlatformStatus(any());
		doReturn(mockState).when(mockClient).getPlatformState();
		doReturn(Tenant.forId("1")).when(mockTenantStatus).getTenant();

		task.poll();

		//make sure the status was sent to the messaging framework
		verify(mockPlatformMgr).sendMessage(Topic.TOPIC_STATUS, Tenant.forId("1"), mockTopicStatus);

		//test properties after execute
		assertEquals(1L, task.getPollCount());
		assertEquals(0L, task.getErrorCount());
		assertNotNull(task.getLastStatus());
		assertEquals(setTime.now(), task.getLastStatus());

	}


	@Test
	public void testTaskStatus() throws Exception {

		TaskStatus status = task.getStatus();

		assertEquals(0.0, status.getProgress(), 0.0);
		assertEquals(State.RUNNING, status.getState());

	}

	@Test
	public void test_execute() throws Exception {

		PlatformStatusPoller poller = spy(new PlatformStatusPoller());
		poller.initialize(new DefaultTaskContext(mock(LocalTaskContainer.class), TimeSource.SYSTEM_TIME, mock(TaskDef.class), Tenant.forId("1"), mock(TaskService.class), mock(DocumentMgr.class)));

		//just run the loop once
		poller.setRunning(false);

		doNothing().when(poller).poll();

		//don't wait very long, we're testing
		poller.setPollFrequency(Duration.ofMillis(1));

		poller.execute();

		//make sure we call poll, not much else to check
		verify(poller, times(1)).poll();

	}

	//test data set up before each unit test
	private PlatformStatusPoller task;
	private PlatformMgr mockPlatformMgr;
	private PlatformClient mockClient;
	private TimeSource setTime;
	private TaskContext mockTaskContext;


}
