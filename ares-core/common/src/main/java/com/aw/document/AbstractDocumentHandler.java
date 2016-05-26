package com.aw.document;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.system.scope.ResourceScope;
import com.aw.common.tenant.Tenant;
import com.aw.document.action.Action;
import com.aw.document.action.ActionMirror;
import com.aw.document.action.IAction;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentActionException;
import com.aw.document.exceptions.DocumentNameNotFoundException;
import com.aw.document.exceptions.DocumentSecurityException;
import com.aw.document.security.DocumentPermission;
import com.aw.platform.PlatformMgr;
import com.aw.util.Statics;

/**
 * Class to handle pre-persistence integrity and broker return of results <br>
 *     Contains abstract methods to interact with a persistent store (typically JDBC)
 *
 */
public abstract class AbstractDocumentHandler implements DocumentHandler, SecurityAware {
    public static final Logger logger = Logger.getLogger(AbstractDocumentHandler.class);

    public static final String RETRIEVE_ALL = "full";
    public static final String RETRIEVE_HEADER = "header";
    public static final String RETRIEVE_PAYLOAD = "payload";

    protected boolean archiveEnabled = false;

    private static HashSet<String> _reservedAuthors = new HashSet<String>();

    protected DocumentMgr docMgr;
    protected PlatformMgr platformMgr;

    public static HashSet<String> getReservedAuthors()  {
        return _reservedAuthors;
    }

    public AbstractDocumentHandler(DocumentMgr docMgr, PlatformMgr platformMgr) {

    	this.docMgr = docMgr;
    	this.platformMgr = platformMgr;

        //TODO -- allow setting reserved author IDs
        _reservedAuthors.add(Statics.DG_USER);

    }


    public abstract void reset() throws Exception;

    @Override
    public void initForTenant(boolean doBootstrap) throws Exception {
    	DocUtils.initForTenant(platformMgr, this, doBootstrap);
    }

    /**
     *
     * @param doc
     * @return Inserted Document
     * @throws Exception
     *
     * Method to insert a new Semantic document.  Version will default to 1
     */
    @Override
    public  DocumentEnvelope createDocument(Document doc) throws Exception {

    	//the return value
    	Document ret = null;

    	//set up the document to be inserted, this will check for security issues etc
    	prepareForCreate(doc);

    	//determine if name is being forced for this new document
        boolean forceName = isForcedName(doc);

    	//create the document
    	ret = createDocumentInDB(doc, forceName);

		//call out to perform any relevant action -- doc is now marked with op sequence
		onChange(ret, Operation.CREATE);

		//success
		return (DocumentEnvelope) ret;

    }

    /**
     * whether this new document being added is trying to force a name property
     *
     * @param doc
     * @return
     * @throws Exception
     */
    private boolean isForcedName(Document doc) throws Exception {

    	boolean ret = false;
        if (doc.getName() != null) {

        	//if it contains reserved characters, throw
            if (doc.getName().contains(ActionMirror.MIRROR_FILENAME_DELIMITER)) {
                throw new  RuntimeException("unsafe document name " + doc.getName() + "  contains reserved character " + ActionMirror.MIRROR_FILENAME_DELIMITER);
            }

            //if the document type indicates that users should have a name equal to their username, force it
            if (doc.getDocumentType().getDefaultDocName() != null && doc.getName() == getUserID()) {
                ret = true;
            }

            //if the author is a reserved author, allow name to be set
            else if (_reservedAuthors.contains(doc.getAuthor())) {
                ret = true;
            }

            //else we aren't allowing the name to be forced
            else {
            	throw new DocumentSecurityException("name force not allowed by user "  + getUserID() + " for doc type " + doc.getDocumentType());
            }


        }

        return ret;

    }

    /**
     * prepare the document to be added as a newly created document
     *
     * @param doc the document to be created
     * @throws Exception if there are any problems with the creation process
     */
    protected void prepareForCreate(Document doc) throws Exception {

    	if (!doc.getDocumentType().isInScope(getTenant())) {
			throw new DocumentActionException(" document type " + doc.getDocumentType() + " out of scope for tenant " + getTenantID());
		}

    	Document existing = getExisting(doc);
    	if (existing != null) {
    		throw new DocumentActionException("cannot create a document that already exists ( " + doc.getName() + ")");
    	}

        //set doc ID and Author
        doc.setID(getGUID());

        doc.setTenantID(getTenantID());

        String user = getUserID();
        String author = user;

        //if no author specified, set it
        if (author == null) {
        	author = user;
        }

        //set the author
        doc.setAuthor(author);

        //check author
        if (!author.equals(user) ) {

        	if (_reservedAuthors.contains(doc.getAuthor())) {
                //force name to null, as this is a user save of a "aw" doc
                logger.debug(" setting name to null  " + doc.getAuthor());
                doc.setName(null);
            }
            else {
                //TODO: check for a write permission
                throw new DocumentSecurityException(" create not allowed by user "  + user + " for author " + doc.getAuthor());
            }

        }

        //force Author to userID
        doc.setAuthor(getUserID());


    }

    protected abstract Document createDocumentInDB(Document doc, boolean forceName) throws Exception;

    @Override
    public  DocumentEnvelope updateDocument(Document doc) throws Exception {

    	prepareForUpdate(doc);

		doc =  updateDocumentInDB(doc);

		//let listeners know
		onChange(doc, Operation.UPDATE);

		return doc;

    }

    protected void prepareForUpdate(Document doc) throws Exception {

    	//set doc ID and Author
        doc.setID(getGUID());

        //set tenant ID
        doc.setTenantID(getTenantID());

        //get author for existing envelope, if any - can't trust the author on the incoming document
        Document existing = getExisting(doc);
        String user = getUserID();

        if (existing == null) {
        	throw new DocumentNameNotFoundException("no such " + doc.getDocumentType() + " document: " + doc.getName());
        }

        //if the document was authored by someone else
        if (!existing.getAuthor().equals(user)) {

        	//if this document doesn't allow others to write to it, throw an exception
        	if (Tenant.SYSTEM_TENANT_UID.equals(existing.getAuthor()) || existing.getWritePerm() != DocumentPermission.ALL) {
	            throw new DocumentSecurityException("no write permission for user " + getUserID() + " on doc " + doc.getName() + " with author " + existing.getAuthor());
        	}

        }

        //check tenant
        if (!ObjectUtils.equals(existing.getTenantID(), getTenantID())) {
        	throw new DocumentSecurityException("cannot create new version of document, it was created by a different tenant");
        }

        //let the system doc handler know

        //if everything looks good, set the author of this new version
        doc.setAuthor(user);

    }

	public boolean isBootstrapping() { return bootstrapping; }
	public void setBootstrapping(boolean isBootstrapping) { this.bootstrapping = isBootstrapping; }
	private boolean bootstrapping = false;




	protected abstract Document updateDocumentInDB(Document doc) throws Exception;

    Document getExisting(Document doc) throws Exception {
    	Document existing = null;
        if (documentExists(doc.getDocumentType(), doc.getName())) {
        	existing = getDocument(doc.getDocumentType(), doc.getName());
        }
        return existing;
    }

    /**
     *
     * @param docType
     * @param docName
     * @return Document
     * @throws Exception
     *
     * Method to "delete" a Semantic document.
     */
    public DocumentEnvelope deleteDocument( DocumentType docType, String docName) throws Exception {

        //update the deleted flag and update the document
        Document doc = getDocument(docType, docName);
		doc.setDeleted(true);

		return updateDocument(doc);

    };

    /**
    *
    * @param docType
    * @param docName
    * @return Document
    * @throws Exception
    *
    * Method to "delete" a Semantic document.
    */
    @Override
   public DocumentEnvelope deleteGroup(DocumentType docType, String docName) throws Exception {

       //update the deleted flag and update the document
       Document doc = getDocument(docType, docName);
	   doc.setDeleted(true);

	   Collection<Document> children = this.getDocumentsOfTypeWithGrouping(docType, docName);
	   children.parallelStream().forEach(document -> {
		   try {
			   updateDocument(document);
		   } catch(Exception e) {
			   logger.error(docType.name() + " in " + docName + " could not be deleted");
		   }
	   });
	   return updateDocument(doc);

   };

	/**
	 * Not currently used -- included for future pruning features
	 * @param docType
	 * @param docName
	 * @return
	 * @throws Exception
	 */
    protected abstract Document deleteDocumentFromDBPermanent(DocumentType docType, String docName) throws Exception;

    /**
     * M
     * @param docType
     * @param docName
     * @return Document
     * @throws Exception
     *
     * Return latest version of a Semantic document
     */
    public  Document getDocument( DocumentType docType, String docName) throws Exception {

        Document doc = null;

        //if in scope, just get it
        if (docType.isInScope(getTenant())) {
            doc = getDocumentFromDB(docType, docName);
            doc.setDocHandler(this);
        }

        //else get from system TODO: permissioning of document type visibility
        else if (docType.getScope() == ResourceScope.SYSTEM) {
        	doc = docMgr.getSysDocHandler().getDocument(docType, docName);
        }

        return doc;

    }

    protected abstract Document getDocumentFromDB(DocumentType docType, String docName) throws Exception;

    public abstract boolean documentExists(DocumentType docType, String docName) throws Exception;

	public abstract boolean documentVersionExists(DocumentType docType, String docName, int version) throws Exception;

    public abstract boolean documentExists(String docID) throws Exception;
    /**
     *
     * @param docID
     * @return Document
     * @throws Exception
     *
     * Return a specific document instance
     */
    public abstract Document getDocument(String docID) throws Exception;


	/**
	 * Search-type methods
	 */

    /**
     * Utility methods
     */

    /**
     *
      * @return String
     * Returns a GUID
     */
    private String getGUID() {
        UUID u = UUID.randomUUID();
        return u.toString();
    }

    public boolean tenantExists(String tid) throws Exception {

    	verifySystemAccess();

        Document doc = null;
        try {
             doc = getDocument(DocumentType.TENANT, tid);
        }
        catch (DocumentNameNotFoundException docEx) {
            return false;
        }

        if (doc != null) {return true;}
        else {return false;}

    }


    private void onChange(Document doc, Operation op) throws Exception {

    	//we are the doc handler for this document
    	doc.setDocHandler(this);

    	//update listeners
    	updateListeners(doc, op);

    	//do actions
    	doActions(doc, op);

    }

    /**
     * Notify listeners of a change
     *
     * @param document
     * @param op
     * @throws Exception
     */
    @Override
    public void updateListeners(Document document, Operation op) throws Exception {

    	updateListeners(this, document, op);

    	//if we aren't the system tenant doc handler
    	if (!getTenant().getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {

        	//get the system tenant doc handler
        	DocumentHandler sysDocs = docMgr.getSysDocHandler();

    		//let the system doc handler know about this
    		sysDocs.updateListeners(document, op);

    	}

    }

    private void updateListeners(AbstractDocumentHandler docs, Document document, Operation op) throws Exception {

    	for (DocumentListener listener : docs.listeners) {

    		try {

        		switch (op) {
        			case CREATE: listener.onDocumentCreated(document); break;
        			case DELETE: listener.onDocumentDeleted(document); break;
        			case UPDATE: listener.onDocumentUpdated(document); break;
        		}

    		} catch (Exception e) {
    			//can't let it stop us from letting everyone else know
    			logger.error("error notifying listener of document change", e);
    		}

    	}

    }

    /**
     * Take actions as a result of a change
     *
     * @param doc
     * @param op
     * @throws Exception
     */
    private void doActions(Document doc, Operation op) throws Exception {

		if (bootstrapping) {
			logger.warn(" skipping actions in BOOTSTRAP MODE for doc " + doc.getName()); //TODO: demote
			return;
		}

        for (Action a : Action.values()) {

            if (a.getHandler() == null) {
                continue;  //only try actions with defined handlers
            }


            Class c = null;
            try {
                c = Class.forName(a.getHandler());
            } catch (ClassNotFoundException e) {
                logger.error(" Class not found for document action: " + a.getHandler());
                throw e;
            }
            Constructor cc = c.getConstructor();
            IAction action = (IAction) cc.newInstance();

            //call the action -- it can internally decide to no-op
            action.doAction(platformMgr.getPlatform(), doc, op);

        }
    }

    @Override
    public void addListener(DocumentListener listener) {

    	//don't add multiple times
    	if (!listeners.contains(listener)) {
    		listeners.add(listener);
    	}

    }

    //why is this abstract? can't it be implemented here?
	public abstract Collection<Document> getAllTenantsFromDB() throws Exception;

	public  Collection<Document> getAllTenants() throws Exception {
		return getAllTenantsFromDB();
	}


    @Override
    public boolean removeListener(DocumentListener listener) {
    	return listeners.remove(listener);
    }

    //the listeners
    private List<DocumentListener> listeners = new CopyOnWriteArrayList<>();

    public Tenant getTenant() { return m_tenant; }
	public void setTenant(Tenant tenant) { m_tenant = tenant; }
	private Tenant m_tenant;

	public void setDBMgr(DBMgr dbMgr) { this.dbMgr = dbMgr; }
	protected DBMgr dbMgr;

}
