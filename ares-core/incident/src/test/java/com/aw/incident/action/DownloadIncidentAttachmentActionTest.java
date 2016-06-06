package com.aw.incident.action;

import com.aw.action.ActionContext;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.structure.HadoopPurpose;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadIncidentAttachmentActionTest {

	@Mock
	private ActionContext actionContext;
	@Mock
	private PlatformMgr platformMgr;
	@Mock
	private FileReader fileReader;

	private DownloadIncidentAttachmentAction action;
	private AttachIncidentAction attachAction;

	@Before
	public void setup() throws Exception {
		SecurityUtil.setThreadSystemAccess();
		when(actionContext.getPlatformMgr()).thenReturn(platformMgr);
		when(platformMgr.getTenantFileReader()).thenReturn(fileReader);
		action = new DownloadIncidentAttachmentAction(getAttachIncidentAction());
	}

	@Test
	public void testExecute() throws Exception {
		FileWrapper fakeWrapper = mock(FileWrapper.class);
		when(fileReader.read(HadoopPurpose.INCIDENT, new Path("/this_is_a/path/to_the_file"), "filename.ext")).thenReturn(fakeWrapper);
		InputStream fakeStream = mock(InputStream.class);
		when(fakeWrapper.getInputStream()).thenReturn(fakeStream);
		action.execute(actionContext);
		assertEquals(fakeStream, action.getDownloadedFile());
	}

	@Test(expected = ActionExecutionException.class)
	public void testExecuteWithException() throws Exception {
		when(fileReader.read(HadoopPurpose.INCIDENT, new Path("/this_is_a/path/to_the_file"), "filename.ext")).thenThrow(new Exception());
		action.execute(actionContext);
	}

	@Test
	public void testConstructor() {
		new DownloadIncidentAttachmentAction();
	}

	@Test
	public void testGetters() {
		assertEquals(attachAction.getIncidentGuid(), action.getIncidentGuid());
		assertEquals(IncidentActionType.DOWNLOAD_INCIDENT_ATTACHMENT, action.getType());
		assertEquals(DownloadIncidentAttachmentAction.UNITY_TYPE, action.getUnityType());
		assertEquals(attachAction.getGuid().toString(), action.getAttachmentActionGuid());
		//assertEquals(attachAction.getFileName(), action.getActionFileName());
	}

	private AttachIncidentAction getAttachIncidentAction() {
		attachAction = new AttachIncidentAction(null, "cat video.mv", UUID.randomUUID());
		//attachAction.setFilePath("/this_is_a/path/to_the_file/filename.ext");
		return attachAction;
	}
}
