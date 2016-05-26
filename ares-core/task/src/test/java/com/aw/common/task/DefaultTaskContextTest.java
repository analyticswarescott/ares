package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Arrays;

import org.junit.Test;

import com.aw.common.tenant.Tenant;
import com.aw.common.util.SetTimeSource;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;

public class DefaultTaskContextTest {

	@Test
	public void test() throws Exception {

		LocalTaskContainer container = new LocalTaskContainer();
		TaskDef taskDef = mock(TaskDef.class);
		doReturn("scalar").when(taskDef).getConfigScalar(any(), any());
		doReturn(Arrays.asList("value1", "value2")).when(taskDef).getConfigVector(any(), any());
		Tenant tenant = Tenant.forId("1");
		TimeSource time = new SetTimeSource(Instant.now());
		TaskService mockService = mock(TaskService.class);

		DocumentMgr docMgr = mock(DocumentMgr.class);

		DefaultTaskContext ctx = new DefaultTaskContext(container, time, taskDef, tenant, mockService, docMgr);
		assertEquals("scalar", ctx.getConfigScalar("test", String.class));
		assertEquals(2, ctx.getConfigVector("test", String.class).size());
		assertEquals("value1", ctx.getConfigVector("test", String.class).get(0));
		assertEquals("value2", ctx.getConfigVector("test", String.class).get(1));

		assertSame(container, ctx.getContainer());
		assertSame(taskDef, ctx.getTaskDef());
		assertSame(tenant, ctx.getTenant());

	}

}
