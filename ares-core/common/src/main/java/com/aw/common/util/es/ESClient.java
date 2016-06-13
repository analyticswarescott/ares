package com.aw.common.util.es;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONHandler;
import com.aw.common.util.RestClient;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.google.common.base.Preconditions;

public class ESClient extends RestClient implements JSONHandler, SecurityAware {

	private static final Logger logger = Logger.getLogger(ESClient.class);

	private static final String UPDATE_PREFIX = "{\"doc\":";
	private static final String UPDATE_SUFFIX = "}";

	public ESClient(Platform platform) {
		super(NodeRole.ELASTICSEARCH, platform);
	}

	/**
	 * Updates a document only if the previous version is equal to the provided version, which makes the update an atomic transaction. This is
	 * needed as updates of full documents require multiple operations: get, local modification, put.
	 *
	 * @param index
	 * @param type
	 * @param id
	 * @param previousVersion
	 * @param json
	 * @throws Exception
	 */
	public boolean updateFull(String tenant, ESKnownIndices index, Instant time, String type, String id, long previousVersion, String json) throws Exception {

		//just put the document in again, replacing the old document and incrementing the version
		HttpResponse resp = execute(HttpMethod.POST, "/" + indexForTenant(Tenant.forId(tenant), index, time) + "/" + type + "/" + id + "?version=" + previousVersion , json);
		String response = IOUtils.toString(resp.getEntity().getContent());

		return HttpStatusUtils.isSuccessful(resp.getStatusLine().getStatusCode());

	}

	/**
	 * Update a document with the given json fragment.
	 *
	 * @param index the index to update
	 * @param type the unity type to update
	 * @param docId the guid of the document to update
	 * @param data the json fragment to apply to the document
	 * @param time the time the event (data) occurred
	 * @throws Exception if anything goes wrong
	 */
	public void update(ESKnownIndices index, String type, String docId, String data, Instant time) throws Exception {

		//get the index name
		String indexName = index.buildIndexFor(Tenant.forId(getTenantID()), time);

		//execute the update
		HttpResponse resp = execute(HttpMethod.POST, "/" + indexName + "/" + type + "/" + docId + "/_update" , UPDATE_PREFIX + data + UPDATE_SUFFIX);

		String response = IOUtils.toString(resp.getEntity().getContent());

		if (!HttpStatusUtils.isSuccessful(resp.getStatusLine().getStatusCode())) {
			throw new Exception("error updating " + type + " : " + resp.getStatusLine() + " : " + response);
		}

		//else everything went well

	}

	/**
	 * Update the document in elasticsearch using the json verbatim with no modifications
	 *
	 * @param index The index the document is in
	 * @param type The type of document to update
	 * @param docId The document id to update
	 * @param json The json update
	 * @throws Exception If anything goes wrong
	 */
	public void updateRaw(String index, String type, String docId, String json) throws Exception {

		HttpResponse resp = execute(HttpMethod.POST, "/" + index + "/" + type + "/" + docId + "/_update" , json);
		String response = IOUtils.toString(resp.getEntity().getContent());
		if (!HttpStatusUtils.isSuccessful(resp.getStatusLine().getStatusCode())) {
			throw new Exception("error updating " + type + " : " + resp.getStatusLine() + " : " + IOUtils.toString(resp.getEntity().getContent()));
		}

		//else everything went well

	}

	private String indexForTenant(Tenant tenant, ESKnownIndices index, Instant time) throws ConfigurationException {
		return index.buildIndexFor(tenant, time);
	}

	@Override
	protected HttpUriRequest applyAuthentication(HttpUriRequest request) throws Exception {
		//nothing for now
		return request;
	}

	/**
	 * Remove the given index name from elasticsearch. An exception will be thrown if the current tenant does not
	 * have permission to do this.
	 *
	 * @param index
	 * @param indexName
	 * @throws Exception
	 */
	public void deleteIndex(ESKnownIndices index, String indexName) throws Exception{

		//if system tenant or the tenant owns the index, we can delete it
		if (getTenantID().equals(Tenant.SYSTEM_TENANT_ID) || indexName.startsWith(index.toPrefix(Tenant.forId(getTenantID())))) {

			//delete all indices matching the pattern
			HttpResponse response = execute(HttpMethod.DELETE, "/" + index.toPrefix(Tenant.forId(getTenantID())) + "*");

			//consume the response payload
			EntityUtils.consume(response.getEntity());

			if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
				throw new ConfigurationException("error deleting index " + indexName + " : " + response.getStatusLine());
			}

		}

		//else we can't delete the index, throw an exception explaining that
		else {
			throw new ConfigurationException("no permission to delete index " + indexName + " as tenant " + getTenantID());
		}

	}

	public boolean removeIndexMappings(DocumentHandler docs, PlatformMgr platformMgr) throws Exception{

		List<Document> indices = docs.getDocumentsOfType(DocumentType.CONFIG_INDEX);

		for (Document indexDoc : indices) {

			ESKnownIndices index = ESKnownIndices.valueOf(indexDoc.getName().toUpperCase());

			logger.warn("deleting tenant index " + index);

			//delete all indices matching the pattern
			HttpResponse response = execute(HttpMethod.DELETE, "/" + index.toPrefix(Tenant.forId(getTenantID())) + "*");
			EntityUtils.consume(response.getEntity());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				platformMgr.handleError("failed to delete index " + index + " message from es: " + response.getStatusLine(), NodeRole.REST); //shouldn't happen
				continue;
			}

		}

		return true;
	}

	/**
	 * get all indexes for the given tenant
	 *
	 * @param tenant
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllIndices(Tenant tenant, ESKnownIndices index) throws Exception {

		HttpResponse response = execute(HttpMethod.GET, "/_cat/indices");

		List<String> ret = new ArrayList<String>();
		JSONArray indexArray = new JSONArray(IOUtils.toString(response.getEntity().getContent()));
		for (int x=0; x<indexArray.length(); x++) {

			String strIndex = indexArray.getString(x);
			if (strIndex.startsWith(index.toPrefix(tenant))) {
				ret.add(indexArray.getString(x));
			}

		}

		return ret;

	}

	/**
	 * @param index The index name without any tenant prefix
	 * @return Whether the given index name exists for the current tenant
	 * @throws Exception If any errors occur
	 */
	public boolean indexExists(ESKnownIndices index, Instant time) throws Exception {

		HttpResponse response = execute(HttpMethod.HEAD, "/" + indexForTenant(Tenant.forId(getTenantID()), index, time));

		boolean ret = HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode());

		return ret;

	}

	/**
	 * Create an elasticsearch index with the given index name - will be created for the current tenant
	 *
	 * @param indexName The raw index name to create, minus any tenant ID
	 * @return Whether the index was created
	 * @throws Exception
	 */
	public boolean applyIndexMappings(ESKnownIndices index, DocumentHandler docs, Platform platform, Instant time) throws Exception {
		return applyIndexMappings(docs.getDocument(DocumentType.CONFIG_INDEX, index.name().toLowerCase()), platform, time);
	}

	private String getAdjustedMappings(Document indexDoc, Platform platform) throws Exception {

		//adjust the settings for sharding and replication based on the size of the cluster and (TODO: t-shirt tenant size)

		JSONObject indexDef = indexDoc.getBodyAsObject();

		List<PlatformNode> esNodes =  platform.getNodes(NodeRole.ELASTICSEARCH);
		int nodeCount = esNodes.size();

		int shards = nodeCount; //TODO: re-factor target shard settings for time-based indices, tenant size, etc.

		logger.warn("setting shard count to " + shards + " for index " + indexDoc.getKey());
		JSONObject settings = indexDef.getJSONObject("settings");
		settings.put("number_of_shards", shards);

		return indexDef.toString();


	}

	private boolean applyIndexMappings(Document indexDoc, Platform platform, Instant time) throws Exception {

		//get the index enum value
		ESKnownIndices index = ESKnownIndices.valueOf(indexDoc.getName().toUpperCase());

		//build the tenant's index name
		String indexName = indexForTenant(Tenant.forId(getTenantID()), index, time);

		logger.info("creating tenant index " + indexName);

		String adjustedMappings = getAdjustedMappings(indexDoc, platform);

		//apply the index
		HttpResponse response = execute(HttpMethod.POST, "/" + indexName, adjustedMappings);
		EntityUtils.consume(response.getEntity()); //consume response TODO: do something with the response
		if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {

			//TODO: trying to extract the response content gives closed stream error
			throw new ProcessingException("error applying index mappings, response from elasticsearch: "
					+ response.getStatusLine() );

		}

		return true;

	}

	/**
	 * Apply index mappings for the current tenant - this should NOT be called for the system tenant
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean applyIndexMappings(DocumentHandler docs, Platform platform, Instant time) throws Exception {

		Preconditions.checkState(!getTenantID().equals(Tenant.SYSTEM_TENANT_ID), "method not supported for system tenant");

		List<Document> indices = docs.getDocumentsOfType(DocumentType.CONFIG_INDEX);
		boolean ret = true;

		//add all indexes for the current tenant
		for (Document index : indices) {

			ret &= applyIndexMappings(index, platform, time);

		}

		return ret;

	}

	/**
	 * Update the aliases for the system index to refer to all tenant indexes
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean updateSystemMappings() throws Exception {

		logger.warn("TODO: SLC-854 add / update elasticsearch indexes for system tenant on new tenant");
		return true;

	}

	/**
	 * used to look for errors after bulk insert
	 *
	 * TODO: don't bother with this if error == false at the top
	 */
	@Override
	public void handleJson(String json, long ordinal, Object context) throws Exception {

		//if we haven't found an error yet, check the next result
		if (m_error == null) {

			JSONObject item = new JSONObject(json);

			//check for error
			if (item.has("index")) {
				JSONObject index = item.getJSONObject("index");
				if (!HttpStatusUtils.isSuccessful(index.getInt("status"))) {
					JSONObject error = index.getJSONObject("error");
					m_error = error.getString("reason");
					m_error_ordinal = ordinal;

					//check for cause
					if (error.has("caused_by")) {
						JSONObject cause = error.getJSONObject("caused_by");
						m_error = m_error + " (" + cause.getString("type") + ": " + cause.getString("reason") + ")";
					}
				}
			}

		}

	}

	//TODO: re-work
	public long docCount(String index, String type) throws Exception{

		HttpResponse resp = execute(HttpMethod.GET, "/" + index + "/" + type + "/_count");
		JSONObject o = (JSONObject) getJSONEntity(resp);

		return Long.parseLong(o.getString("count"));

	}

	protected Object getJSONEntity( HttpResponse response ) throws Exception {

		String data = IOUtils.toString(response.getEntity().getContent());

		if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
			throw new Exception("Unexpected http error: " + response.getStatusLine().getStatusCode() + " : " + data);
		}

		try {
			return new JSONObject(data);
		} catch (Exception e) {
			//try array
			return new JSONArray(data);
		}
	}

	//if errors occur, we'll save the first reason for an exception message
	private String m_error = null;
	private long m_error_ordinal = 0;

}
