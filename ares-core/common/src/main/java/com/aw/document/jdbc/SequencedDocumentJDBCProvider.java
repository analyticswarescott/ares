package com.aw.document.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Provides vendor-specific SQL for SQL/DML operations, including cluster-aware operations. For a database type to
 * be supported in cluster-aware mode, this interface must be implemented
 */
public interface SequencedDocumentJDBCProvider extends DocumentJDBCProvider {

	/**
	 * returns insert clause for recording a write operation sequence that has been applied locally
	 * @param conn
	 * @param docID
	 * @param opSequenceKey
	 * @param opType
	 * @param opSequence
	 * @return
	 * @throws Exception
	 */
	public PreparedStatement getInsertOpSequnce(Connection conn, String docID, String opSequenceKey, long opSequence) throws Exception;

	/**
	 * returns SQL to get op_sequence
	 * @param conn
	 * @param opSequenceKey
	 * @return
	 * @throws Exception
	 */
	public String getReadOpSequence(String opSequenceKey) throws Exception;

	/**
	 * returns SQL to get op_sequence
	 * @param conn
	 * @param opSequenceKey
	 * @return
	 * @throws Exception
	 */
	public String getIDForOpSequence() throws Exception;


}
