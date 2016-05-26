package com.aw.document.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.aw.document.SequencedDocumentHandler;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentMgr;
import com.aw.document.action.Operation;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.google.common.base.Preconditions;

public class SequencedJDBCDocumentHandler extends JDBCDocumentHandler implements SequencedDocumentHandler {

	SequencedDocumentJDBCProvider sequencedSqlProvider;

	public SequencedJDBCDocumentHandler(DocumentMgr docMgr, PlatformMgr platformMgr, SequencedDocumentJDBCProvider sqlProvider, RestCluster restCluster) {
		super(docMgr, platformMgr, sqlProvider);
		sequencedSqlProvider = sqlProvider;
	}

	@Override
	protected Document createDocumentInDB(Document doc, boolean forceName) throws Exception {
		Document ret = super.createDocumentInDB(doc, forceName);
		ret.setOpSequence(doc.getOpSequence());
		return ret;
	}

	@Override
	protected Document updateDocumentInDB(Document doc) throws Exception {
		Document ret = super.updateDocumentInDB(doc);
		ret.setOpSequence(doc.getOpSequence());
		return ret;
	}

	@Override
	public void writeOpSequence(DocumentEnvelope doc) throws Exception {

		Preconditions.checkNotNull(sequencedSqlProvider, "sql provider " + sqlProvider + " does not support clustered operations");

		try (Connection conn = dbMgr.getConnection(getTenant())) {

			conn.setAutoCommit(false);

			PreparedStatement ps = sequencedSqlProvider.getInsertOpSequnce(conn, doc.getID(),
			 getTenantID() + "-" + doc.getDocumentType().toString(), doc.getOpSequence());

			logger.debug(">>>>>>>>>>>>>> DEBUG: writing op sequence " + doc.getOpSequence() + "  for document " + doc.getKey() );

			ps.executeUpdate();

			conn.commit();

		}

	}


	public long getOpSequence(String opSequenceKey) throws Exception {

		Preconditions.checkNotNull(sequencedSqlProvider, "sql provider " + sqlProvider + " does not support clustered operations");

		try (Connection conn = dbMgr.getConnection(getTenant())) {

			conn.setAutoCommit(false);

			String sql = sequencedSqlProvider.getReadOpSequence(getTenantID() + "-" + opSequenceKey);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getTenantID() + "-" + opSequenceKey);

				try (ResultSet rs = ps.executeQuery()) {
					 rs.next();

					long seq = rs.getLong("opseq");
					return seq;
				}


		}
	}


	public String getIdForOpSequence(String opSequenceKey, long opSequence) throws Exception {

		Preconditions.checkNotNull(sequencedSqlProvider, "sql provider " + sqlProvider + " does not support clustered operations");

		try (Connection conn = dbMgr.getConnection(getTenant())) {

			conn.setAutoCommit(false);

			String sql = sequencedSqlProvider.getIDForOpSequence();

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getTenantID() + "-" + opSequenceKey);
			ps.setLong(2, opSequence);

				try (ResultSet rs = ps.executeQuery()) {

					String ret = rs.getString("doc_id");
					return ret;
				}

		}

	}

	public Document getDocumentBySequence(String opSequenceKey, long opSequence) throws  Exception {
		String docID = getIdForOpSequence(opSequenceKey, opSequence);
		if (docID == null) {
			throw new Exception(" no ID found for key " + opSequenceKey + " sequence " + opSequence);
		}
		return getAnyDocument(docID);
	}

	@Override
	public void acceptMirrorUpdate(Document doc, Operation operation) throws Exception{


		if (!documentVersionExists(doc.getDocumentType(), doc.getName(), doc.getVersion())) {
			applyDocumentVerbatim(doc);
			writeOpSequence(doc);
		}
		else {
			logger.warn(" skipping doc mirror as it already exists "
				+ doc.getKey());
		}

	}

}
