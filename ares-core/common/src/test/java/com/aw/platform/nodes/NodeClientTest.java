package com.aw.platform.nodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.aw.common.inject.TestProvider;
import com.aw.common.util.RestResponse;
import com.aw.platform.Platform;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.util.HttpMethod;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.roles.Rest;

public class NodeClientTest {

	private final PlatformNode platformNode = mock(PlatformNode.class);

	private final DefaultNodeClient nodeClient = spy(new DefaultNodeClient(platformNode, new TestProvider<Platform>(
		mock(Platform.class))));

	@Before
	public void setUp() throws Exception {
		nodeClient.setPort(Rest.PORT);
		nodeClient.setRole(NodeRole.REST);
	}

	@Test
    public void getRoleStatus() throws Exception {

		final String jsonData = "{\"state\": \"RUNNING\"}";

		// Set up mocks
		final RestResponse response = mock(RestResponse.class);
		when(response.payloadToString()).thenReturn(jsonData);
		final StatusLine statusLine = mock(StatusLine.class);
		when(response.getStatusCode()).thenReturn(200);
		doReturn(response).when(nodeClient).execute(any(HttpMethod.class), anyString());

        NodeRoleStatus roleStatus = nodeClient.getRoleStatus(NodeRole.REST);
        assertNotNull(roleStatus);
		assertEquals(State.RUNNING, roleStatus.getState());
    }

    @Test(expected=Exception.class)
    public void getRoleStatusException() throws Exception {

		// Set up mocks
		final RestResponse response = mock(RestResponse.class);

		when(response.getStatusCode()).thenReturn(500);
		doReturn(response).when(nodeClient).execute(any(HttpMethod.class), anyString());

        nodeClient.getRoleStatus(NodeRole.REST);
    }

    @Test
    public void changeRoleState() throws Exception {

		final String jsonData = "{\"state\": \"RUNNING\"}";

		// Set up mocks
		final RestResponse response = mock(RestResponse.class);
		when(response.payloadToString()).thenReturn( jsonData);

		when(response.getStatusCode()).thenReturn(200);
		doReturn(response).when(nodeClient).execute(any(HttpMethod.class), anyString());

        NodeRoleStatus roleStatus = nodeClient.changeRoleState(NodeRole.REST, State.RUNNING);
        assertNotNull(roleStatus);
        assertEquals(State.RUNNING, roleStatus.getState());
    }

    @Test(expected=Exception.class)
    public void changeRoleStateFail() throws Exception {
		// Set up mocks
		final HttpResponse response = mock(HttpResponse.class);
		final StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(500);
		when(response.getStatusLine()).thenReturn(statusLine);
		doReturn(response).when(nodeClient).execute(any(HttpMethod.class), anyString());

		nodeClient.changeRoleState(NodeRole.REST, State.RUNNING);
    }

}
