package com.aw.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aw.common.Tag;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.tenant.Tenant;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentException;

/**
 * Interface for secure document operations (CRUD).
 *
 * System methods for internal management are also included, but require system access be present in the SecurityContextImpl in the executing thread.
 */
public interface DocumentHandler {

    /**
     *
     * Creates a new document with a type-unique name (semantic identifier) <br>
     *
     * Integrity checks: <br>
     * A privileged (DG) author may supply a name for content management purposes. <br>
     * Any DocumentType that is marked with a default document name in the DocumentType enum <br>
     *
     * @param doc The document to be created
     * @return DocumentEnvelope
     * @throws Exception
     */
    public DocumentEnvelope createDocument(Document doc) throws Exception;

    /**
     *
     * Method to update Semantic document, first performing integrity checks.  Version will increment while name property remains the same. <br>
     *
     * Integrity checks will ensure that: <br><br>
     *   1. the document author matches the user, otherwise an exception will be thrown <br>
     *   2. the tenant ID of the incoming document matches thread security
     *
     * @param doc
     * @return Document Envelope
     * @throws Exception
     *
     */
    public DocumentEnvelope updateDocument(Document doc) throws Exception;


    /**
     * Retrieve latest version of document by type and semantic name
     *
     * @param docType  DocumentType to be retrieved
     * @param docName Semantic name of document
     * @return Document
     * @throws Exception
     */
    public Document getDocument(DocumentType docType, String docName) throws Exception;

    /**
     * Retrieve a specific instance of a document using its ID.
     *
     *
     * @param docID
     * @return Document
     * @throws Exception
     */
    public Document getDocument(String docID) throws Exception;

    /**
     *
     * Check if semantic document exists
     *
     * @param docType
     * @param docName
     * @return
     * @throws Exception
     */
    public  boolean documentExists(DocumentType docType, String docName) throws Exception;


    /**
     * Check if specific document exists by ID
     *
     * @param docID
     * @return
     * @throws Exception
     */
    public  boolean documentExists(String docID) throws Exception;


    /**
     *
     * @param docType
     * @param docName
     * @return
     * @throws Exception
     */
    public DocumentEnvelope deleteDocument(DocumentType docType, String docName) throws Exception;

    /**
     *
     * @param docType
     * @return
     * @throws Exception
     */
    public List<Document> getDocumentsOfType(DocumentType docType) throws Exception;

    /**
     * Get all documents of a type with the given tags
     *
     * @param docType The document type
     * @param tags The tags that must be associated with the returned documents
     * @return The documents found, if any
     */
    List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags);


    /**
    *
    * @param docType
    * @return
    * @throws Exception
    */
    public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception;

	/**
	 * Allows the retrieval of any documents that match a given type and a collection of tags.
	 * @param docType The doc type to retrieve
	 * @param tags The tags that should match a document's tags
     * @return List of {@link DocumentEnvelope}s matching the type and tags
     */
    List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags);

    /**getBodiesFromType
     *System Method for tenant provisioning
     *
     * TODO: move / remove this?
     *
     * @return
     * @throws Exception
     */
    public Collection<Document> getAllTenants() throws Exception;

    /**
     *
     * System method for tenant provisioning
     *
     * TODO: move / remove this?
     *
     * @param tid
     * @return
     * @throws Exception
     */
    public boolean tenantExists(String tid) throws Exception;

    /**
     * Listen to this document handler for changes
     *
     * @param listener
     */
    public void addListener(DocumentListener listener);

    /**
     * No longer listen to this document handler for changes
     *
     * @param listener
     * @return TODO
     */
    public boolean removeListener(DocumentListener listener);

    /**
     * Initialize the document handler as the handler for the given tenant
     *
     * @param tenant
     */
    public void initForTenant(boolean doBootstrap) throws Exception;

	/**
	 * Get all tags
	 *
	 * @return All tags
	 */
	Collection<Tag> getTags();

    /**
     * Get all tags for a given document type
     *
     * @param documentType The document type
     * @return All tags associated with the document type
     */
	Collection<Tag> getTagsForDocumentType(DocumentType documentType);


	public DocumentTree getDocumentTree(DocumentType documentType);

	/**
	 * Get all documents of a certain type belonging to the group name
	 *
	 * @param type
	 * @param grouping
	 * @return
	 * @throws Exception
	 */
	public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType type, String grouping) throws Exception;

	/**
	 * Delete a group and its children
	 * @param forValue
	 * @param name
	 * @return
	 */
	public DocumentEnvelope deleteGroup(DocumentType forValue, String name) throws Exception;

	/**
	 * Get a collection of body instances for the given document type
	 *
	 * @param docType The type of document whose bodies are needed
	 * @param bodyType The type of body returned
	 * @return The collection of bodies
	 * @throws Exception If anything goes wrong
	 */
	default public <T> Collection<T> getBodiesAsObjects(DocumentType docType, Class<T> bodyType) throws DocumentException {

		Collection<T> ret = new ArrayList<T>();

		try {

			Collection<Document> docs = getDocumentsOfType(docType);
			for (Document doc : docs) {
				ret.add(doc.getBodyAsObject(bodyType));
			}

			return ret;

		} catch (Exception e) {
			throw new DocumentException("error getting bodies for document type " + docType, e);
		}

	}

	/**
	 * apply this exact document as-is to the database
	 *
	 * @param document the document to apply
	 * @throws DocumentException if the apply cannot be performed
	 */
	public void applyDocumentVerbatim(Document document) throws DocumentException;

	/**
	 * whether this doc handler should be in bootstrapping mode, i.e. setting up the first time without all necessary system
	 * services running
	 *
	 * @return whether we're in bootstrapping mode
	 */
	public boolean isBootstrapping();
	public void setBootstrapping(boolean isBootstrapping);

	public void updateListeners(Document doc, Operation op) throws Exception;

	/**
	 * @return the tenant associated with this document handler
	 */
	public Tenant getTenant();
	public void setTenant(Tenant tenant);

	/**
	 * @param dbMgr the database manager
	 */
	public void setDBMgr(DBMgr dbMgr);

}
