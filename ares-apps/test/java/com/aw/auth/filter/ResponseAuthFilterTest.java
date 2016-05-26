package com.aw.auth.filter;

import com.aw.common.rest.security.ThreadLocalStore;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Verifies the functionality of the {@link ResponseAuthFilter} filter.
 */
public class ResponseAuthFilterTest {

	private final ResponseAuthFilter responseAuthFilter = new ResponseAuthFilter();

	private ContainerRequestContext containerRequestContext;
	private ContainerResponseContext containerResponseContext;

	@Before
	public void setUp() throws Exception {
		containerRequestContext = mock(ContainerRequestContext.class);

		containerResponseContext = mock(ContainerResponseContext.class);
		final MultivaluedMap<String, Object> mockHeaders = new MultivaluedHashMap<>();
		when(containerResponseContext.getHeaders()).thenReturn(mockHeaders);
	}

	@Test
	public void testFilter() throws Exception {

		responseAuthFilter.filter(containerRequestContext, containerResponseContext);
		final MultivaluedMap<String, Object> headers = containerResponseContext.getHeaders();

		assertEquals("http://localhost:8080", getHeader(headers, "Access-Control-Allow-Origin"));
		assertEquals("GET, POST, HEAD, DELETE, OPTIONS, PUT, PATCH", getHeader(headers, "Access-Control-Allow-Methods"));
		assertEquals("true", getHeader(headers, "Access-Control-Allow-Credentials"));
		assertEquals("accessToken, Access-Control-Allow-Origin, Authorization, tenantkey, Origin, Content-Type, Content-Range, Content-Disposition, Content-Description", getHeader(headers, "Access-Control-Allow-Headers"));

		// Make sure that unset was called
		assertNull(ThreadLocalStore.get());
	}

	private String getHeader(MultivaluedMap<String, Object> headers, String headerName) {
		return (String) ((List) headers.get(headerName)).get(0);
	}

}