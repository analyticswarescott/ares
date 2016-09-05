package com.aw.common.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Provider;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Rest;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

/**
 * Provides a generic REST client interface in the platform. Allows raw access to a base URL if needed, but if
 * the thread has a Platform available to it, a NodeRole and optional port Setting can be used. In this mode, calls
 * to execute() will try to talk to all known platform nodes with the given role in case of failure. If all nodes fail,
 * an exception will be thrown. If a port setting is not specified, a "PORT" setting will be used if present for that node
 * role. If that is not present, an exception will be thrown.
 *
 * @author jlehmann
 *
 */
@SuppressWarnings("unused")
public class RestClient implements PoolableObjectFactory<DefaultHttpClient> {

	private static final Logger logger = Logger.getLogger(RestClient.class);
	private static final String SHARED_SECRET_HEADER = "serviceSharedSecret";

	//default port property if none specified
	private static final String PORT = "PORT";

	/**
	 * maximum simultaneous rest connections from this rest client
	 */
	private static final int DEFAULT_MAX_ACTIVE = 10;

	private ObjectPool<DefaultHttpClient> clients;

	/**
	 * Set up for raw rest access via request() - execute() will not work using this constructor, as
	 * role and port must be set.
	 */
	public RestClient(Provider<Platform> platform) {
		this((NodeRole)null, null, platform);
	}


	/**
	 * Call a specific node
	 * @param specificNode The node for which this client is being used
	 * @param role The role of the node
	 * @param platform The platform provider, to keep up to date platform information
	 */
	public RestClient(PlatformNode specificNode, NodeRole role, Provider<Platform> platform) {
		this(role, role.settingValueOf(PORT), platform);
		this.specificNode = specificNode;
	}

	/**
	 * Rest client for role
	 */
	public RestClient(NodeRole role, Provider<Platform> platform) {
		this(role, role.settingValueOf(PORT), platform);
	}

	//TODO: this is needed to make calls before platform is known

	public RestClient(NodeRole role, PlatformNode.RoleSetting port, Provider<Platform> platform) {
		clients = new GenericObjectPool<>(this, DEFAULT_MAX_ACTIVE, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 0, DEFAULT_MAX_ACTIVE);
		setRole(role);
		setPort(port);
		this.platform = platform;
	}

	/**
	 * Execute a GET. Everything behaves as if you sent a GET method and null payload to execute().
	 *
	 * @param path The tail end of the URI
	 * @return The response
	 * @throws Exception If anything goes wrong
	 */
	public RestResponse get(String path) throws Exception {
		return execute(HttpMethod.GET, path, (HttpEntity) null);
	}

	/**
	 * Get response as a string. If unsuccessful in performing the GET, an exception will be thrown.
	 *
	 * @param path The path to get
	 * @return The string response payload
	 * @throws Exception If anything goes wrong
	 */
	public String getString(String path) throws Exception {

		try (RestResponse response = get(path)) {

			//if not successful, we need to throw here
			if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {
				throw new ProcessingException("error getting " + path + " : " + response);
			}

			//else success, return as a string
			return response.payloadToString();

		}


	}

	public <T> T executeReturnObject(HttpMethod method, String path, Class<T> type) throws Exception {
		return executeReturnObjectInternal(method, path, type, true);
	}

	public <T> T executeReturnObject(HttpMethod method, String path, Class<T> type, boolean epochTime) throws Exception {
		return executeReturnObjectInternal(method, path, type, epochTime);
	}
	/**
	 * Execute a get and return a deserialized object using the standard platform serialization technique
	 *
	 * @param method The http method
	 * @param path The context path for the rest request
	 * @param type The type to return
	 * @return The type deserialized from the response
	 * @throws Exception If the return code was not successful
	 */
	private <T> T executeReturnObjectInternal(HttpMethod method, String path, Class<T> type, boolean epochTime) throws Exception {

		try (RestResponse response = execute(HttpMethod.GET, path)) {

			String content = response.payloadToString();

			if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {
				throw new ProcessingException("error during " + method + " rest operation, path=" + path + " type=" + type.getSimpleName() + " status=" + response);
			}

			try {
				return JSONUtils.objectFromString(content, type, false, epochTime);

			} catch (Exception e) {
				throw new Exception("error returning object from json, path=" + path + " json=" + new JSONObject(content).toString(4), e);
			}

		}


	}

	/**
	 * Execute a request with no payload
	 *
	 * @param method The method
	 * @param path The path
	 * @return The response
	 * @throws ProcessingException if anything goes wrong
	 */
	public RestResponse execute(HttpMethod method, String path) throws ProcessingException {
		return execute(method, path, (HttpEntity)null);
	}

	/**
	 * Executes a request. Streams the input stream as the content payload of the request
	 *
	 * @param method The method to execute
	 * @param path The path to use
	 * @param payload The content to send, if applicable
	 * @return The response as a result of the execute
	 */
	public RestResponse execute(HttpMethod method, String path, InputStream payload) throws ProcessingException {
		return execute(method, path, new InputStreamEntity(payload, -1L));
	}

	/**
	 * Executes a request. Streams the object as json
	 *
	 * @param method The method to execute
	 * @param path The path to use
	 * @param payload The content to send, if applicable
	 * @return The response as a result of the execute
	 */
	public RestResponse execute(HttpMethod method, String path, Object payload) throws ProcessingException {
		return execute(method, path, JSONUtils.objectToString(payload, false, false, false));
	}

	/**
	 * Executes a request.
	 *
	 * @param method The method to execute
	 * @param path The path to use
	 * @param payload The content to send, if applicable
	 * @return The response as a result of the execute
	 */
	public RestResponse execute(HttpMethod method, String path, String payload) throws ProcessingException {
		try {
			return execute(method, path, new StringEntity(payload));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Send a request with a raw byte array specified as the entity payload
	 *
	 * @param method The http method
	 * @param path The REST path
	 * @param payload The entity payload
	 * @return The response
	 * @throws ProcessingException If anything goes wrong
	 */
	public RestResponse execute(HttpMethod method, String path, byte[] payload) throws ProcessingException {
		return execute(method, path, new ByteArrayEntity(payload));
	}


	/**
	 * Send a request with the entity payload specified. If no entity payload should be sent, the payload
	 * can be null.
	 *
	 * @param method The http method
	 * @param path The REST path
	 * @param payload The entity payload
	 * @return The response
	 * @throws ProcessingException If anything goes wrong
	 */
	public RestResponse execute(HttpMethod method, String path, HttpEntity payload) throws ProcessingException {
		//shuffle the nodes without modifying incoming list

		//allow a call to a specific node
		List<PlatformNode> nodes = getNodes();

		Exception lastException = null;
		HttpUriRequest request;
		RestResponse response = null;
		String url = null;
		for (PlatformNode node : nodes) {

			try {

				url = getScheme() + "://" + node.getHost() + ":" + node.getSettingInt(m_port) + path;

				logger.debug(" Rest URL is " + url); //TODO: promoted for ease of cluster debugging -- demote when stable

				request = generateRequest(url, method, payload);

				request = applyHeaders(request);

				response = execute(request);

				//when we've succeeded in making a call to one of the nodes, stop
				break;

			} catch (Exception e) {
				lastException = e; //don't report until we've tried all
			}

		}

		if (response == null) {
			//we've tried all nodes, give up
			throw new ProcessingException("could not communicate with " + m_role +
				" url=" + url + ", last exception chained", lastException);
		}

		else {
			return response;
		}

	}

	protected String request(HttpUriRequest request, boolean throwOnError) throws Exception {

		try {

			//null request would mean something went wrong (if throwOnError is false)
			String ret;

			//apply authentication to the request
			request = applyAuthentication(request);

			//add content header
			request.addHeader("Content-Type", "application/json;charset=UTF-8");

			//execute the request
			try (RestResponse response = execute(request)) {

				//get the response content as a string
				String strResponse = null;
				if (response.hasContent()) {
					strResponse = response.payloadToString();
				}

				//figure out error status if any
				int status = response.getStatusCode();
				if (!HttpStatusUtils.isSuccessful(status)) {

					if (throwOnError) {
						throw new Exception("error during REST document operation (" + status + "): " + strResponse);
					}

					else {
						logger.error("error during REST document operation (" + status + "): " + strResponse);
						ret = null;
					}

				}

				//if we're good, just return the string
				else {
					ret = strResponse;
				}

				return ret;

			}


		} catch (Exception e) {
			throw new Exception("error communicating with " + request.getURI(), e);
		}

	}

	protected RestResponse execute(HttpUriRequest request) throws Exception {

		setContentType(request);

		//take an available client
		DefaultHttpClient client = clients.borrowObject();

		try {

			HttpResponse response = client.execute(request);
			return new DefaultRestResponse(response, clients, client);

		} catch (Exception e) {
			//on exception return the client
			clients.returnObject(client);
			throw e;
		}

	}

	protected void setContentType(HttpUriRequest request) {
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	/**
	 * Modify the request for any specific authentication details. Does nothing by default.
	 *
	 * @param request The request to authenticate
	 * @return Authenticated request
	 * @throws Exception if anything goes wrong
	 */
	protected HttpUriRequest applyAuthentication(HttpUriRequest request) throws Exception {
		// TODO: This needs to be implemented, or removed
		return request;
	}

	/**
	 * @return The REST request scheme, defaults to http
	 */
	@JsonIgnore
	public String getScheme() { return m_scheme; }
	public void setScheme(String scheme) { m_scheme = scheme; }
	private String m_scheme = "http";

	@JsonIgnore
	public NodeRole getRole() { return m_role; }
	public void setRole(NodeRole role) { m_role = role; }
	private NodeRole m_role;

	@JsonIgnore
	public PlatformNode.RoleSetting getPort() { return m_port; }
	@JsonIgnore
	public void setPort(PlatformNode.RoleSetting port) { m_port = port; }
	private PlatformNode.RoleSetting m_port;

	/**
	 * If we've requested a specific node to connect to, this will be set
	 */
	@JsonIgnore
	public void setSpecificNode(PlatformNode node) { specificNode = node; }
	protected PlatformNode specificNode = null;

	@JsonIgnore
	public void setPlatform(Provider<Platform> platform) { this.platform = platform; }
	protected Provider<Platform> platform;


	private List<PlatformNode> getNodes() {
		List<PlatformNode> nodes;

		if (platform == null || platform.get() == null) {
			Preconditions.checkNotNull(EnvironmentSettings.getFirstNodeConnect(), "no platform and no dg rest url environment variable, cannot communicate with platform");
			String[] hostPort = EnvironmentSettings.getFirstNodeConnect().split(":");
			nodes = Collections.singletonList(new DefaultPlatformNode(hostPort[0], Rest.PORT, Integer.parseInt(hostPort[1])));
		} else {
			if (specificNode != null) {

				nodes = new ArrayList<>();
				nodes.add(specificNode);
			}
			else {
				nodes = new ArrayList<>(platform.get().getNodes(m_role));
				Collections.shuffle(nodes);
			}
		}
		return nodes;
	}

	private HttpUriRequest generateRequest(String url, HttpMethod method, @Nullable HttpEntity payload) {
		HttpUriRequest request;

		switch (method) {
			case GET:
				request = new HttpGet(url);
				break;
			case POST:
				HttpPost post = new HttpPost(url);
				post.setEntity(payload);
				request = post;
				break;
			case PUT:
				HttpPut put = new HttpPut(url);
				put.setEntity(payload);
				request = put;
				break;
			case DELETE:
				request = new HttpDelete(url);
				break;
			case HEAD:
				request = new HttpHead(url);
				break;
			default:
				throw new UnsupportedOperationException("method " + method + " not supported");
		}
		return request;
	}

	private HttpUriRequest applyHeaders(HttpUriRequest request) {
		if (m_role == NodeRole.REST || m_role == NodeRole.NODE) {
			String serviceSharedSecret = EnvironmentSettings.getServiceSharedSecret();
			if ( serviceSharedSecret != null ) {
				request.addHeader(SHARED_SECRET_HEADER, serviceSharedSecret);
			}
		}
		return request;
	}

	@Override
	public void activateObject(DefaultHttpClient obj) throws Exception {
		//no-op
	}

	@Override
	public void destroyObject(DefaultHttpClient obj) throws Exception {
		//no-op
	}

	@Override
	public DefaultHttpClient makeObject() throws Exception {
		return new DefaultHttpClient();
	}

	@Override
	public void passivateObject(DefaultHttpClient obj) throws Exception {
		//no-op
	}

	@Override
	public boolean validateObject(DefaultHttpClient obj) {
		return true;
	}

	/**
	 * @return number of simultaneous clients active at once
	 */
	public int getMaxActiveClients() { return this.maxActiveClients;  }
	public void setMaxActiveClients(int maxActiveClients) { this.maxActiveClients = maxActiveClients; }
	private int maxActiveClients = DEFAULT_MAX_ACTIVE;
}
