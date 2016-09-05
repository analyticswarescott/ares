package com.aw.incident;

import com.aw.common.util.RestResponse;
import com.aw.common.util.es.ESKnownIndices;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.VersionedObject;
import com.aw.platform.Platform;
import com.aw.unity.DataType;
import com.aw.unity.UnityInstance;
import com.aw.unity.es.UnityESClient;

import javax.inject.Provider;

/**
 * This incident specific class provides apis needed within spark while processing incidents. It should not be required once
 * spark 1.5.2 and elasticsearch 2.0 library issues are resolved so that we can import elasticsearch 2.0 client libraries into
 * spark.
 *
 *
 *
 */
public class IncidentESClient extends UnityESClient implements TenantAware {

	public IncidentESClient(Provider<Platform> platform) {
		super(platform);
	}

	public VersionedObject<Incident> getIncident(UnityInstance unity, String guid, int maxRetries, long delay) throws Exception {
		return getIncident(unity.getDataType(Incident.UNITY_TYPE), guid, maxRetries, delay);
	}

	/**
	 * Get an incident from elasticsearch by guid - this is currently needed because elasticsearch 2.0 libraries don't work in
	 * spark 1.5.2 due to a library conflict. Once this is fixed, we can use the pojo call in UnityUtil instead.
	 *
	 * This implementation will retry a maximum of N times, waiting delay millis between tries in the case of a 404. We allow for this
	 * possibility because data may just not be visible in elasticsearch yet.
	 *
	 * @param type
	 * @param guid
	 * @return
	 */
	public VersionedObject<Incident> getIncident(DataType type, String guid, int maxRetries, long delay) throws Exception {

		//search indexes that apply
		RestResponse response = get("/_cat/indices/" + ESKnownIndices.INCIDENTS.toPrefix(Tenant.forId(getTenantID())) + "*");

		JSONArray array = new JSONArray(response.payloadToString());

		//order by most recent first

		//get the json
		int retry = 0;
		while (true) {

			if (retry > maxRetries) {
				throw new ProcessingException("maximum retries reached ( " + maxRetries + "), could not find incident " + guid);
			}

			//try each index each time
			for (int x=0; x<array.length(); x++) {

				response = get("/" + array.getJSONObject(x).getString("index") + "/" + type.getName() + "/" + guid);

				String data = response.payloadToString();

				//if we hit max retries, throw
				//try again if we get a 404
				if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					Thread.sleep(delay);
				}

				//anything other than 404 is an immediate error
				else if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {
					throw new ProcessingException("error getting incident from elasticsearch: " + response.toString());
				}

				else {
					JSONObject json = new JSONObject(data);
					Incident ret = JSONUtils.objectFromString(json.getJSONObject("_source").toString(), Incident.class);
					return new VersionedObject<>(json.getInt("_version"), ret);
				}

			}

			//next try
			retry++;

		}

	}

}
