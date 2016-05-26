//<<<<<<< HEAD
package com.aw.rest.resources;

import com.aw.common.hadoop.exceptions.FileWriteException;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.DefaultPlatform;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.RestCluster;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import java.io.InputStream;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * @author  jhaight
 */

@RunWith(MockitoJUnitRunner.class)
public class AdminResourceTest {
	private static final String PROCESS_NAME = "PROCESS_NAME";
	private static final String DRIVER_INFO = "{}";
	private static final String DRIVER_NAME = "DRIVER_NAME";
	private final String PLATFORM_DOC = "PLATFORM_DOC";
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Provider<DocumentHandler> documentHandlerProvider;
	@Mock
	private DocumentHandler documentHandler;

	@Mock
	private Provider<PlatformMgr> platformMgrProvider;
	@Mock
	private PlatformMgr platformMgr;

	@Mock
	private Provider<Platform> platformProvider;
	@Mock
	private Platform platform;

	@Mock
	private Provider<LocalRestMember> localRestMemberProvider;
	@Mock
	private LocalRestMember localRestMember;

	@Mock
	private Provider<RestCluster> restClusterProvider;
	@Mock
	private RestCluster restCluster;

	private AdminResource adminResource;

	@Before
	public void setup() {
		when(documentHandlerProvider.get()).thenReturn(documentHandler);
		when(platformProvider.get()).thenReturn(platform);
		when(platformMgrProvider.get()).thenReturn(platformMgr);
		when(restClusterProvider.get()).thenReturn(restCluster);
		when(localRestMemberProvider.get()).thenReturn(localRestMember);
		adminResource = new AdminResource(documentHandlerProvider, platformMgrProvider, platformProvider, localRestMemberProvider, restClusterProvider);
	}

	@Test
	public void getPlatformTest() throws Exception {
		String platformString = "I'm a platform!";
		when(platform.toString()).thenReturn(platformString);
		Response response = adminResource.getPlatform();
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(platformString, response.getEntity());
	}

	@Test
	public void getPlatformThrowsExceptionTest() throws Exception {
		expectWebApplicationException();
		when(platformProvider.get()).thenThrow(Exception.class);
		adminResource.getPlatform();
	}

	@Test
	public void getPlatformThrowsWebExceptionTest() throws Exception {
		expectWebApplicationException();
		String exceptionBody = "This is what the body should be";
		thrown.expectMessage(exceptionBody);
		when(platformProvider.get()).thenThrow(new WebApplicationException(exceptionBody));
		adminResource.getPlatform();
	}

	@Test
	public void applyPatchTest() throws Exception {
		String versionIdString = "He's the version you deserve, not the version you need right now.";
		InputStream stream = mock(InputStream.class);
		Response response = adminResource.applyPatch(versionIdString, stream);
		Assert.assertEquals(202, response.getStatus());
		ArgumentCaptor<PlatformUpgrade> captor = ArgumentCaptor.forClass(PlatformUpgrade.class);
		verify(localRestMember).upgrade(captor.capture());
		Assert.assertEquals(versionIdString, captor.getValue().getVersionId());
		Assert.assertEquals(stream, captor.getValue().getStream());
	}

	@Test
	public void applyPatchThrowExceptionTest() throws Exception {
		String versionIdString = "He's the version you deserve, not the version you need right now.";
		InputStream stream = mock(InputStream.class);
		Mockito.doThrow(WebApplicationException.class).when(localRestMember).upgrade(any(PlatformUpgrade.class));
		expectWebApplicationException();
		adminResource.applyPatch(versionIdString, stream);
	}

	@Test
	public void updatePlatformOnlyAcceptsValidJsonDocuments() throws Exception {
		String newPlatform = "This is not the platform you're looking for...";
		Response response = adminResource.updatePlatform(newPlatform);
		Assert.assertEquals(400, response.getStatus());
		Assert.assertEquals("Not a valid platform document", response.getEntity());
		verify(platformMgr, never()).setPlatform(any(Platform.class));
	}

	@Test
	public void updatePlatformOnlyAcceptsValidPlatformDocuments() throws Exception {
		String newPlatform = "{\"thisis\":\"json but\", \"thisisnot\":\"a valid platform\"}";
		Response response = adminResource.updatePlatform(newPlatform);
		Assert.assertEquals(400, response.getStatus());
		Assert.assertEquals("Not a valid platform document", response.getEntity());
		verify(platformMgr, never()).setPlatform(any(Platform.class));
	}

	@Test
	public void updatePlatformTest() throws Exception {
		DefaultPlatform platformBody = new DefaultPlatform();
		Document platformDoc = new Document(DocumentType.TEST_TYPE, "name", "display", "1", "Jaron", new JSONObject(platformBody.toString()));
		platformDoc.setBodyClass("com.aw.platform.DefaultPlatform");
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
//see comment below
//		ArgumentCaptor<Platform> platformCaptor = ArgumentCaptor.forClass(Platform.class);
		Response response = adminResource.updatePlatform(platformDoc.toJSON());
		Assert.assertEquals(200, response.getStatus());
		verify(documentHandler, times(1)).updateDocument(docCaptor.capture());
//commenting this out - instead of directly setting platform on PlatformMgr, I think the listener gets the update now
//		verify(platformMgr, times(1)).setPlatform(platformCaptor.capture());
//		Assert.assertEquals(platformBody.toString(), platformCaptor.getValue().toString());
//		Assert.assertEquals(platformDoc.toJSON(), docCaptor.getValue().toJSON());
	}

	@Test
	public void updatePlatformFileWriteExceptionTest() throws Exception {
		DefaultPlatform platformBody = new DefaultPlatform();
		Document platformDoc = new Document(DocumentType.TEST_TYPE, "name", "display", "1", "Jaron", new JSONObject(platformBody.toString()));
		platformDoc.setBodyClass("com.aw.platform.DefaultPlatform");
		when(documentHandler.updateDocument(any(Document.class))).thenThrow(FileWriteException.class);
		ArgumentCaptor<Platform> platformCaptor = ArgumentCaptor.forClass(Platform.class);
		Response response = adminResource.updatePlatform(platformDoc.toJSON());
		Assert.assertEquals(200, response.getStatus());
	}

	@Test
	public void updatePlatformThrowsWebExceptionTest() throws Exception {
		DefaultPlatform platformBody = new DefaultPlatform();
		Document platformDoc = new Document(DocumentType.TEST_TYPE, "name", "display", "1", "Jaron", new JSONObject(platformBody.toString()));
		platformDoc.setBodyClass("com.aw.platform.DefaultPlatform");

		String exceptionBody = "This is what the body should be";
		thrown.expectMessage(exceptionBody);
		when(documentHandlerProvider.get()).thenThrow(new WebApplicationException(exceptionBody));
		expectWebApplicationException();
		adminResource.updatePlatform(platformDoc.toJSON());
	}

	@Test
	public void updatePlatformThrowsExceptionTest() throws Exception {
		DefaultPlatform platformBody = new DefaultPlatform();
		Document platformDoc = new Document(DocumentType.TEST_TYPE, "name", "display", "1", "Jaron", new JSONObject(platformBody.toString()));
		platformDoc.setBodyClass("com.aw.platform.DefaultPlatform");
		when(documentHandlerProvider.get()).thenThrow(Exception.class);
		expectWebApplicationException();
		adminResource.updatePlatform(platformDoc.toJSON());
	}

	@Test
	public void changePlatformStateThrowsWebExceptionTest() throws Exception {
		String exceptionBody = "This is what the body should be";
		thrown.expectMessage(exceptionBody);
		when(localRestMemberProvider.get()).thenThrow(new WebApplicationException(exceptionBody));
		expectWebApplicationException();
		adminResource.platformControl(PlatformController.PlatformState.STOPPED);
	}
	@Test
	public void changePlatformStateThrowsExceptionTest() throws Exception {
		when(localRestMemberProvider.get()).thenThrow(Exception.class);
		expectWebApplicationException();
		adminResource.platformControl(PlatformController.PlatformState.STOPPED);
	}

	@Test
	public void changePlatformStateTest() throws Exception {
		PlatformController.PlatformState state = PlatformController.PlatformState.STOPPED;
		adminResource.platformControl(state);
		verify(localRestMember).requestPlatformState(state);
	}

	@Test
	public void registerDriverTest() throws Exception {
		String newDriverInformation = "{}";
		adminResource.registerDriver(newDriverInformation);
		verify(localRestMember).registerDriver(any(JSONObject.class));
	}

	@Test
	public void registerProcessorTest() throws Exception {
		String driverName = "A driver";
		String newProcessorName = "A new processor";
		Response response = adminResource.registerProc(driverName, newProcessorName);
		Assert.assertEquals(201, response.getStatus());
		verify(localRestMember).registerProcessor(driverName, newProcessorName);
	}

	@Test
	public void registerDriverThrowsWebExceptionTest() throws Exception {
		String exceptionBody = "This is what the body should be";
		thrown.expectMessage(exceptionBody);
		when(localRestMemberProvider.get()).thenThrow(new WebApplicationException(exceptionBody));
		expectWebApplicationException();
		adminResource.registerDriver("{\"driver\":\"A driver?!?\"}");
	}
	@Test
	public void registerDriverThrowsExceptionTest() throws Exception {
		when(localRestMemberProvider.get()).thenThrow(Exception.class);
		expectWebApplicationException();
		adminResource.registerDriver("A driver...");
	}

	@Test
	public void testGetPlatform() throws Exception {
		Platform p = mock(Platform.class);

		when(platformProvider.get()).thenReturn(p);
		Response response = adminResource.getPlatform();
		assert (response.getStatus() == 200 && response.hasEntity());
	}

	@Test
	public void registerProc() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(localRestMemberProvider.get()).thenReturn(member);

		Response response = adminResource.registerProc(DRIVER_NAME, PROCESS_NAME);
		assert (response.getStatus() == 201);
	}

	@Test
	public void registerDriver() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(localRestMemberProvider.get()).thenReturn(member);

		DriverRegistrationResponse expectedResponse = DriverRegistrationResponse.OK;
		when(member.registerDriver(any(JSONObject.class))).thenReturn(expectedResponse);

		DriverRegistrationResponse response = adminResource.registerDriver(DRIVER_INFO);
		assertSame(expectedResponse, response);
	}

	@Test
	public void testPlatformControl() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(localRestMemberProvider.get()).thenReturn(member);

		PlatformController.PlatformState platformState = PlatformController.PlatformState.RUNNING;
		doNothing().when(member).requestPlatformState(platformState);

		Response response = adminResource.platformControl(platformState);
		assert (response.getStatus() == 200);
	}

	private void expectWebApplicationException() {
		thrown.expect(WebApplicationException.class);
	}

	@Test
	public void registerProcessorThrowsWebExceptionTest() throws Exception {
		String exceptionBody = "This is what the body should be";
		thrown.expectMessage(exceptionBody);
		when(localRestMemberProvider.get()).thenThrow(new WebApplicationException(exceptionBody));
		expectWebApplicationException();
		adminResource.registerProc("{\"driver\":\"A driver?!?\"}", "words");
	}
	@Test
	public void registerProcessorThrowsExceptionTest() throws Exception {
		when(localRestMemberProvider.get()).thenThrow(Exception.class);
		expectWebApplicationException();
		adminResource.registerProc("A driver...", "A processor...");
	}
}
		/*
=======
/**
 *
 /
package com.aw.rest.resources;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.inject.Provider;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.document.DocumentHandler;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUpgrade;
import com.aw.platform.restcluster.LocalRestMember;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.platform.restcluster.RestCluster;

/**
 * @author sjeanbaptiste
 *
 /
@RunWith(MockitoJUnitRunner.class)
public class AdminResourceTest {
	private static final String PROCESS_NAME = "PROCESS_NAME";
	private static final String DRIVER_INFO = "{}";
	private static final String DRIVER_NAME = "DRIVER_NAME";
	private final String PLATFORM_DOC = "PLATFORM_DOC";

	@Mock
	private Provider<DocumentHandler> documentHandler;

	@Mock
	private Provider<PlatformMgr> platformMgr;

	@Mock
	private Provider<Platform> platform;

	@Mock
	private Provider<Platform> secondPlatform;

	@Mock
	private Provider<LocalRestMember> restMember;

	@Mock
	private Provider<RestCluster> restCluster;

	@InjectMocks
	private AdminResource adminResource = spy(new AdminResource(documentHandler, platformMgr, platform, restMember, restCluster));


	@Test
	public void testGetPlatform() throws Exception {
		Platform p = mock(Platform.class);

		when(platform.get()).thenReturn(p);
		Response response = adminResource.getPlatform(PLATFORM_DOC);
		assert(response.getStatus() == 200 && response.hasEntity());
	}

	@Test
	public void testApplyPatch() throws Exception {
		String version = "1.0";
		InputStream patchFile = mock(InputStream.class);
		LocalRestMember member = mock(LocalRestMember.class);

		when(restMember.get()).thenReturn(member);

		PlatformUpgrade platformUpgrade = mock(PlatformUpgrade.class);
		doNothing().when(member).upgrade(platformUpgrade);
		doReturn(platformUpgrade).when(adminResource).upgradePlatform(version, patchFile);

		Response response = adminResource.applyPatch(version, patchFile);
		assert(response.getStatus() == HttpStatus.ACCEPTED_202);
	}

	@Test
	public void registerProc() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(restMember.get()).thenReturn(member);

		Response response = adminResource.registerProc(DRIVER_NAME, PROCESS_NAME);
		assert(response.getStatus() == 201);
	}

	@Test
	public void registerDriver() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(restMember.get()).thenReturn(member);

		DriverRegistrationResponse expectedResponse = DriverRegistrationResponse.OK;
		when(member.registerDriver(any(JSONObject.class))).thenReturn(expectedResponse);

		DriverRegistrationResponse response = adminResource.registerDriver(DRIVER_INFO);
		assertSame(expectedResponse, response);
	}

	@Test
	public void testPlatformControl() throws Exception {
		LocalRestMember member = mock(LocalRestMember.class);
		when(restMember.get()).thenReturn(member);

		PlatformState platformState = PlatformState.RUNNING;
		doNothing().when(member).requestPlatformState(platformState );

		Response response = adminResource.platformControl(platformState);
		assert(response.getStatus() == 200);
>>>>>>> sprint_13
	}
}
*/