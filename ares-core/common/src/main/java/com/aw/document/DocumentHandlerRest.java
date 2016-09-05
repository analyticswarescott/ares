package com.aw.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aw.common.util.RestResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.Tag;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.RestClient;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentException;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.google.common.base.Preconditions;

import javax.inject.Provider;

/**
 * RESTful implementation for document handler. The idea here is that any node in the platform can connect to a remote
 * document handler for configuration documents. Not all methods are guaranteed to be implemented.
 *
 *
 */
public class DocumentHandlerRest extends RestClient implements SequencedDocumentHandler, TenantAware {

	private static final String DEFAULT_BASE_PATH = com.aw.util.Statics.VERSIONED_REST_PREFIX + "/documents";

	public static final Logger logger = Logger.getLogger(DocumentHandlerRest.class);

	public DocumentHandlerRest(String tenantID, Provider<Platform> platform) {
		super(NodeRole.REST, platform);

		//we are scoped to the tenant ID at the time we were created for the current thread
		setTenant(Tenant.forId(tenantID));

	}

	public DocumentHandlerRest(String tenantID, PlatformNode target, Provider<Platform> platform) {
		super(target, NodeRole.REST, platform);
		setTenant(Tenant.forId(tenantID));
	}

	@Override
	public boolean isBootstrapping() {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	@Override
	public void setBootstrapping(boolean isBootstrapping) {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	@Override
	public void writeOpSequence(DocumentEnvelope doc) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	/**
	 * Not implemented yet
	 *
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	@Override
	public DocumentEnvelope createDocument(Document doc) throws Exception {
		String response = post("/" + doc.getDocumentType().name().toLowerCase() + "/" + doc.getName(), doc.toJSON().toString());
		return new DocumentEnvelope(response);
	}

	@Override
	public String getIdForOpSequence(String opSequenceKey, long opSequence) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	@Override
	public long getOpSequence(String opSequenceKey) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	@Override
	public void updateListeners(Document doc, Operation op) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	/**
	 * Not implemented yet
	 *
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	@Override
	public DocumentEnvelope updateDocument(Document doc) throws Exception {
		String response = put("/" + doc.getDocumentType().name().toLowerCase() + "/" + doc.getName(), doc.toJSON().toString());
		return new DocumentEnvelope(response);
	}

	/**
	 * Get all active documents of a specific type.
	 *
	 * @param docType
	 * @param docName
	 * @return
	 * @throws Exception
	 */
	@Override
	public Document getDocument(DocumentType docType, String docName) throws Exception {
		JSONObject doc = new JSONObject(getString(getBasePath() + "/" + docType.name().toLowerCase() + "/" + docName + "?tenant=" + getTenantID()));
		return new Document(doc);
	}



	/**
	 * Get all active documents of a specific type.
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public Document getDocumentBySequence(String opSequenceKey, long opSequence) throws Exception {
		JSONObject doc = new JSONObject(getString(getBasePath() + "/" + opSequenceKey + "/" + opSequence + "?tenant=" + getTenantID()));
		return new Document(doc);
	}


	/**
	 * Not implemented yet
	 *
	 * @param docID
	 * @return
	 * @throws Exception
	 */
	@Override
	public Document getDocument(String docID) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	/**
	 * Checks for the existence of a document by name.
	 *
	 * @param docType
	 * @param docName
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean documentExists(DocumentType docType, String docName) throws Exception {
		logger.debug(" documentExists invoked");
		//if we get anything back, it's there
		String ret = get("/" + docType.name().toLowerCase() + "/" + docName, "", false);

		logger.debug("  documentExists returned " + ret.length() + " bytes ");
		return ret != null;

	}

	/**
	 * Not implemented yet
	 *
	 * @param docID
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean documentExists(String docID) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	/**
	 * Not implemented yet
	 *
	 * @param docType
	 * @param docName
	 * @return
	 * @throws Exception
	 */
	@Override
	public DocumentEnvelope deleteDocument(DocumentType docType, String docName) throws Exception {
		String response = delete("/" + docType.name().toLowerCase() + "/" + docName);
		return new DocumentEnvelope(response);
	}




	/* (non-Javadoc)
	 * @see com.aw.document.DocumentHandler#deleteGroup(com.aw.document.DocumentType, java.lang.String)
	 */
	@Override
	public DocumentEnvelope deleteGroup(DocumentType documentType, String name) throws Exception {
		String response = delete("/" + documentType.name().toLowerCase() + "/groups/" + name);
		return new DocumentEnvelope(response);
	}


	@Override
	public void applyDocumentVerbatim(Document document) throws DocumentException {
		throw new DocumentException("no implemented in rest document handler");
	}

	/**
	 * Accept a mirrored update
	 *
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	@Override
	public void acceptMirrorUpdate(Document doc, Operation op) throws Exception {
		logger.warn(" about to post mirror for " + op + " on doc-name :" + doc.getKey());
		  post("/mirror/" + op, doc.toJSON().toString());

	}


/*	/**
	 * Apply (bootstrap) a new tenant created on another node
	 *
	 * @param tenantID
	 * @return
	 * @throws Exception
	 *//*
	@Override
	public void mirrorNewTenant(String tenantID) throws Exception {
		logger.warn(" about to post mirror for new tenant " + tenantID);
		post("/mirror" , tenantID);

	}*/



	@Override
	public List<Document> getDocumentsOfType(DocumentType docType) throws Exception {
		return getDocumentsOfTypeWithTags(docType, Collections.emptyList());
	}

	@Override
	public Collection<Tag> getTags() {
		return getTags("/tags/list/all", "Unable to get tags");
	}

	@Override
	public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
		Preconditions.checkArgument(documentType != null, "Document type is required");
		return getTags("/tags/list/" + documentType.name().toLowerCase(), "Unable to get tags for doc type");
	}

	protected Collection<Tag> getTags(String url, String exceptionText) {
		List<Tag> tagList = new ArrayList<>();
		try {
			JSONArray tags = new JSONArray(get(url, "body=true"));
			for (int i = 0; i < tags.length(); i++) {
				String tag = tags.getString(i);
				tagList.add(Tag.valueOf(tag));
			}
		} catch (Exception e) {
			throw new RuntimeException(exceptionText, e);
		}
		return tagList;
	}

	@Override
	public List<Document> getDocumentsOfTypeWithGrouping(DocumentType documentType, String grouping) throws Exception {
		try {
			JSONArray docs = new JSONArray(get("/" + documentType.name().toLowerCase() +"/groups/" + grouping, "body=true"));

			// add all documents to the document list
			List<Document> ret = new ArrayList<>();
			for (int x = 0; x < docs.length(); x++) {
				final Document document = new Document(docs.getJSONObject(x));
				ret.add(document);
			}

			return ret;
		} catch (Exception e) {
			logger.error("Unable to query docs with type and grouping", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public DocumentTree getDocumentTree(DocumentType documentType) {
		try {

			JSONArray typesArray = new JSONArray(get("/" + documentType.name().toLowerCase() + "?tenant=" + getTenantID(), "body=true"));
			JSONArray groupsArray = new JSONArray(get("/" + documentType.DOCUMENT_GROUP.name().toLowerCase() + "?tenant=" + getTenantID(), "body=true"));

			List<DocumentEnvelope> documentEnvelopes = new ArrayList<>();

			for (int i = 0; i < typesArray.length(); i++) {
				DocumentEnvelope documentEnvelope = new DocumentEnvelope(typesArray.getJSONObject(i));
				documentEnvelopes.add(documentEnvelope);
			}

			for (int i = 0; i < groupsArray.length(); i++) {
				DocumentEnvelope documentEnvelope = new DocumentEnvelope(groupsArray.getJSONObject(i));
				documentEnvelopes.add(documentEnvelope);
			}


			DocumentTree documentTree = new DocumentTree(documentType, documentEnvelopes);

			documentTree.buildTree();

			return documentTree;

		} catch (Exception e) {
			throw new RuntimeException("Unable to get grouped documents", e);
		}
	}

	@Override
	public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		try {
			JSONArray docs = new JSONArray(get("/" + docType.name().toLowerCase(), "body=true"));

			//build the list of documents
			List<Document> ret = new ArrayList<>();
			for (int x = 0; x < docs.length(); x++) {
				final Document document = new Document(docs.getJSONObject(x));
				if (isTagMatch(document, tags)) {
					ret.add(document);
				}
			}

			return ret;
		} catch (Exception e) {
			logger.error("Unable to query docs with type and tags", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
		return getEnvelopesOfTypeWithTags(docType, Collections.emptyList());
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {

		try {
			JSONArray envelopes = new JSONArray(get("/" + docType.name().toLowerCase(), "body=false"));

			//build the list of envelopes
			List<DocumentEnvelope> ret = new ArrayList<>();
			for (int x = 0; x < envelopes.length(); x++) {

				final DocumentEnvelope documentEnvelope = new DocumentEnvelope(envelopes.getJSONObject(x));
				if (isTagMatch(documentEnvelope, tags)) {
					ret.add(documentEnvelope);
				}
			}

			return ret;
		} catch (Exception e) {
			logger.error("Unable to get Envelopes matching type and tags", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Makes sure the given document matches all the required tags.
	 * @param documentEnvelope The document to check.
	 * @param tags The tags that must match.
     * @return True if the document has all of the necessary tags.
     */
	private boolean isTagMatch(DocumentEnvelope documentEnvelope, Collection<Tag> tags) {
		return tags == null || tags.isEmpty() || documentEnvelope.getTags().containsAll(tags);
	}

	/**
	 * Not implemented yet
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public Collection<Document> getAllTenants() throws Exception {
		JSONArray ary = new JSONArray(getString(getBasePath() + "/tenants?tenant=" + getTenantID()));
		Collection<Document> ret = new ArrayList<Document>();
		for (int x=0; x<ary.length(); x++) {
			ret.add(new Document(ary.getJSONObject(x)));
		}
		return ret;
	}

	/**
	 * Not implemented yet
	 *
	 * @param tid
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean tenantExists(String tid) throws Exception {
		throw new UnsupportedOperationException("not implemented in REST document handler");
	}

	//with params
	private String get(String path, String params) throws Exception {

		return get(path, params, true);

	}

	private String put(String path, String payload) throws Exception {

		RestResponse response = execute(HttpMethod.PUT, getBasePath() + path + "?tenant=" + getTenantID(), payload);
		return response.payloadToString();

	}

	private String delete(String path) throws Exception {

		RestResponse response = execute(HttpMethod.DELETE, getBasePath() + path + "?tenant=" + getTenantID());
		return response.payloadToString();

	}

	private String post(String path, String payload) throws Exception {

		RestResponse response = execute(HttpMethod.POST, getBasePath() + path + "?tenant=" + getTenantID(), payload);
		return response.payloadToString();

	}

	private String get(String path, String params, boolean throwOnError) throws Exception {

		//set up the query string appropriately
		String separator = ""; //no separator if no params were provided
		if (params != null && params.length() > 0) {
			separator = "&";
		} else {
			params = ""; //make sure it's not null
		}

		return getString(getBasePath() + path + "?" + params + separator + "tenant=" + getTenantID());

	}

	protected HttpUriRequest applyAuthentication(HttpUriRequest request) throws Exception {
        String serviceSharedSecret = EnvironmentSettings.getServiceSharedSecret();
        if ( serviceSharedSecret != null ) {
            request.addHeader("serviceSharedSecret", serviceSharedSecret);
        }

        //TODO: figure out if we throw here - i'll throw for now
        else {
        	throw new Exception("shared secret is null!");
        }

        return request;
	}

	@Override
	public void addListener(DocumentListener listener) {
		logger.warn("DocumentHandlerRest.addListener() not implemented in rest yet");
	}

	@Override
	public boolean removeListener(DocumentListener listener) {
		logger.warn("DocumentHandlerRest.removeListener() not implemented in rest yet");
		return false;
	}

	@Override
	public void initForTenant(boolean doBootstrap) throws Exception {
	}

	/**
	 * @return The base REST path within the rest server for document operations
	 */
	public String getBasePath() { return m_basePath; }
	public void setBasePath(String basePath) { m_basePath = basePath; }
	private String m_basePath = DEFAULT_BASE_PATH;

	/**
	 * @return The scheme for the requests, defaults to "http"
	 */
	public String getScheme() { return m_scheme; }
	public void setScheme(String scheme) { m_scheme = scheme; }
	private String m_scheme = "http";

	/**
	 * @return The tenantID for this doc handler
	 */
	public String getTenantID() { return tenant.getTenantID(); }

	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	public DBMgr getDBMgr() { return this.dbMgr;  }
	public void setDBMgr(DBMgr dbMgr) { this.dbMgr = dbMgr; }
	private DBMgr dbMgr;

}
