/*
package com.aw.document.jdbc.derby;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.Tag;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.scope.ResourceScope;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.document.security.DocumentPermission;

public class DerbyJDBCProviderTest {

	SequencedDerbyJDBCProvider provider;
	Connection conn;
	PreparedStatement pStmt;
	Document doc;

	@Before
	public void before() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		provider = new SequencedDerbyJDBCProvider();
		conn = mock(Connection.class);
		pStmt = mock(PreparedStatement.class);
		doReturn(pStmt).when(conn).prepareStatement(any(String.class));

		JSONObject body = new JSONObject();
		doc = mock(Document.class);
		doReturn(ResourceScope.ALL).when(doc).getScope();
		doReturn(DocumentType.FILTER).when(doc).getDocumentType();
		doReturn("name").when(doc).getName();
		doReturn(body).when(doc).getBody();
		doReturn(DocumentPermission.ALL).when(doc).getReadPerm();
		doReturn(DocumentPermission.ALL).when(doc).getWritePerm();

	}

	@Test
	public void getSelectCurrentDocumentTags() throws Exception {

		provider.getSelectCurrentDocument(conn, DocumentType.FILTER, Arrays.asList(Tag.valueOf("tag1"), Tag.valueOf("tag2")));

		verify(conn).prepareStatement("select tenant_id, id, name, type, body_class, version, is_current, body, description, display_name, author, scope, version_date,  version_author,  deleted, grouping, perm_read, perm_write, dependencies_guid, dependencies_name  from DOCUMENT where type=? and is_current=1 and deleted=0 AND id IN (SELECT doc_id FROM document_tag WHERE tag IN (?, ?)) ");
		verify(pStmt).setObject(1, DocumentType.FILTER.toString());
		verify(pStmt).setObject(2, "tag1");
		verify(pStmt).setObject(3, "tag2");

	}

	@Test
	public void getInsertWithName() throws Exception {

		provider.getInsertWithName(conn, doc);

		verify(conn).prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write )"
                + "values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		//verify prepared statement - check some things
		int index = 1;
		verify(pStmt).setString(index++, "name");
		verify(pStmt).setInt(index++, 1);
		verify(pStmt).setInt(index++, 1);
		verify(pStmt).setString(index++, DocumentType.FILTER.toString());
		verify(pStmt).setString(index++, null);
		verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
		verify(pStmt).setString(index++, doc.getID());
		verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_UID);
		verify(pStmt).setString(index++, doc.getScope().toString());
		verify(pStmt).setString(index++, doc.getDisplayName());

	}

	@Test
	public void getInsertWithoutName() throws Exception {

        //set name basis
        String nameBasis = doc.getDocumentType().toString() + "_" + doc.getAuthor();

        provider.getInsertWithoutName(conn, doc);

        verify(conn).prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write )"
                + "values ( CAST((select  ? || TRIM(CAST(CAST ( count(*) + 1  AS CHAR(30)) as VARCHAR(30))) as cnt " +
                "from document where type = ? and author = ? and tenant_id = ?) as VARCHAR(100)) " +
                ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		//verify prepared statement calls
		int index = 1;
        verify(pStmt).setString(index++, nameBasis + "_");
        verify(pStmt).setString(index++, doc.getDocumentType().toString());
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_UID);
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
        verify(pStmt).setInt(index++, 1);
        verify(pStmt).setInt(index++, 1);
        verify(pStmt).setString(index++, doc.getDocumentType().toString());
        verify(pStmt).setString(index++, doc.getBodyClass());
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
        verify(pStmt).setString(index++, doc.getID());
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_UID);
        verify(pStmt).setString(index++, doc.getScope().toString());
        verify(pStmt).setString(index++, doc.getDisplayName());
        verify(pStmt).setString(index++, doc.getDescription());
        index++;
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_UID); //lud_user
        verify(pStmt).setString(index++, doc.getGrouping());

	}

	@Test
	public void getInsertVerbatim() throws Exception {

		provider.getInsertVerbatim(conn, doc);

		int index = 1;

		verify(conn).prepareStatement("insert into DOCUMENT (name, version, is_current, type, body_class, tenant_id, id,  author, scope, display_name, description, body, version_author )"
				+ "values ( ?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		verify(pStmt).setString(index++, doc.getName());
		verify(pStmt).setInt(index++, doc.getVersion());

		if (doc.getIsCurrent()) {
			verify(pStmt).setInt(index++, 1);
		}
		else {
			verify(pStmt).setInt(index++, 0);
		}

		verify(pStmt).setString(index++, doc.getDocumentType().toString());
		verify(pStmt).setString(index++, doc.getBodyClass());
		verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
		verify(pStmt).setString(index++, doc.getID());

		verify(pStmt).setString(index++, doc.getAuthor());
		verify(pStmt).setString(index++, doc.getScope().toString());

		verify(pStmt).setString(index++, doc.getDisplayName());
		verify(pStmt).setString(index++, doc.getDescription());

		index++;

		verify(pStmt).setString(index++, doc.getVersionAuthor()); //lud_user

	}

	@Test
	public void getUpdate() throws Exception {

		PreparedStatement stmt = provider.getUpdate(conn, doc);

		assertSame(pStmt, stmt);

		verify(conn).prepareStatement("insert into DOCUMENT (version, name, is_current, type, body_class, id, tenant_id,  " +
                        "author, scope, display_name, description, body, version_author, grouping, perm_read, perm_write, deleted)"
                        + "values ( (select max(version) + 1 from DOCUMENT where type =? and name = ? and tenant_id = ?) " +
                        ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

        int index = 1;
        verify(pStmt).setString(index++, doc.getDocumentType().toString());
        verify(pStmt).setString(index++, doc.getName());
		verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
		verify(pStmt).setString(index++, doc.getName());
        verify(pStmt).setInt(index++, 1);
        verify(pStmt).setString(index++, doc.getDocumentType().toString());
        verify(pStmt).setString(index++, doc.getBodyClass());
		verify(pStmt).setString(index++, doc.getID());
		verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);
		verify(pStmt).setString(index++, doc.getAuthor());
		verify(pStmt).setString(index++, doc.getScope().toString());
        verify(pStmt).setString(index++, doc.getDisplayName());
        verify(pStmt).setString(index++, doc.getDescription());
        index++;
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_UID); //lud_user stays even in overridden write permission scenario
        verify(pStmt).setString(index++, doc.getGrouping());
        index++;
        index++;

		if (doc.getDeleted()) {
			verify(pStmt).setInt(index++, 1);
		}
		else {verify(pStmt).setInt(index++, 0);}

	}

	@Test
	public void getUpdateOldVersions() throws Exception {

		PreparedStatement stmt = provider.getUpdateOldVersions(conn, doc);

		assertSame(pStmt, stmt);

		verify(conn).prepareStatement("update DOCUMENT set IS_CURRENT = 0 where name = ? and type = ? and tenant_id = ? ");

		int index = 1;
        verify(pStmt).setString(index++, doc.getName());
        verify(pStmt).setString(index++, doc.getDocumentType().toString());
        verify(pStmt).setString(index++, Tenant.SYSTEM_TENANT_ID);


	}

	@Test
	public void getDeletePermanent() throws Exception {

		PreparedStatement stmt = provider.getDeletePermanent(conn, doc);
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement("delete from DOCUMENT where type = ? and name = ? and tenant_id = ?");

		verify(pStmt).setString(1, doc.getDocumentType().toString());
    	verify(pStmt).setString(2, doc.getName());
    	verify(pStmt).setString(3, Tenant.SYSTEM_TENANT_ID);

	}

	@Test
	public void getSelectDocument_conn_guid_includeDeleted() throws Exception {

		PreparedStatement stmt = provider.getSelectDocument(conn, "1234", true);
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClauseForDocument() + " where id = ? ");

	}

	@Test
	public void getSelectDocument_conn_guid_excludeDeleted() throws Exception {

		PreparedStatement stmt = provider.getSelectDocument(conn, "1234", false);
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClauseForDocument() + " where id = ? and deleted=0");

	}

	@Test
	public void getSeelctCurrentDocument_conn_type_name() throws Exception {

		PreparedStatement stmt = provider.getSelectCurrentDocument(conn, DocumentType.FILTER, "my name");
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClauseForDocument() + " where is_current=1 and  type=? and name=? and deleted=0 order by version desc");

	}

	@Test
	public void getSeelctCurrentDocument_conn_type_name_non_system() throws Exception {

		//mock non-system tenant
		provider = spy(provider);
		doReturn("1").when(provider).getTenantID();

		PreparedStatement stmt = provider.getSelectCurrentDocument(conn, DocumentType.FILTER, "my name");
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClauseForDocument() + " where is_current=1 and  type=? and name=? and deleted=0 AND ( author = ? OR  perm_read = 'all' OR author='aw' ) order by version desc");

	}

	@Test
	public void getInsertOpSequence() throws Exception {

		PreparedStatement stmt = provider.getInsertOpSequnce(conn, "doc_id", "seq_key", 123L);
		assertSame(pStmt, stmt);

		int index = 1;
		verify(pStmt).setString(index++, "doc_id");
		verify(pStmt).setString(index++, "seq_key");
		verify(pStmt).setLong(index++, 123L);

	}

	@Test
	public void getSelectDocument_conn_author_type_name() throws Exception {

		PreparedStatement stmt = provider.getSelectDocument(conn, "author", DocumentType.FILTER, "test name");
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClauseForDocument() + " where deleted=0 AND author=? and type=? and name=? and deleted=0 ");

	}

	@Test
	public void getSelectCurrentEnvelopes() throws Exception {

		PreparedStatement stmt = provider.getSelectCurrentEnvelopes(conn, DocumentType.FILTER);
		assertSame(pStmt, stmt);

		verify(conn).prepareStatement(provider.getSelectClause(false) + " where type=? and is_current=1 and deleted=0");

	}

	@Test
	public void getSelectForDocument() {

		SequencedDerbyJDBCProvider derby = new SequencedDerbyJDBCProvider();
		assertTrue("body not included in document get clause: " + derby.getSelectClauseForDocument(), derby.getSelectClauseForDocument().toLowerCase().matches(".*\\,\\s*body\\s*\\,.*"));

	}

	@Test
	public void getSelectForEnvelope() {

		SequencedDerbyJDBCProvider derby = new SequencedDerbyJDBCProvider();
		assertFalse("body included in envelope get clause: " + derby.getSelectClauseForEnvelope(), derby.getSelectClauseForEnvelope().toLowerCase().contains(".*,\\s*body\\s*,.*"));

	}


}
*/
