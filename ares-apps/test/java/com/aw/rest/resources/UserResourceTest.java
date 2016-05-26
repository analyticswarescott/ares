package com.aw.rest.resources;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.tenant.Tenant;
import com.aw.user.UserManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

	private UserResource userResource;
	private String userId = "Robin Hood";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private UserManager userManager;
	@Mock
	private PlatformSecurityContext context;

	@Before
	public void setup() throws Exception {
		userResource = new UserResource(userManager);
		AuthenticatedUser user = mock(AuthenticatedUser.class);
		when(context.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId);
		ThreadLocalStore.set(context);
	}

	@Test
	public void testGetUsers() {
		when(context.getTenantID()).thenReturn("2");
		Collection<String> users = userResource.get();
		assertEquals(4, users.size());
		assertTrue(users.contains("joe-2"));
		assertTrue(users.contains("bob-2"));
		assertTrue(users.contains("jane-2"));
		assertTrue(users.contains("jack-2"));
	}

	@Test
	public void testGetTenantsTenant0BadId() throws Exception {
		when(context.getTenantID()).thenReturn("0");
		thrown.expect(ForbiddenException.class);
		userResource.getTenants("Sheriff of Nottingham");
	}

	@Test
	public void testGetTenantsTenantNot0BadId() throws Exception {
		when(context.getTenantID()).thenReturn("2");
		thrown.expect(ForbiddenException.class);
		userResource.getTenants("Prince John");
	}

	@Test
	public void testGetTenantsTenant0NoId() throws Exception {
		when(context.getTenantID()).thenReturn("0");
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("400 Bad Request");
		userResource.getTenants(null);
	}

	@Test
	public void testGetTenantsTenantNot0NoId() throws Exception {
		when(context.getTenantID()).thenReturn("2");
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("400 Bad Request");
		userResource.getTenants(null);
	}

	@Test
	public void testGetTenantsTenant0() throws Exception {
		when(context.getTenantID()).thenReturn("0");
		List<Tenant> fakeTenantList = new ArrayList<>();
		when(userManager.getTenants(userId)).thenReturn(fakeTenantList);
		Collection<Tenant> tenants = userResource.getTenants(userId);
		assertSame(fakeTenantList, tenants);
	}

	@Test
	public void testGetTenantsTenantNot0() throws Exception {
		when(context.getTenantID()).thenReturn("2");
		Collection<Tenant> tenants = userResource.getTenants(userId);
		assertEquals(1, tenants.size());
		assertEquals("2", tenants.iterator().next().getTenantID());
	}
}
