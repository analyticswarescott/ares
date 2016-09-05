package com.aw.unity.es;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.aw.common.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.unity.Data;
import com.aw.util.Statics;
import com.google.common.base.Preconditions;

import javax.inject.Provider;

/**
 * A general use elasticsearch client. Uses the platform to determine how to connect. This client is NOT thread safe.
 * Each thread talking to elasticsearch should have its own client. In general this is good practice anyway, as each
 * client will randomly order the elasticsearch nodes to determine which node to connect to for the next operation.
 *
 *
 */
public class ESClient extends RestClient implements JSONHandler, SecurityAware {

	private static final Logger logger = Logger.getLogger(ESClient.class);

	private static final String UPDATE_PREFIX = "{\"doc\":";
	private static final String UPDATE_SUFFIX = "}";

	public enum Index {
		ERRORS("errors", false),
		INCIDENTS("incidents", true),
		EDR("edr", true),
		DLP("dlp", true);

		Index(String indexName, boolean perTenant) {
			this.indexName = indexName;
			this.perTenant = perTenant;
		}

		/**
		 * @return The index name suffix for this index - the tenant will be prepended to build the final index name
		 */
		public String getIndexName() { return this.indexName; }
		private String indexName;

		/**
		 * @return Whether this index is provisioned per tenant
		 */
		public boolean isPerTenant() { return this.perTenant; }
		private boolean perTenant;

	}

	public static final String SYSTEM_ERROR_INDEX = "errors";
	public static final String INCIDENT_INDEX = "incidents";
	public static final String PERF_STAT_INDEX = "perf_stat";

	public ESClient(Provider<Platform> platform) {
		super(NodeRole.ELASTICSEARCH, platform);
	}

	/**
	 * Equivalent to calling bulkInsert(index, null, data) - with no type override, the elasticsearch type will be set to the unity
	 * data type name.
	 *
	 * @param index The index to bulk insert to
	 * @param data The data to bulk insert
	 * @throws Exception
	 */
	public void bulkInsert(String tenant, String index, Iterable<Data> data) throws Exception {
		bulkInsert(tenant, index, null, data);
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
	public boolean updateFull(String tenant, String index, String type, String id, long previousVersion, String json) throws Exception {

		//just put the document in again, replacing the old document and incrementing the version
		RestResponse resp = execute(HttpMethod.POST, "/" + indexForTenant(tenant, index) + "/" + type + "/" + id + "?version=" + previousVersion , json);
		String response = resp.payloadToString();

		return HttpStatusUtils.isSuccessful(resp.getStatusCode());

	}

	/**
	 * Update the document in elasticsearch
	 *
	 * @param index The index the document is in
	 * @param type The type of document to update
	 * @param docId The document id to update
	 * @param json The json update
	 * @throws Exception If anything goes wrong
	 */
	public void update(String index, String type, String docId, String json) throws Exception {

		RestResponse resp = execute(HttpMethod.POST, "/" + indexForTenant(getTenantID(), index) + "/" + type + "/" + docId + "/_update" , UPDATE_PREFIX + json + UPDATE_SUFFIX);

		String response = resp.payloadToString();

		if (!HttpStatusUtils.isSuccessful(resp.getStatusCode())) {
			throw new Exception("error updating " + type + " : " + resp + " : " + response);
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

		RestResponse resp = execute(HttpMethod.POST, "/" + index + "/" + type + "/" + docId + "/_update" , json);
		String response = resp.payloadToString();
		if (!HttpStatusUtils.isSuccessful(resp.getStatusCode())) {
			throw new Exception("error updating " + type + " : " + resp + " : " + resp.payloadToString());
		}

		//else everything went well

	}

	/**
	 * Performs a bulk insert into the ES cluster.
	 *
	 * @param index The index being loaded
	 * @param typeOverride The type to force all data to - if null, the unity data type will be assumed to be the elasticsearch type
	 * @param data The data to bulk insert
	 * @throws Exception If anything goes wrong
	 */
	public void bulkInsert(String tenant, String index, String typeOverride, Iterable<Data> data) throws Exception {
		//build the bulk insert using a temp file to avoid excessive memory usage
		List<File> chunkies = new ArrayList<File>();
		File tmpFile = null;

		//keep track of streams in case things go wrong, we need to close these before we can delete the file
		PrintWriter out = null;
		BufferedInputStream in = null;

		try {

			tmpFile = File.createTempFile("es_load", ".json");
			chunkies.add(tmpFile);

			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), Statics.UTF_8)));

			//TODO:  replace this with byte counting
			int recs = 0;

			String lastType = "none";
			for (Data cur : data) {
				recs++;

				String type = typeOverride == null ? cur.getType().getName() : typeOverride;
				lastType = type;
				out.println("{ \"index\" : { \"_type\" : \"" + type + "\", \"_id\" : \"" + cur.getGuid() + "\" } }");
				out.println(cur.toJsonString(false, true, true)); //single line json, epoch milli timestamps, only unity fields

				if (recs >= 10000) {
					out.close();
					tmpFile = File.createTempFile("es_load", ".json");
					chunkies.add(tmpFile);
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), Statics.UTF_8)));
					recs = 0;
				}
			}

			//close the output stream now so we can pump it into elasticsearch
			out.close();
			out = null;

			for (File tf : chunkies) {

				logger.debug(" processing file to ES " + tf.getAbsolutePath() + " size is: " + tf.length());
				//post it using our standard charset
				String path = "/" + indexForTenant(tenant, index) + "/_bulk";
				in = new BufferedInputStream(new FileInputStream(tf));
				RestResponse resp = execute(HttpMethod.POST, path, in);

				//close the input stream
				in.close();
				in = null;
				//close the tmp file
				tf.delete();

				//scan for error messages - avoid excessive memory use by streaming
				m_error = null; //reset error from any previous operation
				m_error_ordinal = 0;

				JSONStreamUtils.processArrayElements("items", resp.getStream(), this, null);

				//TODO: maybe log these to hadoop and include a pointer ?
				if (m_error != null) {

					logger.error( " Bulk insert error offending line " + m_error_ordinal + " was " );
					logger.error("Last type processed was " + lastType);

					throw new ProcessingException(" ElasticSearch errors detected on bulk insert, first error: " + m_error);
				}

				logger.info(" inserted " + tmpFile.length() + " bytes to index " + index);

			}
			//tmpFile.delete();



		} catch (Exception e) {

			//log it - this will be lost if the finally blows up TODO: error topic
			logger.error("error writing to elasticsearch", e);
			throw e;

		} finally {

			//always close resources as needed, and delete the tmp file

			try {

				if (out != null) {
					out.close();
				}

				if (in != null) {
					in.close();
				}

			} finally {

				if (tmpFile != null) {
					tmpFile.delete();
				}

			}

		}

	}

	private String indexForTenant(String tenantId, String index) {
		return tenantId + "_" + index;
	}

	private String indexForTenant(String index) {
		return indexForTenant(getTenantID(), index);
	}

	@Override
	protected HttpUriRequest applyAuthentication(HttpUriRequest request) throws Exception {
		//nothing for now
		return request;
	}


	public boolean removeIndexMappings(DocumentHandler docs, PlatformMgr platformMgr) throws Exception{

		List<Document> indices = docs.getDocumentsOfType(DocumentType.CONFIG_INDEX);

		for (Document index : indices) {

			//build the tenant's index name
			String indexName = indexForTenant(index.getName());

			logger.warn("deleting tenant index " + indexName);

			if (indexExists(index.getName())) {

				//apply the index
				RestResponse response = execute(HttpMethod.DELETE, "/" + indexName);

				if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					platformMgr.handleError("failed to delete index " + indexName + " message from es: " + response, NodeRole.REST); //shouldn't happen
					continue;
				}

			}

		}

		return true;
	}

	/**
	 * @param index The index name without any tenant prefix
	 * @return Whether the given index name exists for the current tenant
	 * @throws Exception If any errors occur
	 */
	public boolean indexExists(String index) throws Exception {

		RestResponse response = execute(HttpMethod.HEAD, "/" + indexForTenant(index));

		boolean ret = HttpStatusUtils.isSuccessful(response.getStatusCode());

		return ret;

	}

	/**
	 * Create an elasticsearch index with the given index name - will be created for the current tenant
	 *
	 * @param indexName The raw index name to create, minus any tenant ID
	 * @return Whether the index was created
	 * @throws Exception
	 */
	public boolean applyIndexMappings(String indexName, DocumentHandler docs, Platform platform) throws Exception {
		return applyIndexMappings(docs.getDocument(DocumentType.CONFIG_INDEX, indexName), platform);
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

	private boolean applyIndexMappings(Document indexDoc, Platform platform) throws Exception {

		//build the tenant's index name
		String indexName = indexForTenant(indexDoc.getName());

		logger.info("creating tenant index " + indexName);


		String adjustedMappings = getAdjustedMappings(indexDoc, platform);

		//apply the index
		RestResponse response = execute(HttpMethod.POST, "/" + indexName, adjustedMappings);

		if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {

			//TODO: trying to extract the response content gives closed stream error
			throw new ProcessingException("error applying index mappings, response from elasticsearch: "
					+ response.payloadToString() );

		}

		return true;

	}

	/**
	 * Apply index mappings for the current tenant - this should NOT be called for the system tenant
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean applyIndexMappings(DocumentHandler docs, Platform platform) throws Exception {

		Preconditions.checkState(!getTenantID().equals(Tenant.SYSTEM_TENANT_ID), "method not supported for system tenant");

		List<Document> indices = docs.getDocumentsOfType(DocumentType.CONFIG_INDEX);
		boolean ret = true;

		//add all indexes for the current tenant
		for (Document index : indices) {

			ret &= applyIndexMappings(index, platform);

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

		RestResponse resp = execute(HttpMethod.GET, "/" + index + "/" + type + "/_count");
		JSONObject o = new JSONObject(resp.payloadToString());

		return Long.parseLong(o.getString("count"));

	}

	protected Object getJSONEntity( RestResponse response ) throws Exception {

		String data = response.payloadToString();

		if (!HttpStatusUtils.isSuccessful(response.getStatusCode())) {
			throw new Exception("Unexpected http error: " + response.getStatusCode() + " : " + data);
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
