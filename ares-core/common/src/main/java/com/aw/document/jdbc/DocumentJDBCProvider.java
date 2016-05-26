package com.aw.document.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import com.aw.common.Tag;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentType;

/**
 * Provides database-specific support for documents
 *
 *
 *
 */
public interface DocumentJDBCProvider extends JDBCProvider {

	static String DOCUMENT_NAME_DELIMITER = "_";

    /**
    *
    *Returns prepared statment to insert a document whose name is already supplied
    *
    * @param conn
    * @param doc
    * @return
    * @throws Exception
    */
   public PreparedStatement getInsertWithName(Connection conn, Document doc) throws Exception;

   /**
    * Returns prepared statement to insert a new document, formulating a unique name in the process
    *
    * @param conn
    * @param doc
    * @return
    * @throws Exception
    */
   public PreparedStatement getInsertWithoutName(Connection conn, Document doc) throws Exception;


   /**
    * Returns prepared statment to create a new version of an existing document
    *
    * @param conn
    * @param doc
    * @return
    * @throws Exception
    */
   public PreparedStatement getUpdate(Connection conn, Document doc) throws Exception;

   /**
    *
    * Returns prepared statement to update all exsting versions of a semantic document to is_current = 0
    *
    * @param conn
    * @param doc
    * @return
    * @throws Exception
    */
   public PreparedStatement getUpdateOldVersions(Connection conn, Document doc) throws Exception;

   /**
    * Returns a statement to delete the given document.  This is not currently used and is included for future pruning features
    *
    * @param conn The connection from which the statement is built
    * @param envelope The envelope of the document name to delete
    * @return The statement to execute
    * @throws Exception If anything goes wrong
    */
   public PreparedStatement getDeletePermanent(Connection conn, DocumentEnvelope envelope) throws Exception;

   /**
    * Return statement to get a particular document based on guid
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectDocument(Connection conn, String guid, boolean includeDeleted) throws SQLException;

   /**
    * Return statement to get a particular document based on guid
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectDocument(Connection conn, String guid) throws SQLException;

   /**
    * Return statement to get current document of given type and name
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectCurrentDocument(Connection conn, DocumentType type, String name) throws SQLException;

   /**
    * Return statement to get latest document by the given author for the given type and name
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectDocument(Connection conn, String author, DocumentType type, String name) throws SQLException;

   /**
    * Return statement to get latest document by the given author for the given type and name
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectCurrentDocument(Connection conn, DocumentType type, Collection<Tag> tags) throws SQLException;

   /**
    * Return statement to get latest document by the given author for the given type and name
    *
    * @return the prepared statement
    */
   public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type, Collection<Tag> tags) throws SQLException;

   /**
    * Return statement to get list of envelopes of the requested type
    *
    * @param conn the connection to use to build the statement
    * @param type the type of document requested
    * @return the statement for querying envelopes of the requested type
    * @throws SQLException if anything goes wrong
    */
   public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type) throws SQLException;

   /**
    * Return statement to get list of envelopes of the requested type and grouping
    *
    * @param conn the connection to use to build the statement
    * @param type the type of document requested
    * @param grouping the requested grouping
    * @return the statement for querying envelopes of the requested type
    * @throws SQLException if anything goes wrong
    */
   public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type, String grouping) throws SQLException;

	/**
	 * Returns prepared statement to insert a document verbatim, i.e. using only its content. Used for accepting mirror updates
	 *
	 * @param conn
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public PreparedStatement getInsertVerbatim(Connection conn, Document doc) throws Exception;

	/**
	 * Returns prepared statement that selects the count of documents matching the given type and name
	 *
	 * @param conn
	 * @param docType
	 * @param docName
	 * @return
	 */
	public PreparedStatement getDocumentExists(Connection conn, DocumentType docType, String docName) throws SQLException;

	/**
	 * Returns prepared statement that selects the count of documents matching the given type and name
	 *
	 * @param conn
	 * @param docID
	 * @return
	 */
	public PreparedStatement getDocumentExists(Connection conn, String docID) throws SQLException;

}
