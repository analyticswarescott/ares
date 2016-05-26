package com.aw.document.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * default implementation for op sequencing statements
 *
 *
 *
 */
public abstract class AbstractSequencedDocumentJDBCProvider extends AbstractDocumentJDBCProvider implements SequencedDocumentJDBCProvider {

	@Override
	public String getReadOpSequence(String opSequenceKey) throws Exception {
		return "select max(op_sequence) opseq from op_sequence where op_sequence_key = ? ";
	}

	@Override
	public String getIDForOpSequence() throws Exception {
		return "select doc_id from op_sequence where op_sequence_key = ? and op_sequence = ? ";

	}

	@Override
	public PreparedStatement getInsertOpSequnce(Connection conn, String docID, String opSequenceKey, long opSequence) throws Exception {

		PreparedStatement ps =
			conn.prepareStatement("insert into OP_SEQUENCE (doc_id, op_sequence_key, op_sequence)"
				+ "values ( ?,?,?) ");

		int paramOrdinal = 1;

		ps.setString(paramOrdinal++, docID);
		ps.setString(paramOrdinal++, opSequenceKey);
		ps.setLong(paramOrdinal++, opSequence);

		return ps;
	}

}
