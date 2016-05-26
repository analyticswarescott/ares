package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.Test;

import com.aw.common.task.TaskStatus.State;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.SetTimeSource;
import com.aw.common.util.es.ESClient;
import com.aw.common.util.es.ElasticIndex;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;

public class TenantPruneTaskTest {

	@Test
	public void testPruning() throws Exception {

		TenantPruneTask task = spy(new TenantPruneTask());
		TaskDef taskDef = new TaskDef();
		Tenant tenant = Tenant.forId("1");
		LocalTaskContainer container = mock(LocalTaskContainer.class);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		doReturn(platformMgr).when(container).getPlatformMgr();

		DocumentMgr docMgr = mock(DocumentMgr.class);
		DocumentHandler docs = mock(DocumentHandler.class);
		doReturn(docs).when(docMgr).getDocHandler();
		doReturn(docs).when(docMgr).getSysDocHandler();
		doReturn(Arrays.asList(tenant)).when(docs).getBodiesAsObjects(DocumentType.TENANT, Tenant.class);

		TaskStatus status = task.getStatus();
		assertEquals(State.PENDING, status.getState());

		task.initialize(new DefaultTaskContext(container, new SetTimeSource(LocalDateTime.of(2016, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)), taskDef, tenant, mock(TaskService.class), docMgr));

		ESClient mockClient = mock(ESClient.class);

		for (ElasticIndex index : ElasticIndex.values()) {
			doReturn(index.getIndices(tenant, LocalDateTime.of(2015, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), LocalDateTime.of(2016, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)))
				.when(mockClient).getAllIndices(tenant, index);
		}

		doReturn(mockClient).when(task).newESClient(any(Platform.class));

		task.execute();

		//make sure pruning was done properly - should prune up to the beginning of december
		for (ElasticIndex index : ElasticIndex.values()) {
			verify(mockClient).deleteIndex(index, index + "_1_2015_01");
			verify(mockClient).deleteIndex(index, index + "_1_2015_15");
			verify(mockClient).deleteIndex(index, index + "_1_2015_48");
			verify(mockClient, times(0)).deleteIndex(index, index + "_1_2015_49");
			verify(mockClient, times(0)).deleteIndex(index, index + "_1_2015_50");
			verify(mockClient, times(0)).deleteIndex(index, index + "_1_2015_51");
		}

		status = task.getStatus();
		assertEquals(State.SUCCESSFUL, status.getState());

	}


}
