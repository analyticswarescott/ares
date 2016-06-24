package com.aw.common.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Rest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

/**
 * Provides a generic REST client interface in the platform. Allows raw access to a base URL if needed, but if
 * the thread has a Platform available to it, a NodeRole and optional port Setting can be used. In this mode, calls
 * to execute() will try to talk to all known platform nodes with the given role in case of failure. If all nodes fail,
 * an exception will be thrown. If a port setting is not specified, a "PORT" setting will be used if present for that node
 * role. If that is not present, an exception will be thrown.
 *
 *
 *
 */
public class RestClient {

	private static final Logger logger = Logger.getLogger(RestClient.class);

	//default port property if none specified
	private static final String PORT = "PORT";

	/**
	 * Set up for raw rest access via request() - execute() will not work using this constructor, as
	 * role and port must be set.
	 */
	public RestClient() {
		this((NodeRole)null, null, PlatformMgr.getCachedPlatform());
	}


	/**
	 * Call a specific node
	 * @param specificNode
	 * @param role
	 * @param sigChanger
	 */
	public RestClient(PlatformNode specificNode, NodeRole role) {
		this(role, role.settingValueOf(PORT), PlatformMgr.getCachedPlatform());
		this.specificNode = specificNode;
	}

	/**
	 * Rest client for role
	 */
	public RestClient(NodeRole role, Platform platform) {
		this(role, role.settingValueOf(PORT), platform);
	}

	//TODO: this is needed to make calls before platform is known

	public RestClient(NodeRole role, PlatformNode.RoleSetting port, Platform platform) {
		setRole(role);
		setPort(port);
		this.platform = platform;
	}

	/**
	 * (re)initialize with the given platform
	 *
	 * @param platform
	 */
	public void initialize(Platform platform) {
		this.platform = platform;
	}

	/**
	 * Execute a GET. Everything behaves as if you sent a GET method and null payload to execute().
	 *
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public HttpResponse get(String path) throws Exception {
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

		HttpResponse response = get(path);

		//if not successful, we need to throw here
		if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
			throw new ProcessingException("error getting " + path + " : " + response.getStatusLine());
		}

		//else success, return as a string
		return IOUtils.toString(response.getEntity().getContent());

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

		HttpResponse response = execute(HttpMethod.GET, path);

		String content = EntityUtils.toString(response.getEntity());

		if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
			throw new ProcessingException("error during " + method + " rest operation, path=" + path + " type=" + type.getSimpleName() + " status=" + response.getStatusLine());
		}

		try {

			T ret = JSONUtils.objectFromString(content, type, false, epochTime);
			return ret;

		} catch (Exception e) {
			throw new Exception("error returning object from json, path=" + path + " json=" + new JSONObject(content).toString(4), e);
		}


	}

	/**
	 * Execute a request with no payload
	 *
	 * @param method The method
	 * @param path The path
	 * @return The response
	 * @throws Exception if anything goes wrong
	 */
	public HttpResponse execute(HttpMethod method, String path) throws Exception {
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
	public HttpResponse execute(HttpMethod method, String path, InputStream payload) throws Exception {
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
	public HttpResponse execute(HttpMethod method, String path, Object payload) throws Exception {
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
	public HttpResponse execute(HttpMethod method, String path, String payload) throws Exception {
		return execute(method, path, new StringEntity(payload));
	}
	/**
	 * Send a request with a raw byte array specified as the entity payload
	 *
	 * @param method The http method
	 * @param path The REST path
	 * @param payload The entity payload
	 * @return The response
	 * @throws Exception If anything goes wrong
	 */
	public HttpResponse execute(HttpMethod method, String path, byte[] payload) throws Exception {
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
	 * @throws Exception If anything goes wrong
	 */
	public HttpResponse execute(HttpMethod method, String path, HttpEntity payload) throws Exception {
		//shuffle the nodes without modifying incoming list

		//allow a call to a specific node
		List<PlatformNode> nodes = null;

		if (platform == null) {

			Preconditions.checkNotNull(EnvironmentSettings.getFirstNodeConnect(), "no platform and no aw restawrl environment variable, cannot communicate with platform");
			String[] hostPort = EnvironmentSettings.getFirstNodeConnect().split("\\:");
			nodes = Collections.singletonList(new DefaultPlatformNode(hostPort[0], Rest.PORT, Integer.parseInt(hostPort[1])));

		}

		else {

			if (specificNode != null) {

				//logger.warn(" calling specific node " + m_specificNode.getHost());
				nodes = new ArrayList<>();
				nodes.add(specificNode);
			}
			else {
				try {
					nodes = new ArrayList<>(platform.getNodes(m_role));
					Collections.shuffle(nodes);
				} catch (Exception e) {
					throw e;
				}
			}


		}

		Exception lastException = null;
		HttpUriRequest request = null;
		HttpResponse response = null;
		String url = null;
		for (PlatformNode node : nodes) {

			try {

				url = getScheme() + "://" + node.getHost() + ":" + node.getSettingInt(m_port) + path;

				logger.debug(" Rest URL is " + url); //TODO: promoted for ease of cluster debugging -- demote when stable
				//System.out.println(" Rest URL is " + url); //TODO: promoted for ease of cluster debugging -- demote when stable

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
						HttpDelete delete = new HttpDelete(url);
						request = delete;
						break;
					case HEAD:
						HttpHead head = new HttpHead(url);
						request = head;
						break;
					default: throw new UnsupportedOperationException("method " + method + " not supported");
				}

				//add shared secret if applicable
				if (m_role == NodeRole.REST || m_role == NodeRole.NODE) {
					String serviceSharedSecret = EnvironmentSettings.getServiceSharedSecret();
					if ( serviceSharedSecret != null ) {
						request.addHeader("serviceSharedSecret", serviceSharedSecret);
					}
				}

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

		HttpResponse response = null;

		try {

			//null request would mean something went wrong (if throwOnError is false)
			String ret = null;

			//apply authentication to the request
			request = applyAuthentication(request);

			//add content header
	        request.addHeader("Content-Type", "application/json;charset=UTF-8");

	        //execute the request
	        response = execute(request);

	        //get the response content as a string
	        String strResponse = null;
	        if (response.getEntity() != null && response.getEntity().getContent() != null) {
	    		strResponse = IOUtils.toString(response.getEntity().getContent());
	        }

			//figure out error status if any
			int status = response.getStatusLine().getStatusCode();
			if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {

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

		} catch (Exception e) {
			throw new Exception("error communicating with " + request.getURI(), e);
		} finally {

			//make sure response is consumed
			if (response != null) {
				EntityUtils.consume(response.getEntity());
			}

		}

	}

	protected HttpResponse execute(HttpUriRequest request) throws Exception {

		setContentType(request);
		HttpResponse response = getClient().execute(request);
		return response;

	}

	protected void setContentType(HttpUriRequest request) {
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	/**
	 * Modify the request for any specific authentication details. Does nothing by default.
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected HttpUriRequest applyAuthentication(HttpUriRequest request) throws Exception {
		return request;
	}

	/**
	 * @return Our http client for REST operations
	 */
	public HttpClient getClient() { return new DefaultHttpClient(); }

	/**
	 * @return The REST request scheme, defaults to http
	 */
	public String getScheme() { return m_scheme; }
	public void setScheme(String scheme) { m_scheme = scheme; }
	private String m_scheme = "http";

	public NodeRole getRole() { return m_role; }
	public void setRole(NodeRole role) { m_role = role; }
	private NodeRole m_role;

	@JsonIgnore
	public PlatformNode.RoleSetting getPort() { return m_port; }
	public void setPort(PlatformNode.RoleSetting port) { m_port = port; }
	private PlatformNode.RoleSetting m_port;

	/**
	 * If we've requested a specific node to connect to, this will be set
	 */
	@JsonIgnore
	public void setSpecificNode(PlatformNode node) { specificNode = node; }
	protected PlatformNode specificNode = null;

	public void setPlatform(Platform platform) { this.platform = platform; }
	protected Platform platform;

}
