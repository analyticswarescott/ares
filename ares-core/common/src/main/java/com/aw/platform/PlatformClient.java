package com.aw.platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import com.aw.common.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.monitoring.DefaultPlatformStatus;
import com.aw.platform.monitoring.PlatformStatus;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.restcluster.PlatformController.PlatformState;
import com.aw.platform.restcluster.SparkHandler;
import com.aw.platform.roles.Rest;
import com.aw.util.Statics;
import com.google.common.base.Preconditions;

import javax.inject.Provider;

/**
 * Convenience rest client for platform operations
 *
 *
 *
 */
public class PlatformClient extends RestClient implements SparkHandler {

	public static final String PLATFORM_BASE_PATH = Statics.VERSIONED_REST_PREFIX + "/platform";
	public static final String ADMIN_BASE_PATH = Statics.VERSIONED_REST_PREFIX + "/admin";

	public enum PayloadType {
		BUNDLE,
		SCAN,
	}

	public String getHost() {
		Preconditions.checkNotNull(specificNode, "target host cannot be returned for client, no specific PlatformNode target set");
		return specificNode.getHost();
	}

	public String getHostAndPort() {
		Preconditions.checkNotNull(specificNode, "target host:port cannot be returned for client, no specific PlatformNode target set");
		return specificNode.getHost() + ":" + specificNode.getSettingInt( Rest.PORT);
	}

	static final Logger logger = Logger.getLogger(PlatformClient.class);

	public PlatformClient(PlatformNode node, Provider<Platform> platformProvider) {
		super(node, NodeRole.REST, platformProvider);
	}

	public PlatformClient(Provider<Platform> platform) {
		super(NodeRole.REST, platform);
	}

    public DriverRegistrationResponse registerDriver(JSONObject driverInfo) throws Exception {

        try {

            RestResponse resp = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/admin/register", driverInfo.toString());

            if (resp.getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(" failed to register: status  " + resp.getStatusCode() );
            } else {
				String content = resp.payloadToString();
				DriverRegistrationResponse dr  = JSONUtils.objectFromString(content, DriverRegistrationResponse.class);

				return dr;
            }
        }
        catch (Exception ex) {
            logger.error(" registration error was: " + ex.getMessage(), ex);
            throw ex;
        }

    }
    public void registerProcessor(String driverName, String processorName) throws Exception {

    	//TODO: why are we doing this all over the place? shouldn't be needed everywhere
    	SecurityUtil.setThreadSystemAccess();
		RestResponse resp = execute(HttpMethod.POST, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/admin/register/" + driverName , processorName );


        if (resp.getStatusCode() != 201) {
            throw new InitializationException(" failed to register processor : " + processorName +  ": status  " + resp.getStatusCode() );
        }

    }

	/**
	 * Announce that we're here
	 *
	 * @return Get back our platform node, telling us what we need to configure/start up
	 */
	public String announcePresence() throws Exception {

		RestResponse response = execute(HttpMethod.POST, PLATFORM_BASE_PATH + "/node/" + EnvironmentSettings.getHost());

		String content = response.payloadToString();


		if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {

			throw new PlatformStateException("error announcing presence : " + response.getStatusCode() + " : " + content);
		}

		//build the platform node - because of issues converting a typed map directly to a pojo with jackson, this extra step is needed
/*		//TODO: research jackson serialization to get platform node to convert directly from json using JSONUtils call
		Platform ret = new DefaultPlatform();
		JSONUtils.updateFromString(content, data);
		ret.initialize(null, data);*/
		return content;

	}

	/**
	 * Request a specific platform state. Can be performed against any rest member, the request will be routed to the
	 * leader.
	 *
	 * @param state
	 * @throws Exception
	 */
	public void requestState(PlatformState state) throws Exception {

		RestResponse response = execute(HttpMethod.PUT, ADMIN_BASE_PATH + "/platform/" + state);

		if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {

			String strResponse = response.payloadToString();
			throw new PlatformStateException("error setting platform state to " + state + " : " + response.getStatusCode() + " : " + strResponse);

		}

	}

	public void requestPlatformState(PlatformController.PlatformState state) throws Exception {

		RestResponse response = execute(HttpMethod.PUT, PLATFORM_BASE_PATH + "/" + state);

		if (response.getStatusCode() != HttpStatus.SC_OK) {
			throw new Exception(" Platform state request failed with status "
				+ response.getStatusCode());
		}

	}

	public PlatformState getPlatformState() throws Exception {
		return executeReturnObject(HttpMethod.GET, PLATFORM_BASE_PATH + "/state", PlatformState.class);
	}


	public PlatformStatus getPlatformStatus(Instant lastPoll) throws Exception {
		//TODO: include lastPoll in call
		return executeReturnObject(HttpMethod.GET, PLATFORM_BASE_PATH + "/status/" + lastPoll.toEpochMilli(), DefaultPlatformStatus.class, false);
	}

	/**
	 * Provision a tenant through rest
	 *
	 * @param tenant
	 * @throws Exception
	 */
	public boolean provision(Tenant tenant) throws Exception {

		logger.debug(" calling provision tenant for ID " + tenant.getTenantID());
		RestResponse response = execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/ares/tenants", JSONUtils.objectToString(tenant));
		logger.warn(" done calling provision tenant for ID + " + tenant.getTenantID());
		return HttpStatusUtils.isSuccessful(response.getStatusCode());

	}

	/**
	 * Provision a tenant through rest
	 *
	 * @param tenant
	 * @throws Exception
	 */
	public boolean unProvision(String tenantID) throws Exception {

		logger.warn(" calling unProvision tenant for ID " + tenantID);
		RestResponse response = execute(HttpMethod.DELETE, Statics.VERSIONED_REST_PREFIX + "/ares/tenants/" + tenantID);
		logger.warn(" done calling unProvision tenant for ID  " + tenantID);
		return HttpStatusUtils.isSuccessful(response.getStatusCode());

	}

	/**
	 * Verify tenant existence
	 *
	 * @param tenant
	 * @throws Exception
	 */
	public Boolean tenantExists(Tenant tenant) throws Exception {

		logger.warn(" checking existence of tenant for ID " + tenant.getTenantID());
		RestResponse resp =  execute(HttpMethod.GET, Statics.VERSIONED_REST_PREFIX + "/documents/tenants/exists/" + tenant.getTenantID());
		return Boolean.parseBoolean(resp.payloadToString());

	}


	/**
	 * Log an error to the platform
	 *
	 * @param error
	 * @throws Exception
	 */
	public void logError(Object error, NodeRole source) throws Exception {

		log("/platform/errors?origin=" + source.toString(), error);

	}

	/**
	 * Log an informational message to the platform
	 *
	 * @param message
	 * @throws Exception
	 */
	public void logMessage(Object message) throws Exception {

		log("/platform/logs", message);

	}

	//log an error or message
	private void log(String path, Object payload) throws Exception {

		//convert exception stack trace to string
		if (payload instanceof Exception) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			((Exception)payload).printStackTrace(pw);
			pw.close();
			payload = sw.toString();
		}

		RestResponse response = execute(HttpMethod.POST, com.aw.util.Statics.VERSIONED_REST_PREFIX + path, String.valueOf(payload));
		if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {

			if (payload instanceof Exception) {
				logger.error("error logging to platform", (Exception)payload);
			}

			else {
				logger.error("error logging to platform" + String.valueOf(payload));
			}

			throw new Exception("error logging platform exception to rest: " + response.payloadToString());

		}

	}

	/**
	 * Provision a tenant through rest
	 *
	 * @param tenant
	 * @throws Exception
	 */
	public RestResponse provisionTenant(Tenant tenant) throws Exception {

		logger.warn(" calling provision tenant for ID " + tenant.getTenantID());
		return execute(HttpMethod.POST, Statics.VERSIONED_REST_PREFIX + "/ares/tenants", JSONUtils.objectToString(tenant));
	}

	public void postPayload(String tenantID, PayloadType type, String machineID, String payloadID, HttpEntity entity) throws Exception {

		String path = null;
		switch (type) {
			case BUNDLE: path = "/bundles/"; break;
			case SCAN: path = "/edr_scans/"; break;
		}

		System.out.println("type=" + type + " path=" + path);
		//build the target for the post
		path =   Statics.VERSIONED_REST_PREFIX + "/daw" + tenantID + "/" + machineID + path + payloadID;

		//TODO: replace with logging and add logging config to test framework
		System.out.println("posting bundle\n\turl=" + path + "\n\tmachine=" + machineID + "\n\tid=" + payloadID);

		RestResponse response = execute(HttpMethod.POST, path, entity);

	}

}
