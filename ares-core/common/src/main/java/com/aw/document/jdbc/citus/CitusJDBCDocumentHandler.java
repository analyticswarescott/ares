package com.aw.document.jdbc.citus;

import com.aw.common.Tag;
import com.aw.common.cluster.db.CuratorLockWrapper;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.tenant.Tenant;
import com.aw.document.*;
import com.aw.document.exceptions.*;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Implements persistence methods from DocumentHandlerBase for JDBC databases <br>
 *     SQL is obtained from an IDocumentSQLProvider implementation
 */
public class CitusJDBCDocumentHandler extends AbstractDocumentHandler implements SecurityAware  {
    public static final Logger logger = Logger.getLogger(CitusJDBCDocumentHandler.class);
    protected DocumentJDBCProvider sqlProvider;

	protected RestCluster restCluster;


	//TODO: this is a workaround for postgres plan re-use issues that cause citus to fail after a prepared statement is used 5 times
	//TODO: ensure cross-process safety if many of these handlers are hitting the DB (i.e. assume the 5x rule is per connection)
			//one option is to install a server # in the REST role and use it to cycle different ranges
	//TODO: shouldn't most or all of the SQL here be moved to the SQL provider?
	long q = 0;
	long z = 0;
	private synchronized String bogusWhere() {
		if (q == Long.MAX_VALUE) {
			q = 0;
		}
		q++;
		return " and " + q + "=" + q;
	}
	private synchronized long bogusLong() {
		if (z == Long.MAX_VALUE) {
			z = 0;
		}
		z++;
		return z;
	}

	@Override
    public void reset() throws Exception {
    }

    @Inject
    public CitusJDBCDocumentHandler(DocumentMgr docMgr, PlatformMgr platformMgr, DocumentJDBCProvider sqlProvider, RestCluster restCluster) {
    	super(docMgr, platformMgr);
    	this.sqlProvider = sqlProvider;
		this.restCluster = restCluster;
    }

    @Override
    protected Document createDocumentInDB(Document doc, boolean forceName) throws Exception {

		CuratorLockWrapper lock = null;
		try (Connection conn = dbMgr.getConnection(getTenant())) {

			if (restCluster.getClient() != null) {
				lock = restCluster.acquireLock(doc.getLockKey());
			}
			//conn.setAutoCommit(false);

			try {

				final boolean generateName;
				if (doc.getName() != null) { //doc submitted with a name -- this should be reserved for known docs uploaded by DG (?) and throw an error if exists

					if (!forceName) { //unless beforeCreate has allowed a force, you have to be DG to save with a name
						SecurityUtil.verifySystemAccess();
					}
					generateName = false;

				} else { //generate a name
					generateName = true;
				}

				try (PreparedStatement ps = generateName ? sqlProvider.getInsertWithoutName(conn, doc) : sqlProvider.getInsertWithName(conn, doc)) {
					//logger.error("CREATE: " + ps.toString());
					ps.executeUpdate();
				}

				// Save any tags tied to this document
				saveTags(doc, conn);

				//TODO: need to re-read because if incremental name -- possible to use GeneratedKeys and still be generic-JDBC?
				return getDocument(doc.getID());

				//conn.commit();
			} finally {
				if (lock != null) {
					lock.release();
				}
			}

		}
	}


    @Override
    protected Document updateDocumentInDB(Document doc) throws Exception {

		CuratorLockWrapper lock = null;
        try (Connection conn = dbMgr.getConnection(getTenant())) {
           // conn.setAutoCommit(false);

			if (restCluster.getClient() != null) {
				lock = restCluster.acquireLock(doc.getLockKey());
			}

			//TODO: will need rollback here to restore state after a DML failure


            //Mark all current versions as OLD
            try (PreparedStatement ps = sqlProvider.getUpdateOldVersions(conn, doc)) {
				if (ps.executeUpdate() < 1) {
					throw new Exception("could not find current document for " + doc.getDocumentType() + "/" + doc.getName());
				}

			}

			//TODO: re-visit -- this is to make original tests pass from when deleted was a hard delete
			if (DocumentType.DOCUMENT_GROUP == doc.getDocumentType() && doc.getDeleted()) {
				// remove this grouping from any documents

				for (DocumentType t : DocumentType.values()) { //TODO: this is a workaround for one-shard rule in Citus (by adding type to the WHERE)

					try (PreparedStatement ps = conn.prepareStatement("UPDATE document SET grouping = NULL WHERE grouping = ? and type = ? " + bogusWhere())) {
						ps.setString(1, doc.getName());
						ps.setString(2, t.toString());
						ps.executeUpdate();
					}
				}
			}

			//now insert the new version
			try (PreparedStatement ps = sqlProvider.getUpdate(conn, doc)) {

				if (ps.executeUpdate() < 1) {
					throw new Exception("could not update document in database");
				}

			}

           // conn.commit();
			saveTags(doc, conn);

			//TODO: need to re-read because if incremental name -- possible to use GeneratedKeys and still be generic-JDBC?
			return getAnyDocument(doc.getID());


        }
		finally {
			if (lock != null) {
				lock.release();
			}
		}
    }

    @Override
    protected Document deleteDocumentFromDBPermanent(DocumentType docType, String docName) throws Exception {


        try (Connection conn = dbMgr.getConnection(getTenant())) {

			conn.setAutoCommit(false);

        	try {

            	//get the document - if it doesn't exist that's an error
            	Document document = getDocument(docType, docName);
            	if (document == null) {
            		throw new DocumentNotFoundException("no such document, type=" + docType.toString().toLowerCase() + " name=" + docName);
            	}

    			// Remove any tags tied to this document
    			removeTags(document, conn);

    			if (DocumentType.DOCUMENT_GROUP == docType) {
    				// remove this grouping from any documents
    				try (PreparedStatement ps = conn.prepareStatement("UPDATE document SET grouping = NULL WHERE grouping = ?")) {
    					ps.setString(1, document.getName());
    					ps.executeUpdate();
    				}
    			}

            	//do the delete
            	try (PreparedStatement ps = sqlProvider.getDeletePermanent(conn, document)) {
    				ps.executeUpdate();
    			} catch (Exception e) {
    				conn.rollback();
    				throw e;
    			}

            	//commit if everything goes ok
            	conn.commit();

            	//return whether the document was actually deleted
            	return document;

        	} catch (Exception e) {

        		//rollback on error
        		conn.rollback();
        		throw e;

        	} finally {
        		conn.setAutoCommit(true);
        	}

        }

    }

    //retrieval

    @Override
    protected Document getDocumentFromDB(DocumentType docType, String docName) throws Exception {

        try (Connection conn = dbMgr.getConnection(getTenant())) {

            try (PreparedStatement ps = sqlProvider.getSelectCurrentDocument(conn, docType, docName)) {
            	try (ResultSet rs = ps.executeQuery()) {

					JSONArray ret = DBMgr.list2JSON(rs, RETRIEVE_ALL);

					if (ret.length() == 0) {

						//TODO: is enum-based a good way to make this pluggable?
						if (docType.getDefaultDocName() != null) {
							//TODO: can we always name default docs with the user ID? -- have added type to allow one default doc per type and conform to current standard

							return getDefaultDoc(docType, docType.getDefaultDocName(), getUserID(), getUserID());
						}

						throw new DocumentNameNotFoundException(" Document name: \"" + docName + "\" no current version found for type " + docType.toString());

					}

					//just warn in thi case
					if (ret.length() > 1) {
						logger.warn("multiple current version doc matches for doc " + docName + "for type " + docType.toString() + " tenant=" + getTenantID());
					}

					JSONObject obj = (JSONObject) ret.get(0);

					//TODO -- possibly avoid parsing by returning Doc objects from DBMgr static List2JSON
					final Document document = new Document(obj);

					retrieveTags(document, conn);

					return document;

				}
			}

        }
    }


    protected Document getDefaultDoc(DocumentType docType, String docName, String newName, String newAuthor) throws Exception {

        try (final Connection conn = dbMgr.getConnection(getTenant())) {
            try (PreparedStatement ps = sqlProvider.getSelectDocument(conn, Tenant.SYSTEM_TENANT_UID, docType, docName)) {
				try (ResultSet rs = ps.executeQuery()) {

					JSONArray ret = DBMgr.list2JSON(rs, RETRIEVE_ALL);
					if (ret.length() == 0) {
						throw new DocumentDefaultNotFoundException(" default document " + docName + " not found of type " + docType.toString());
					} else if (ret.length() > 1) {
						throw new DocumentDBStateException("multiple doc matches for default document " + docName);
					}
					JSONObject obj = (JSONObject) ret.get(0);

					//TODO -- possibly avoid parsing by returning Doc objects from DBMgr static List2JSON
					Document toReturn = new Document(obj);
					toReturn.setName(newName);
					toReturn.setAuthor(newAuthor);
					toReturn.setTenantID(getTenant().getTenantID());

					createDocument(toReturn);

					return getDocument(toReturn.getID());

				}
			}
        }
    }



	@Override
	public boolean documentVersionExists(DocumentType docType, String docName, int version) throws Exception {

		try (final Connection conn = dbMgr.getConnection(getTenant())) {
			String sql = "select count(*) as cnt from document where type = ? and name = ? and version = ? ";

			final List<Object> params = new ArrayList<>();
			params.add(docType.toString());
			params.add(docName);
			params.add(version);

			try (final PreparedStatement ps = getTenantSafeResult(getTenant().getTenantID(), conn, sql, params)) {
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					return rs.getLong("cnt") > 0;
				}
			}

		}
	}

    @Override
    public boolean documentExists(DocumentType docType, String docName) throws Exception {

        try (final Connection conn = dbMgr.getConnection(getTenant())) {
            try (final PreparedStatement ps = sqlProvider.getDocumentExists(conn, docType, docName)) {

				//logger.error(" QUERY: " + ps.toString());

				try (ResultSet rs = ps.executeQuery()) {
					//logger.error(" WORKED for " + docName);
					rs.next();
					return rs.getLong("cnt") > 0;
				}
				finally {
					conn.close();
				}

			}
        }
    }


    @Override
    public boolean documentExists(String docID) throws Exception {
		logger.debug(" documentExists invoked");
        try (final Connection conn = dbMgr.getConnection(getTenant())) {
            try (final PreparedStatement ps = sqlProvider.getDocumentExists(conn, docID)) {
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					return rs.getLong("cnt") > 0;
				}
			}
        }
    }


	/**
	 * get document even if deleted
	 * @param docID
	 * @return
	 * @throws Exception
	 */
	protected Document getAnyDocument(String docID) throws Exception {

		try (Connection conn = dbMgr.getConnection(getTenant())) {
			try (PreparedStatement stmt = sqlProvider.getSelectDocument(conn, docID, true)) {
				try (ResultSet rs = stmt.executeQuery()) {

					JSONArray ret = DBMgr.list2JSON(rs, RETRIEVE_ALL);
					if (ret.length() == 0) {
						throw new DocumentIDNotFoundException(docID + " not found");

					}

					if (ret.length() > 1) {
						throw new DocumentDBStateException("multiple doc matches for doc " + docID);
					}
					JSONObject obj = (JSONObject) ret.get(0);

					//TODO -- possibly avoid parsing by returning Doc objects from DBMgr static List2JSON
					final Document document = new Document(obj);

					// Get any tags for this document
					retrieveTags(document, conn);

					return document;
				}

			}

		}

	}

	@Override
    public Document getDocument(String docID) throws Exception {

        try (Connection conn = dbMgr.getConnection(getTenant())) {
            try (PreparedStatement ps = sqlProvider.getSelectDocument(conn, docID)) {
				try (ResultSet rs = ps.executeQuery()) {

					JSONArray ret = DBMgr.list2JSON(rs, RETRIEVE_ALL);
					if (ret.length() == 0) {
						throw new DocumentIDNotFoundException(docID + " not found");
					}

					if (ret.length() > 1) {
						throw new DocumentDBStateException("multiple doc matches for doc " + docID);
					}
					JSONObject obj = (JSONObject) ret.get(0);

					//TODO -- possibly avoid parsing by returning Doc objects from DBMgr static List2JSON
					final Document document = new Document(obj);

					// Get any tags for this document
					retrieveTags(document, conn);

					return document;
				}
			}
        }
    }

	/**
	 * Apply this exact document to the database
	 *
	 * @param doc
	 * @throws Exception
	 */
	@Override
	public void applyDocumentVerbatim(Document doc) throws  DocumentException{
		try (Connection conn = dbMgr.getConnection(getTenant())) {

			conn.setAutoCommit(false);

			try {

				//Mark all current versions as OLD -- this will work for an update and do no harm for a CREATE
				try (PreparedStatement ps = sqlProvider.getUpdateOldVersions(conn, doc)) {
					ps.executeUpdate();
				}

				try (PreparedStatement ps = sqlProvider.getInsertVerbatim(conn, doc)) {
					ps.executeUpdate();
				}

				// Save any tags tied to this document
				saveTags(doc, conn);

				conn.commit();

			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}

		} catch (Exception e) {
			throw new DocumentException("error applying document", e);
		}
	}

	//Search methods

    @Override
    public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
    	return getEnvelopesOfTypeWithTags(docType, Collections.emptyList());
    }

	@Override
	public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		try {
			try (Connection conn = dbMgr.getConnection(getTenant())) {
				try (PreparedStatement ps = sqlProvider.getSelectCurrentEnvelopes(conn, docType, tags)) {
				/*	if (tags.size() > 0) {
						logger.error(" GETTING with TAGS: " + ps.toString());
					}*/
					return getList(conn, docType, ps, DocumentEnvelope.class);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to query envelopes by type and tag", e);
			throw new RuntimeException(e);
		}
	}

	@Override
    public List<Document> getDocumentsOfType(DocumentType docType) throws Exception {
    	return getDocumentsOfTypeWithTags(docType, Collections.emptyList());
    }

	@Override
	public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType docType, String grouping) {
		try {
			try (Connection conn = dbMgr.getConnection(getTenant())) {
				try (PreparedStatement ps = sqlProvider.getSelectCurrentEnvelopes(conn, docType)) {
					return getList(conn, docType, ps, Document.class);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to query documents of type with grouping", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		try {
			try (Connection conn = dbMgr.getConnection(getTenant())) {
				try (PreparedStatement ps = sqlProvider.getSelectCurrentDocument(conn, docType, tags)) {
					return getList(conn, docType, ps, Document.class);
				}
			}
		} catch (Exception e) {
			logger.error("Unable to query documents of type with tags", e);
			throw new RuntimeException(e);
		}
	}

    protected <T extends DocumentEnvelope> List<T> getList(Connection conn, DocumentType docType, PreparedStatement ps, Class<T> type) throws Exception {


		//logger.error(" GETLIST: " + ps.toString());
		try (ResultSet rs = ps.executeQuery()) {

			JSONArray tmp = DBMgr.list2JSON(rs, RETRIEVE_ALL);

			List<T> retD = new ArrayList<>();
			for (int x = 0; x < tmp.length(); x++) {

				//build and initialize the document
				Object doc = tmp.get(x);
				JSONObject docJ = (JSONObject) doc;
				T thisDoc = type.newInstance();
				thisDoc.initialize(docJ.toString());
				thisDoc.setDocHandler(this);

				// Get any tags for this document
				retrieveTags(thisDoc, conn);

				retD.add(thisDoc);

			}

			return retD;
		}

    }

    @Override
	public Collection<Tag> getTags() {
		Set<Tag> tags = new HashSet<>();
		try {
			try (final Connection conn = dbMgr.getConnection(getTenant())) {
				try (final PreparedStatement ps = conn.prepareStatement("SELECT tag FROM document_tag WHERE 1=1 " + bogusWhere())) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							tags.add(Tag.valueOf(rs.getString("tag")));
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Unable to query for tags");
			throw new RuntimeException(e);
		}
		return tags;
	}

	@Override
	public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
		Set<Tag> tags = new HashSet<>();
		try {
			try (final Connection conn = dbMgr.getConnection(getTenant())) {
				try (final PreparedStatement ps = conn.prepareStatement("SELECT tag FROM DOCUMENT_TAG join document " +
					"on DOCUMENT_TAG.doc_id = DOCUMENT.id " +
					"  WHERE DOCUMENT.type = ? " + bogusWhere()))
				{
					ps.setString(1, documentType.toString());
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							tags.add(Tag.valueOf(rs.getString("tag")));
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Unable to query for tags of document type");
			throw new RuntimeException(e);
		}
		return tags;
	}



	@Override
	public DocumentTree getDocumentTree(DocumentType documentType) {
		try {

			// Get all the required document types
			final List<DocumentEnvelope> documentEnvelopes = getEnvelopesOfType(documentType);

			final List<DocumentEnvelope> documentGroups = getEnvelopesOfType(DocumentType.DOCUMENT_GROUP);

			documentEnvelopes.addAll(documentGroups);
			DocumentTree documentTree = new DocumentTree(documentType, documentEnvelopes);

			documentTree.buildTree();
			return documentTree;
		} catch (Exception e) {
			throw new RuntimeException("Unable to get grouped documents", e);
		}

	}


	/**
     * System Methods
     */

    @Override
    public Collection<Document> getAllTenantsFromDB() throws Exception {

    	verifySystemAccess(); //this is a system method and is, by design, not tenant safe
		return getDocumentsOfType(DocumentType.TENANT);

    }

	/**
	 * Adds any tags that have been tied to this document.
	 * @param doc The document to assign tags to.
	 * @param conn The DB connection
	 * @throws SQLException on jdbc errors
     */


	private void retrieveTags(DocumentEnvelope doc, Connection conn) throws SQLException {

		long k = bogusLong();
		try (final PreparedStatement preparedStatement = conn.prepareStatement("SELECT tag FROM document_tag WHERE doc_id = ? " + bogusWhere())) {
			preparedStatement.setString(1, doc.getID());

			//logger.error(" TAGS: " + preparedStatement.toString());
			try (final ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					Tag tag = Tag.valueOf(resultSet.getString("tag"));
					doc.addTag(tag);
				}
			}
		}
	}

	/**
	 * Remove any tags tied to this document
	 * @param doc The document that may have tags.
	 * @param conn The DB connection
	 * @throws SQLException on jdbc error
     */
	private void removeTags(DocumentEnvelope doc, Connection conn) throws SQLException {

		try (final PreparedStatement ps = conn.prepareStatement("DELETE FROM document_tag WHERE doc_id IN (SELECT id from DOCUMENT where type = ? and name = ? and tenant_id = ?)" + bogusWhere())) {
			ps.setString(1, doc.getDocumentType().toString());
			ps.setString(2, doc.getName());
			ps.setString(3, getTenant().getTenantID());
			ps.execute();
		}

	}

	/**
	 * Saves all tags for the given document.
	 * @param doc The document containing tags.
     */
	private void saveTags(DocumentEnvelope doc, Connection conn) throws SQLException {

		// Tie any existing tags to this doc
		if (!doc.getTags().isEmpty()) {

				for (Tag tag : doc.getTags()) {
					try (final PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO document_tag ( tag, type, doc_id) " +
						"VALUES ( '" + tag + "' ,?, '" + doc.getID()
						+ "')")) {
						preparedStatement.setString(1, doc.getDocumentType().toString());
						//TODO: this is a workaround for the fact that citus fails after using the same prepared statment >5 times -- this is related to a hardcoded condition in the postgres planner -- if this is fixed this can be revisited
						//preparedStatement.setString(2, doc.getID());
						//System.out.println("INSERT TAGS: " + preparedStatement.toString());
						preparedStatement.execute();
					}
				}
			}
		}



	protected  PreparedStatement getTenantSafeResult(String tenantID, Connection conn, String sql, List<Object> params) throws Exception {

		final List<Object> paramsIn = new ArrayList<>(params);

		sql += " AND deleted=0 AND tenant_id = ?  ";
		paramsIn.add(tenantID);

		PreparedStatement ps = conn.prepareStatement(sql);

		// Param numbering starts with 1
		int i = 1;
		for (Object val : paramsIn) {
			if (val instanceof String) {
				ps.setString(i, (String) val);
			}
			if (val instanceof Long) {
				ps.setLong(i, (Long) val);
			} else {
				ps.setObject(i, val);
			}
			i++;
		}

		return ps;

	}

}
