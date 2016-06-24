package com.aw.document.jdbc;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.aw.common.Tag;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentType;
import com.aw.document.security.DocumentPermission;

/**
 * implements prepared statement calls using standard sql, and the reference ddl defined in com.aw.common.rdbms.migrations.reference
 *
 *
 *
 */
public abstract class AbstractDocumentJDBCProvider extends AbstractJDBCProvider implements DocumentJDBCProvider, SecurityAware {

	@Override
	public PreparedStatement getInsertWithName(Connection conn, Document doc) throws Exception {

		PreparedStatement ps =
			conn.prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write )"
				+ "values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		int paramOrdinal = 1;
		ps.setString(paramOrdinal++, doc.getName());
		ps.setInt(paramOrdinal++, 1);
		ps.setInt(paramOrdinal++, 1);
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, doc.getBodyClass());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setString(paramOrdinal++, doc.getID());
		ps.setString(paramOrdinal++, getUserID());
		ps.setString(paramOrdinal++, doc.getScope().toString());
		ps.setString(paramOrdinal++, doc.getDisplayName());
		ps.setString(paramOrdinal++, doc.getDescription());
		ps.setCharacterStream(paramOrdinal++, new StringReader(doc.getBody().toString()),
			doc.getBody().toString().length());
		ps.setString(paramOrdinal++, getUserID()); //lud_user
		ps.setString(paramOrdinal++, doc.getGrouping());
		ps.setString(paramOrdinal++, defaultPermission(doc.getReadPerm()));
		ps.setString(paramOrdinal++, defaultPermission(doc.getWritePerm()));

		return ps;
	}

	@Override
	public PreparedStatement getInsertWithoutName(Connection conn, Document doc) throws Exception {

		PreparedStatement ps =
			conn.prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write )"
				+ "values ( CAST((select  ? || TRIM(CAST(CAST ( count(*) + 1  AS CHAR(30)) as VARCHAR(30))) as cnt " +
				"from document where type = ? and author = ? and tenant_id = ?) as VARCHAR(100)) " +
				",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		//set name basis
		String nameBasis = doc.getDocumentType().toString() + "_" + doc.getAuthor();

		int paramOrdinal = 1;
		ps.setString(paramOrdinal++, nameBasis + "_");
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, getUserID());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setInt(paramOrdinal++, 1);
		ps.setInt(paramOrdinal++, 1);
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, doc.getBodyClass());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setString(paramOrdinal++, doc.getID());
		ps.setString(paramOrdinal++, getUserID());
		ps.setString(paramOrdinal++, doc.getScope().toString());
		ps.setString(paramOrdinal++, doc.getDisplayName());
		ps.setString(paramOrdinal++, doc.getDescription());
		ps.setCharacterStream(paramOrdinal++, new StringReader(doc.getBody().toString()),
			doc.getBody().toString().length());
		ps.setString(paramOrdinal++, getUserID()); //lud_user
		ps.setString(paramOrdinal++, doc.getGrouping());
		ps.setString(paramOrdinal++, defaultPermission(doc.getReadPerm()));
		ps.setString(paramOrdinal++, defaultPermission(doc.getWritePerm()));

		return ps;
	}



	@Override
	public PreparedStatement getInsertVerbatim(Connection conn, Document doc) throws Exception {

		PreparedStatement ps =
			conn.prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author )"
				+ "values ( ?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		int paramOrdinal = 1;

		ps.setString(paramOrdinal++, doc.getName());
		ps.setInt(paramOrdinal++, doc.getVersion());

		if (doc.getIsCurrent()) {
			ps.setInt(paramOrdinal++, 1);
		}
		else {
			ps.setInt(paramOrdinal++, 0);
		}

		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, doc.getBodyClass());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setString(paramOrdinal++, doc.getID());

		ps.setString(paramOrdinal++, doc.getAuthor());

		ps.setString(paramOrdinal++, doc.getScope().toString());

		ps.setString(paramOrdinal++, doc.getDisplayName());
		ps.setString(paramOrdinal++, doc.getDescription());

		ps.setCharacterStream(paramOrdinal++, new StringReader(doc.getBody().toString()),
			doc.getBody().toString().length());

		ps.setString(paramOrdinal++, doc.getVersionAuthor()); //lud_user

		return ps;
	}



	protected String getUpdateSQL() {
		return "insert into DOCUMENT (version, name, is_current, type, body_class, id, tenant_id,  " +
			"author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write, deleted)"
			+ "values ( (select max(version) + 1 from DOCUMENT where type =? and name = ? and tenant_id = ?) " +
			",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
	}

	@Override
	public PreparedStatement getUpdate(Connection conn, Document doc) throws Exception {

		PreparedStatement ps =
			conn.prepareStatement(getUpdateSQL());

		int paramOrdinal = 1;
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, doc.getName());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setString(paramOrdinal++, doc.getName());
		ps.setInt(paramOrdinal++, 1);
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, doc.getBodyClass());
		ps.setString(paramOrdinal++, doc.getID());
		ps.setString(paramOrdinal++, getTenantID());
		ps.setString(paramOrdinal++, doc.getAuthor());
		ps.setString(paramOrdinal++, doc.getScope().toString());
		ps.setString(paramOrdinal++, doc.getDisplayName());
		ps.setString(paramOrdinal++, doc.getDescription());
		ps.setCharacterStream(paramOrdinal++, new StringReader(doc.getBody().toString()),
			doc.getBody().toString().length());
		ps.setString(paramOrdinal++, getUserID()); //lud_user stays even in overridden write permission scenario
		ps.setString(paramOrdinal++, doc.getGrouping());
		ps.setString(paramOrdinal++, defaultPermission(doc.getReadPerm()));
		ps.setString(paramOrdinal++, defaultPermission(doc.getWritePerm()));


		if (doc.getDeleted()) {
			ps.setInt(paramOrdinal++, 1);
		}
		else {ps.setInt(paramOrdinal++, 0);}

		return ps;
	}

	@Override
	public PreparedStatement getUpdateOldVersions(Connection conn, Document doc) throws Exception {
		PreparedStatement ps = conn.prepareStatement("update DOCUMENT set IS_CURRENT = 0 where name = ? and type = ? and tenant_id = ? ");
		int paramOrdinal = 1;
		ps.setString(paramOrdinal++, doc.getName());
		ps.setString(paramOrdinal++, doc.getDocumentType().toString());
		ps.setString(paramOrdinal++, getTenantID());
		return ps;
	}

	@Override
	public PreparedStatement getDeletePermanent(Connection conn, DocumentEnvelope envelope) throws Exception {

		PreparedStatement ps = conn.prepareStatement("delete from DOCUMENT where type = ? and name = ? and tenant_id = ?");
		ps.setString(1, envelope.getDocumentType().toString());
		ps.setString(2, envelope.getName());
		ps.setString(3, getTenantID());
		return ps;

	}

	@Override
	public PreparedStatement getSelectDocument(Connection conn, String guid) throws SQLException {
		return getSelectDocument(conn, guid, false);
	}

	@Override
	public PreparedStatement getSelectDocument(Connection conn, String guid, boolean includeDeleted) throws SQLException {
		String sql = getSelectClauseForDocument() + " where id = ? ";

		//restrict to documents that aren't deleted if requested
		if (!includeDeleted) {
			sql = sql + "and deleted=0";
		}

		return applyReadRestrictionsAndPrepare(conn, sql, guid);
	}

	@Override
	public PreparedStatement getSelectCurrentDocument(Connection conn, com.aw.document.DocumentType type, String name) throws SQLException {
		String sql = getSelectClauseForDocument() + " where is_current=1 and  type=? and name=? and deleted=0"; //latest version first
		sql = applyReadRestrictions(sql) + " order by version desc"; //latest version first
		return prepare(conn, sql, type.toString(), name);
	}

	@Override
	public PreparedStatement getSelectDocument(Connection conn, String author, DocumentType type, String name)
		throws SQLException {
		String sql = getSelectClauseForDocument() + " where deleted=0 AND author=? and type=? and name=? and deleted=0 ";
		return applyReadRestrictionsAndPrepare(conn, sql, author, type.toString(), name);
	}

	@Override
	public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type) throws SQLException {
		String sql = getSelectClause(false) + " where type=? and is_current=1 and deleted=0";
		return applyReadRestrictionsAndPrepare(conn, sql, type.toString());
	}

	@Override
	public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type, String grouping)
		throws SQLException {
		String sql = getSelectClause(false) + " where type = ? and grouping = ? and is_current = 1 and deleted=0 ";
		return applyReadRestrictionsAndPrepare(conn, sql, type.toString(), grouping);
	}

	public PreparedStatement getSelectCurrentEnvelopes(Connection conn, DocumentType type, Collection<Tag> tags) throws SQLException {
		return getTaggedDocuments(conn, type, tags, false);
	}

	@Override
	public PreparedStatement getSelectCurrentDocument(Connection conn, DocumentType type, Collection<Tag> tags) throws SQLException {
		return getTaggedDocuments(conn, type, tags, true);
	}

	PreparedStatement getTaggedDocuments(Connection conn, DocumentType type, Collection<Tag> tags, boolean body) throws SQLException {

		String sql = getSelectClause(body) + " where type=? and is_current=1 and deleted=0";
		Object[] params = new Object[] { type.toString() };

		if (tags != null && !tags.isEmpty()) {
			sql =  sql + " AND id IN (SELECT doc_id FROM document_tag WHERE tag IN (" + StringUtils.repeat("?", ", ", tags.size()) + ")) ";

			//convert list of tags to list of strings
			List<Object> paramList = tags.stream().map(t -> t.toString()).collect(Collectors.toList());
			paramList.add(0, type.toString());
			params = paramList.toArray();
		}

		return applyReadRestrictionsAndPrepare(conn, sql, params);
	}

	public String getSelectClauseForDocument() {
		return getSelectClause(true);
	}

	public String getSelectClauseForEnvelope() {
		return getSelectClause(false);
	}

	public String getSelectClause(boolean body) {
		String strBody = "body,";
		if (!body) {
			strBody = "";
		}

		return "select tenant_id, id, name, type, body_class, version, is_current, " + strBody + " description, display_name, author, scope, version_date,  version_author, " +
			" deleted, grouping, perm_read, perm_write, dependencies_guid, dependencies_name " +
			" from DOCUMENT";
	}

	/**
	 * Gets the string representation for a document permission or the default val if null.
	 * @param docPermission The doc permission to stringify.
	 * @return The string value for the doc permission.
	 */
	private String defaultPermission(DocumentPermission docPermission) {
		if (docPermission == null) {
			return DocumentPermission.ALL.toString();
		} else {
			return docPermission.toString();
		}
	}

	@Override
	public PreparedStatement getDocumentExists(Connection conn, DocumentType docType, String docName) throws SQLException {
		String sql = "select count(*) as cnt from document where type = ? and name = ? ";
		return applyReadRestrictionsAndPrepare(conn, sql, docType.name().toLowerCase(), docName);
	}

	@Override
	public PreparedStatement getDocumentExists(Connection conn, String docID) throws SQLException {
		String sql = "select count(*) as cnt from document where id = ? ";
		return applyReadRestrictionsAndPrepare(conn, sql, docID);
	}

	/**
	 * apply read restrictions to the sql statement
	 *
	 * @param sql
	 * @return
	 */
	protected String applyReadRestrictions(String sql) {

		//if not the system tenant, apply standard read restrictions
		if (!getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {

			//where we are the author or read permissions allow all - eventually we will add custom read/write permissions
			sql = sql + " AND ( author = ? OR  perm_read = 'all' OR author='aw' )";

		}

		return sql;

	}

	/**
	 * Apply user visibility restrictions and return a prepared statement
	 *
	 * @param sql The SQL statement to restrict
	 * @param paramsIn The params to restrict
	 * @return a SQL statement with the appropriate visibility restrictions applied
	 * @throws Exception
	 */
	protected PreparedStatement applyReadRestrictionsAndPrepare(Connection conn, String sql, Object... params) throws SQLException {

		sql = applyReadRestrictions(sql);

		return prepare(conn, sql, params);

	}

	/**
	 * prepare the statement given the sql and parameters
	 *
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {

		//create the statement
		PreparedStatement ps = conn.prepareStatement(sql);

		//set parameters
		int index = 1;

		//apply parameters if provided
		if (params != null) {

			for (Object param : params) {
				ps.setObject(index++, param);
			}

		}

		//if not the system tenant, apply standard read restrictions
		if (!getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {

			//where we are the author or read permissions allow all - eventually we will add custom read/write permissions
			ps.setObject(index++, getUserID());

		}

		return ps;

	}


}
