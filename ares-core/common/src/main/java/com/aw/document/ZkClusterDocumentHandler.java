package com.aw.document;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aw.common.Tag;
import com.aw.common.cluster.db.CuratorLockWrapper;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.tenant.Tenant;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentException;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.restcluster.RestCluster;

/**
 * A document handler cluster coordinated with the help of zookeeper
 *
 *
 *
 */
public class ZkClusterDocumentHandler implements SequencedDocumentHandler {

	static final Logger logger = Logger.getLogger(ZkClusterDocumentHandler.class);

	private Map<String, Long> m_cachedOpSequences = new HashMap<>();

	SequencedDocumentHandler wrapped;
	RestCluster restCluster;
	PlatformMgr platformMgr;

	public ZkClusterDocumentHandler(DocumentMgr docMgr, PlatformMgr platformMgr, RestCluster restCluster, SequencedDocumentHandler wrapped) {
		this.platformMgr = platformMgr;
		this.wrapped = wrapped;
		this.restCluster = restCluster;
	}

	//methods to manage write consistency when clustering independent underlying data stores
	private CuratorLockWrapper lockDocument(Document doc)  throws Exception {
		if (isBootstrapping()) {
			return new CuratorLockWrapper();
		}
		else {
			return restCluster.acquireLock(doc.getLockKey());
		}
	}

	/**
<<<<<<< HEAD
	 * Gets the current op sequence + 1
	 *
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	private long getNextOpSequence(DocumentEnvelope doc) throws Exception {

		if (isBootstrapping()) {
			return 0L;
		}

		else {
			return restCluster.getCurrentOpSequence(doc.getDocumentType().toString()) + 1;
		}

	}

	/**
=======
>>>>>>> refs/remotes/origin/sprint_13
	 * operation performed, increment op sequence
	 *
	 * @param doc
	 * @throws Exception
	 */
	private void incrementOpSequence(DocumentEnvelope doc) throws Exception {

		if (isBootstrapping()) {
			return;
		}

		long nextSequence = restCluster.incrementOpSequence(doc.getDocumentType().toString());
		doc.setOpSequence(nextSequence);
		writeOpSequence(doc);

		//update local cache to avoid DB read for consistency check
		m_cachedOpSequences.put(doc.getDocumentType().toString(), nextSequence);

	}

	/**
	 * perform a consistency check which will detect whether the local node is in sync with the cluster with regards to the given
	 * document type
	 *
	 * @param type the document type whose consistency should be checked
	 * @return whether consistency has been confirmed
	 * @throws Exception if anything goes wrong
	 */
	protected boolean checkConsistency(DocumentType type) throws Exception {


		if (isBootstrapping()) {
			//logger.error("DEBUG Skipping consistency check in bootstrap mode for doc " + doc.getKey());
			return true;
		}

		long localSequence;

		if (m_cachedOpSequences.containsKey(type.toString())) {
			localSequence = m_cachedOpSequences.get(type.toString());
		}
		else {
			localSequence = getOpSequence(type.toString());
		}

		long sharedSequence = restCluster.getCurrentOpSequence(type.toString());

		if (localSequence < sharedSequence) {

			logger.warn(" consistency issue detected. Will attempt to resolve:  Shared sequence is " + sharedSequence + " local is " + localSequence);
			resolveConsistency(type.toString(), localSequence, sharedSequence);
			logger.warn(" consistency resolved ");

		}
		else {
			logger.debug("DEBUG consistency OK . Shared sequence is " + sharedSequence + " local is " + localSequence);
		}


		return true;
	}

	/**
	 * performs cluster consistency checking before returning a document
	 */
	@Override
	public Document getDocument(DocumentType docType, String docName) throws Exception {

		checkConsistency(docType);
		return wrapped.getDocument(docType, docName);

	}

	@Override
	public Collection<Document> getAllTenants() throws Exception {

		checkConsistency(DocumentType.TENANT);
		return wrapped.getAllTenants();

	}


	@Override
	public DocumentEnvelope createDocument(Document doc) throws Exception {

		//acquire a lock on tenant-type  +name?
		//IfClusterInterface.lockDocument(doc);
		CuratorLockWrapper lock = lockDocument(doc);

		try {

			//check consistency of local DB with shared sequence
			//if (IfClusterInterface.checkConsisitency(doc)   // block to resolve? -- this will go away if we route to specific nodes
			checkConsistency(doc.getDocumentType());

			//update to the next op sequence
			doc.setOpSequence(getNextOpSequence(doc));

			//ask DB to create the doc -- get a document for post-create activities
			DocumentEnvelope ret = wrapped.createDocument(doc);

			//update shared sequence -- anyone who did not get the mirror is out of sync (local < global)
			incrementOpSequence(ret);

			//success
			return ret;

		}
		catch (Exception ex) {

			//TODO: rollback based on when we errored?
			throw ex;

		}
		finally {
			lock.release();
		}

	}

	@Override
	public DocumentEnvelope updateDocument(Document doc) throws Exception {

		//acquire a lock on tenant-type  +name?
		//IfClusterInterface.lockDocument(doc);
		CuratorLockWrapper lock = lockDocument(doc);
		try {

			//check consistency of local DB with shared sequence
			//if (IfClusterInterface.checkConsisitency(doc)   // block to resolve? -- this will go away if we route to specific nodes?
			checkConsistency(doc.getDocumentType());

			//update to the next op sequence
			doc.setOpSequence(getNextOpSequence(doc));

			//Ask DB to create updated version
			DocumentEnvelope ret = wrapped.updateDocument(doc);

			//update shared sequence -- anyone who did not get the mirror is out of sync (local < global)
			incrementOpSequence(ret);

			//success
			return ret;
		}
		catch (Exception ex) {
			//TODO: rollback based on when we errored?
			throw ex;
		}
		finally {
			lock.release();
		}

	}

	private void resolveConsistency(String opSequenceKey, long localSeqence, long sharedSequence) throws Exception{

		//ignore op sequence if we are the only node in the platform
		String thisHost = platformMgr.getMe().getHost();

		//call each other node in the platform
		List<PlatformNode> restNodes = platformMgr.getPlatform().getNodes(NodeRole.REST);

		//try to get each missing doc
		for (long missing=localSeqence +1; missing <= sharedSequence; missing ++) {

			boolean synced = false;
			for (PlatformNode restNode : restNodes) {
				if (!restNode.getHost().equals(thisHost)) {

					try {
						DocumentHandlerRest target = new DocumentHandlerRest(platformMgr.getTenantID(), restNode);
						Document doc = target.getDocumentBySequence(opSequenceKey, missing);
						wrapped.applyDocumentVerbatim(doc);

					} catch (Exception ex) {//TODO: handle expected messages differently
						ex.printStackTrace();
						logger.warn("current host " + thisHost + ": target node " + restNode.getHost()
							+ " retrieval by op sequence failed on doc " + missing + " with message " + ex.getMessage());
					}

				}
			}

			if (!synced) {
				throw new Exception(" failed to resolve consistency: document with sequence:key "
					+ opSequenceKey + ":" + missing + " could not be retrieved from any peer" );
			}
		}
	}

	//other delegate methods to the wrapped document handler

	public void acceptMirrorUpdate(Document doc, Operation op) throws Exception {
		wrapped.acceptMirrorUpdate(doc, op);
	}

	public Document getDocumentBySequence(String opSequenceKey, long opSequence) throws Exception {
		return wrapped.getDocumentBySequence(opSequenceKey, opSequence);
	}

	public void writeOpSequence(DocumentEnvelope doc) throws Exception {
		wrapped.writeOpSequence(doc);
	}

	public long getOpSequence(String opSequenceKey) throws Exception {
		return wrapped.getOpSequence(opSequenceKey);
	}

	public String getIdForOpSequence(String opSequenceKey, long opSequence) throws Exception {
		return wrapped.getIdForOpSequence(opSequenceKey, opSequence);
	}

	public Document getDocument(String docID) throws Exception {
		return wrapped.getDocument(docID);
	}

	public boolean documentExists(DocumentType docType, String docName) throws Exception {
		return wrapped.documentExists(docType, docName);
	}

	public boolean documentExists(String docID) throws Exception {
		return wrapped.documentExists(docID);
	}

	public DocumentEnvelope deleteDocument(DocumentType docType, String docName) throws Exception {
		return wrapped.deleteDocument(docType, docName);
	}

	public List<Document> getDocumentsOfType(DocumentType docType) throws Exception {
		return wrapped.getDocumentsOfType(docType);
	}

	public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		return wrapped.getDocumentsOfTypeWithTags(docType, tags);
	}

	public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
		return wrapped.getEnvelopesOfType(docType);
	}

	public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		return wrapped.getEnvelopesOfTypeWithTags(docType, tags);
	}

	public boolean tenantExists(String tid) throws Exception {
		return wrapped.tenantExists(tid);
	}

	public void addListener(DocumentListener listener) {
		wrapped.addListener(listener);
	}

	public boolean removeListener(DocumentListener listener) {
		return wrapped.removeListener(listener);
	}

	public void initForTenant(boolean doBootstrap) throws Exception {

		//intercept this and initialize using this wrapper document handler
		DocUtils.initForTenant(platformMgr, this, doBootstrap);

	}

	public Collection<Tag> getTags() {
		return wrapped.getTags();
	}

	public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
		return wrapped.getTagsForDocumentType(documentType);
	}

	public DocumentTree getDocumentTree(DocumentType documentType) {
		return wrapped.getDocumentTree(documentType);
	}

	public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType type, String grouping) throws Exception {
		return wrapped.getDocumentsOfTypeWithGrouping(type, grouping);
	}

	public DocumentEnvelope deleteGroup(DocumentType forValue, String name) throws Exception {
		return wrapped.deleteGroup(forValue, name);
	}

	public void applyDocumentVerbatim(Document document) throws DocumentException {
		wrapped.applyDocumentVerbatim(document);
	}

	public boolean isBootstrapping() { return wrapped.isBootstrapping(); }
	public void setBootstrapping(boolean isBootstrapping) { wrapped.setBootstrapping(isBootstrapping); }

	@Override
	public void updateListeners(Document doc, Operation op) throws Exception {
		wrapped.updateListeners(doc, op);
	}

	@Override
	public Tenant getTenant() { return wrapped.getTenant(); }

	@Override
	public void setTenant(Tenant tenant) { wrapped.setTenant(tenant); }

	@Override
	public void setDBMgr(DBMgr dbMgr) { wrapped.setDBMgr(dbMgr); }

}
