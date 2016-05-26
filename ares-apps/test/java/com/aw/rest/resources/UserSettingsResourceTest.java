package com.aw.rest.resources;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSettingsResourceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	protected Provider<DocumentHandler> documentHandlerProvider;
	@Mock
	protected DocumentHandler documentHandler;

	private UserSettingsResource resource;
	private String user = "flash";

	@Before
	public void setThreadSecurity() {
		resource = new UserSettingsResource(documentHandlerProvider);
		PlatformSecurityContext context = mock(PlatformSecurityContext.class);
		when(documentHandlerProvider.get()).thenReturn(documentHandler);
		when(context.getTenantID()).thenReturn("0");
		when(context.getUser()).thenReturn(new AuthenticatedUser("0", user, user ,""));
		ThreadLocalStore.set(context);
	}

	@Test()
	public void testCheckUserPermission() throws Exception {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("403 Forbidden");
		resource.delete(user);
	}

	@Test()
	public void testCheckUserPermission2() throws Exception {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("403 Forbidden");
		resource.get("reverse flash");
	}

	@Test
	public void getOneTest() throws Exception {
		Document document = new Document();
		when(documentHandler.getDocument(resource.getDocumentType(), user)).thenReturn(document);
		Document returnDocument = resource.get(user);
		Assert.assertSame(document, returnDocument);
	}
}
