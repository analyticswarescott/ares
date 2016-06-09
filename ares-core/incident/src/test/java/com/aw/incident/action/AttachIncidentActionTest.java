package com.aw.incident.action;

import com.aw.action.ActionContext;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.platform.PlatformMgr;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachIncidentActionTest {

	@Mock
	private ActionContext actionContext;
	@Mock
	private PlatformMgr platformMgr;
	@Mock
	private FileWriter fileWriter;
	@Mock
	private InputStream inputStream;

	private AttachIncidentAction action;

	@Before
	public void setup() throws Exception {
		SecurityUtil.setThreadSystemAccess();
		when(actionContext.getPlatformMgr()).thenReturn(platformMgr);
		when(platformMgr.getTenantFileWriter()).thenReturn(fileWriter);
		action = new AttachIncidentAction(inputStream, "cat picture.jpg", UUID.randomUUID());
	}

/*	@Test
	public void testExecute() throws Exception {
		action.execute(actionContext);
		assertTrue(action.getFilePath().contains(action.getIncidentGuid() + "/"));
		verify(fileWriter).writeStreamToFile(HadoopPurpose.INCIDENT, new Path(Path.SEPARATOR + action.getIncidentGuid()), action.getFilePath().split("/")[2], inputStream);
	}*/

	@SuppressWarnings("all")
	@Test(expected = ActionExecutionException.class)
	public void testExecuteWithException() throws Exception {
		when(platformMgr.getTenantFileWriter()).thenThrow(Exception.class);
		action.execute(actionContext);
	}

/*	@Test
	public void testGetters() {
		assertEquals("cat picture.jpg", action.getFileName());
		assertEquals(AttachIncidentAction.UNITY_TYPE, action.getUnityType());
		assertEquals(IncidentActionType.INCIDENT_ATTACHMENT, action.getType());
	}*/

	@Test
	public void testDefaultConstructor() {
		new AttachIncidentAction();
	}
}
