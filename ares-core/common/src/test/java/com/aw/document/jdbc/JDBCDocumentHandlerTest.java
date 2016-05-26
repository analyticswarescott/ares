package com.aw.document.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aw.platform.restcluster.RestCluster;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.aw.common.Tag;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;
import com.aw.document.exceptions.DocumentDefaultNotFoundException;
import com.aw.document.exceptions.DocumentIDNotFoundException;
import com.aw.document.exceptions.DocumentNameNotFoundException;
import com.aw.platform.PlatformMgr;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Tests the logic in the {@link JDBCDocumentHandler} class.
 */
public class JDBCDocumentHandlerTest {

	private DocumentMgr mockManager = mock(DocumentMgr.class);
	private PlatformMgr mockPlatformMgr = mock(PlatformMgr.class);
	private SequencedDocumentJDBCProvider sqlProvider = mock(SequencedDocumentJDBCProvider.class);
	private SequencedJDBCDocumentHandler documentHandler = spy(new SequencedJDBCDocumentHandler(mockManager, mockPlatformMgr, sqlProvider, mock(RestCluster.class)));

	Connection conn = mock(Connection.class);
	DBMgr db = spy(new DBMgr(mockPlatformMgr, mock(JDBCProvider.class)));
	DocumentJDBCProvider prov;
	PreparedStatement ps;
	Document testDoc;

	@Before
	public void before() throws Exception {

		prov = mock(DocumentJDBCProvider.class);
		ps = mock(PreparedStatement.class);
		mockManager = mock(DocumentMgr.class);
		mockPlatformMgr = mock(PlatformMgr.class);
		sqlProvider = mock(SequencedDocumentJDBCProvider.class);
		documentHandler = spy(new SequencedJDBCDocumentHandler(mockManager, mockPlatformMgr, sqlProvider, mock(RestCluster.class)));

		stub(prov.getInsertWithName(any(), any())).toReturn(ps);
		stub(prov.getInsertWithoutName(any(), any())).toReturn(ps);
		stub(prov.getInsertVerbatim(any(), any())).toReturn(ps);
		stub(prov.getDocumentExists(any(), any(), any())).toReturn(ps);
		stub(conn.prepareStatement(anyString())).toReturn(ps);
		stub(prov.getSelectCurrentDocument(any(), any(), any(Collection.class))).toReturn(ps);
		stub(prov.getSelectCurrentDocument(any(), any(), any(String.class))).toReturn(ps);
		stub(prov.getDeletePermanent(any(), any())).toReturn(ps);
		stub(prov.getInsertVerbatim(any(), any())).toReturn(ps);
		stub(prov.getUpdateOldVersions(any(), any())).toReturn(ps);
		stub(prov.getUpdate(any(), any())).toReturn(ps);
		stub(prov.getSelectDocument(any(), any(), any(Boolean.class))).toReturn(ps);
		stub(prov.getSelectDocument(any(), any())).toReturn(ps);
		stub(prov.getSelectDocument(any(), any(), any(), any())).toReturn(ps);
		stub(prov.getDocumentExists(any(), any())).toReturn(ps);

	}

	@Test
	public void testGetDocumentTreeForType() throws Exception, IOException {
		final String REMOVABLE_PATTERN = "\"version_date[^,]+,";

		String inputFile = "com/aw/document/document_groups.json";
		String inputFileData = readFileFromResourcePath(inputFile);
		JSONArray docs = new JSONArray(inputFileData);

		List<DocumentEnvelope> documentEnvelopes = new ArrayList<>();
		List<DocumentEnvelope> documentGroupEnvelopes = new ArrayList<>();

		for (int i = 0; i < docs.length(); i++) {
			final DocumentEnvelope documentEnvelope = new DocumentEnvelope(docs.getJSONObject(i));
			if(documentEnvelope.getDocumentType().equals(DocumentType.WORKSPACE))
				documentEnvelopes.add(documentEnvelope);
			else
				documentGroupEnvelopes.add(documentEnvelope);
		}

		doReturn(documentEnvelopes).when(documentHandler).getEnvelopesOfType(DocumentType.WORKSPACE);
		doReturn(documentGroupEnvelopes).when(documentHandler).getEnvelopesOfType(DocumentType.DOCUMENT_GROUP);


		DocumentTree documentTree = documentHandler.getDocumentTree(DocumentType.WORKSPACE);

		String actualJSONString = documentTree.toJSON().replaceAll(REMOVABLE_PATTERN, "");

		assertTrue(documentTree.getDocuments().size() >= 28);
		assertTrue(documentTree.getGroups().size() == 7);
	}

    @Test
	public void testDML() throws Exception {

		 testDoc = new Document(DocumentType.USER_SETTINGS, "a_doc", "Testing", "1", "aw", new JSONObject());


		documentHandler.setDBMgr(db);
		documentHandler.sqlProvider = prov;


		doReturn(testDoc).when(documentHandler).getDocument(anyString());
		doReturn(testDoc).when(documentHandler).getDocument(any(DocumentType.class), anyString());

		doReturn(new Tenant("0")).when(documentHandler).getTenant();
		doReturn("0").when(documentHandler).getTenantID();
		doReturn(conn).when(db).getConnection(new Tenant("0"));

		//TODO: could improve coverage by creating result sets designed to map to SQL
		 //or at least making sure testResultSet returns a valid document and metadata
		when(ps.executeQuery()).thenReturn(getTestResultSet());
		when(ps.executeUpdate()).thenReturn(1);
		SecurityUtil.setThreadSystemAccess();

		documentHandler.documentExists(testDoc.getDocumentType(), testDoc.getName());
		documentHandler.getDocumentsOfType(DocumentType.TEST_TYPE);
		documentHandler.createDocumentInDB(testDoc, true);
		documentHandler.deleteDocumentFromDBPermanent(testDoc.getDocumentType(), testDoc.getName());

		doReturn(mock(PreparedStatement.class)).when(sqlProvider).getInsertOpSequnce(any(), any(), any(), any(Long.class));

		testDoc.setOpSequence(1);
		documentHandler.writeOpSequence(testDoc);
		documentHandler.getOpSequence(testDoc.getDocumentType().toString());
		documentHandler.getIdForOpSequence("foo", 1);
		documentHandler.applyDocumentVerbatim(testDoc);
		documentHandler.documentVersionExists(testDoc.getDocumentType(), testDoc.getName(), 1);

		documentHandler.getTagsForDocumentType(DocumentType.TEST_TYPE);



		// methods where the get will return an exception (TODO: manipulate result sets

		try {
			documentHandler.updateDocumentInDB(testDoc);
		}
		catch (DocumentIDNotFoundException nfi) {//expected
		 }


		try {
			documentHandler.getDocumentFromDB(testDoc.getDocumentType(), testDoc.getName());
		}
		catch(DocumentNameNotFoundException dnf) {//expected
		}
		catch (DocumentDefaultNotFoundException dd) {
			//expected
		}


		try {
			documentHandler.deleteDocument(testDoc.getDocumentType(), testDoc.getName());

		}
		catch(DocumentNameNotFoundException dnf) {
			//expected
		}


	}

	@Test
	public void testGets() throws Exception {

		testDoc = new Document(DocumentType.USER_SETTINGS, "a_doc", "Testing", "1", "aw", new JSONObject());


		documentHandler.setDBMgr(db);
		documentHandler.sqlProvider = prov;

		doReturn(ps).when(prov).getInsertWithName(conn, testDoc);
		doReturn(ps).when(prov).getInsertWithoutName(conn, testDoc);
		doReturn(ps).when(prov).getInsertVerbatim(conn, testDoc);

		stub(conn.prepareStatement(anyString())).toReturn(ps);
		stub(prov.getSelectDocument(any(), any())).toReturn(ps);

		doReturn(new Tenant("0")).when(documentHandler).getTenant();
		doReturn("0").when(documentHandler).getTenantID();
		doReturn(conn).when(db).getConnection(new Tenant("0"));

		//TODO: could improve coverage by creating result sets designed to map to SQL
		//or at least making sure testResultSet returns a valid document and metadata
		when(ps.executeQuery()).thenReturn(getTestResultSet());
		when(ps.executeUpdate()).thenReturn(1);

		try {
			documentHandler.getDocument("test");
		}
		catch (DocumentIDNotFoundException ex) {

		}

		assertFalse(documentHandler.documentExists("foo"));
		List<DocumentEnvelope> envelopes = documentHandler.getList(conn, DocumentType.TEST_TYPE, ps, DocumentEnvelope.class);
		assertNotNull(envelopes);
		assertEquals(0, envelopes.size());

	}

	@Test
	public void getTagsTest() throws Exception {
		documentHandler.setDBMgr(db);
		documentHandler.sqlProvider = prov;
		documentHandler.setTenant(new Tenant("0"));

		List<String> testTags = new ArrayList<String>();
		testTags.add("tag1");
		testTags.add("tag2");
		testTags.add("tag3");
		Iterator<String> iterator = testTags.iterator();

		Connection mockConnection = mock(Connection.class);
		doReturn(mockConnection).when(db).getConnection(new Tenant("0"));
		//when(db.getConnection(documentHandler.getTenant())).thenReturn(mockConnection);
		PreparedStatement mockStatement = mock(PreparedStatement.class);
		when(mockConnection.prepareStatement("SELECT tag FROM document_tag")).thenReturn(mockStatement);
		ResultSet mockResultSet = mock(ResultSet.class);
		when(mockStatement.executeQuery()).thenReturn(mockResultSet);
		when(mockResultSet.next()).thenAnswer(x -> iterator.hasNext());
		when(mockResultSet.getString("tag")).thenAnswer(x -> iterator.next());
		Collection<Tag> returnTags = documentHandler.getTags();
		assertEquals(3, returnTags.size());
		assertTrue(returnTags.contains(Tag.valueOf("tag1")));
		assertTrue(returnTags.contains(Tag.valueOf("tag2")));
		assertTrue(returnTags.contains(Tag.valueOf("tag3")));
	}

	@Test(expected=RuntimeException.class)
	public void getTagsThrowsExceptionTest() throws Exception {
		documentHandler.setDBMgr(db);
		documentHandler.sqlProvider = prov;
		documentHandler.setTenant(new Tenant("0"));

		List<String> testTags = new ArrayList<String>();
		testTags.add("tag1");
		testTags.add("tag2");
		testTags.add("tag3");
		Iterator<String> iterator = testTags.iterator();

		Connection mockConnection = mock(Connection.class);
		doReturn(mockConnection).when(db).getConnection(new Tenant("0"));
		//when(db.getConnection(documentHandler.getTenant())).thenReturn(mockConnection);
		PreparedStatement mockStatement = mock(PreparedStatement.class);
		when(mockConnection.prepareStatement("SELECT tag FROM document_tag")).thenReturn(mockStatement);
		when(mockStatement.executeQuery()).thenThrow(SQLException.class);
		Collection<Tag> returnTags = documentHandler.getTags();
	}

	private ResultSet getTestResultSet () {
		ResultSet rs =  new ResultSet() {

			@Override
			public boolean next() throws SQLException {
			/*	if (rowcnt == 0) {
					rowcnt++;
					return true;
				}*/
				return false;
			}

			@Override
			public void close() throws SQLException {

			}

			@Override
			public boolean wasNull() throws SQLException {
				return false;
			}

			@Override
			public String getString(int columnIndex) throws SQLException {
				return "dummy";
			}

			@Override
			public boolean getBoolean(int columnIndex) throws SQLException {
				return false;
			}

			@Override
			public byte getByte(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public short getShort(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public int getInt(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public long getLong(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public float getFloat(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public double getDouble(int columnIndex) throws SQLException {
				return 0;
			}

			@Override
			public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
				return null;
			}

			@Override
			public byte[] getBytes(int columnIndex) throws SQLException {
				return new byte[0];
			}

			@Override
			public Date getDate(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Time getTime(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Timestamp getTimestamp(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public InputStream getAsciiStream(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public InputStream getUnicodeStream(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public InputStream getBinaryStream(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public String getString(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public boolean getBoolean(String columnLabel) throws SQLException {
				return false;
			}

			@Override
			public byte getByte(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public short getShort(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public int getInt(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public long getLong(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public float getFloat(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public double getDouble(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
				return null;
			}

			@Override
			public byte[] getBytes(String columnLabel) throws SQLException {
				return new byte[0];
			}

			@Override
			public Date getDate(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Time getTime(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Timestamp getTimestamp(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public InputStream getAsciiStream(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public InputStream getUnicodeStream(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public InputStream getBinaryStream(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public SQLWarning getWarnings() throws SQLException {
				return null;
			}

			@Override
			public void clearWarnings() throws SQLException {

			}

			@Override
			public String getCursorName() throws SQLException {
				return null;
			}

			@Override
			public ResultSetMetaData getMetaData() throws SQLException {


				return new ResultSetMetaData() {
					@Override
					public int getColumnCount() throws SQLException {
						return 0;
					}

					@Override
					public boolean isAutoIncrement(int column) throws SQLException {
						return false;
					}

					@Override
					public boolean isCaseSensitive(int column) throws SQLException {
						return false;
					}

					@Override
					public boolean isSearchable(int column) throws SQLException {
						return false;
					}

					@Override
					public boolean isCurrency(int column) throws SQLException {
						return false;
					}

					@Override
					public int isNullable(int column) throws SQLException {
						return 0;
					}

					@Override
					public boolean isSigned(int column) throws SQLException {
						return false;
					}

					@Override
					public int getColumnDisplaySize(int column) throws SQLException {
						return 0;
					}

					@Override
					public String getColumnLabel(int column) throws SQLException {
						return null;
					}

					@Override
					public String getColumnName(int column) throws SQLException {
						return null;
					}

					@Override
					public String getSchemaName(int column) throws SQLException {
						return null;
					}

					@Override
					public int getPrecision(int column) throws SQLException {
						return 0;
					}

					@Override
					public int getScale(int column) throws SQLException {
						return 0;
					}

					@Override
					public String getTableName(int column) throws SQLException {
						return null;
					}

					@Override
					public String getCatalogName(int column) throws SQLException {
						return null;
					}

					@Override
					public int getColumnType(int column) throws SQLException {
						return 0;
					}

					@Override
					public String getColumnTypeName(int column) throws SQLException {
						return null;
					}

					@Override
					public boolean isReadOnly(int column) throws SQLException {
						return false;
					}

					@Override
					public boolean isWritable(int column) throws SQLException {
						return false;
					}

					@Override
					public boolean isDefinitelyWritable(int column) throws SQLException {
						return false;
					}

					@Override
					public String getColumnClassName(int column) throws SQLException {
						return null;
					}

					@Override
					public <T> T unwrap(Class<T> iface) throws SQLException {
						return null;
					}

					@Override
					public boolean isWrapperFor(Class<?> iface) throws SQLException {
						return false;
					}
				};

			}

			@Override
			public Object getObject(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Object getObject(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public int findColumn(String columnLabel) throws SQLException {
				return 0;
			}

			@Override
			public Reader getCharacterStream(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Reader getCharacterStream(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public boolean isBeforeFirst() throws SQLException {
				return false;
			}

			@Override
			public boolean isAfterLast() throws SQLException {
				return false;
			}

			@Override
			public boolean isFirst() throws SQLException {
				return false;
			}

			@Override
			public boolean isLast() throws SQLException {
				return false;
			}

			@Override
			public void beforeFirst() throws SQLException {

			}

			@Override
			public void afterLast() throws SQLException {

			}

			@Override
			public boolean first() throws SQLException {
				return false;
			}

			@Override
			public boolean last() throws SQLException {
				return false;
			}

			@Override
			public int getRow() throws SQLException {
				return 0;
			}

			@Override
			public boolean absolute(int row) throws SQLException {
				return false;
			}

			@Override
			public boolean relative(int rows) throws SQLException {
				return false;
			}

			@Override
			public boolean previous() throws SQLException {
				return false;
			}

			@Override
			public void setFetchDirection(int direction) throws SQLException {

			}

			@Override
			public int getFetchDirection() throws SQLException {
				return 0;
			}

			@Override
			public void setFetchSize(int rows) throws SQLException {

			}

			@Override
			public int getFetchSize() throws SQLException {
				return 0;
			}

			@Override
			public int getType() throws SQLException {
				return 0;
			}

			@Override
			public int getConcurrency() throws SQLException {
				return 0;
			}

			@Override
			public boolean rowUpdated() throws SQLException {
				return false;
			}

			@Override
			public boolean rowInserted() throws SQLException {
				return false;
			}

			@Override
			public boolean rowDeleted() throws SQLException {
				return false;
			}

			@Override
			public void updateNull(int columnIndex) throws SQLException {

			}

			@Override
			public void updateBoolean(int columnIndex, boolean x) throws SQLException {

			}

			@Override
			public void updateByte(int columnIndex, byte x) throws SQLException {

			}

			@Override
			public void updateShort(int columnIndex, short x) throws SQLException {

			}

			@Override
			public void updateInt(int columnIndex, int x) throws SQLException {

			}

			@Override
			public void updateLong(int columnIndex, long x) throws SQLException {

			}

			@Override
			public void updateFloat(int columnIndex, float x) throws SQLException {

			}

			@Override
			public void updateDouble(int columnIndex, double x) throws SQLException {

			}

			@Override
			public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

			}

			@Override
			public void updateString(int columnIndex, String x) throws SQLException {

			}

			@Override
			public void updateBytes(int columnIndex, byte[] x) throws SQLException {

			}

			@Override
			public void updateDate(int columnIndex, Date x) throws SQLException {

			}

			@Override
			public void updateTime(int columnIndex, Time x) throws SQLException {

			}

			@Override
			public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

			}

			@Override
			public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

			}

			@Override
			public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

			}

			@Override
			public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

			}

			@Override
			public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

			}

			@Override
			public void updateObject(int columnIndex, Object x) throws SQLException {

			}

			@Override
			public void updateNull(String columnLabel) throws SQLException {

			}

			@Override
			public void updateBoolean(String columnLabel, boolean x) throws SQLException {

			}

			@Override
			public void updateByte(String columnLabel, byte x) throws SQLException {

			}

			@Override
			public void updateShort(String columnLabel, short x) throws SQLException {

			}

			@Override
			public void updateInt(String columnLabel, int x) throws SQLException {

			}

			@Override
			public void updateLong(String columnLabel, long x) throws SQLException {

			}

			@Override
			public void updateFloat(String columnLabel, float x) throws SQLException {

			}

			@Override
			public void updateDouble(String columnLabel, double x) throws SQLException {

			}

			@Override
			public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

			}

			@Override
			public void updateString(String columnLabel, String x) throws SQLException {

			}

			@Override
			public void updateBytes(String columnLabel, byte[] x) throws SQLException {

			}

			@Override
			public void updateDate(String columnLabel, Date x) throws SQLException {

			}

			@Override
			public void updateTime(String columnLabel, Time x) throws SQLException {

			}

			@Override
			public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

			}

			@Override
			public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

			}

			@Override
			public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

			}

			@Override
			public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

			}

			@Override
			public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

			}

			@Override
			public void updateObject(String columnLabel, Object x) throws SQLException {

			}

			@Override
			public void insertRow() throws SQLException {

			}

			@Override
			public void updateRow() throws SQLException {

			}

			@Override
			public void deleteRow() throws SQLException {

			}

			@Override
			public void refreshRow() throws SQLException {

			}

			@Override
			public void cancelRowUpdates() throws SQLException {

			}

			@Override
			public void moveToInsertRow() throws SQLException {

			}

			@Override
			public void moveToCurrentRow() throws SQLException {

			}

			@Override
			public Statement getStatement() throws SQLException {
				return null;
			}

			@Override
			public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
				return null;
			}

			@Override
			public Ref getRef(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Blob getBlob(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Clob getClob(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Array getArray(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
				return null;
			}

			@Override
			public Ref getRef(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Blob getBlob(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Clob getClob(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Array getArray(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Date getDate(int columnIndex, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public Date getDate(String columnLabel, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public Time getTime(int columnIndex, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public Time getTime(String columnLabel, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
				return null;
			}

			@Override
			public URL getURL(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public URL getURL(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public void updateRef(int columnIndex, Ref x) throws SQLException {

			}

			@Override
			public void updateRef(String columnLabel, Ref x) throws SQLException {

			}

			@Override
			public void updateBlob(int columnIndex, Blob x) throws SQLException {

			}

			@Override
			public void updateBlob(String columnLabel, Blob x) throws SQLException {

			}

			@Override
			public void updateClob(int columnIndex, Clob x) throws SQLException {

			}

			@Override
			public void updateClob(String columnLabel, Clob x) throws SQLException {

			}

			@Override
			public void updateArray(int columnIndex, Array x) throws SQLException {

			}

			@Override
			public void updateArray(String columnLabel, Array x) throws SQLException {

			}

			@Override
			public RowId getRowId(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public RowId getRowId(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public void updateRowId(int columnIndex, RowId x) throws SQLException {

			}

			@Override
			public void updateRowId(String columnLabel, RowId x) throws SQLException {

			}

			@Override
			public int getHoldability() throws SQLException {
				return 0;
			}

			@Override
			public boolean isClosed() throws SQLException {
				return false;
			}

			@Override
			public void updateNString(int columnIndex, String nString) throws SQLException {

			}

			@Override
			public void updateNString(String columnLabel, String nString) throws SQLException {

			}

			@Override
			public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

			}

			@Override
			public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

			}

			@Override
			public NClob getNClob(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public NClob getNClob(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public SQLXML getSQLXML(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public SQLXML getSQLXML(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

			}

			@Override
			public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

			}

			@Override
			public String getNString(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public String getNString(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public Reader getNCharacterStream(int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public Reader getNCharacterStream(String columnLabel) throws SQLException {
				return null;
			}

			@Override
			public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

			}

			@Override
			public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

			}

			@Override
			public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

			}

			@Override
			public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

			}

			@Override
			public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

			}

			@Override
			public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

			}

			@Override
			public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

			}

			@Override
			public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

			}

			@Override
			public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

			}

			@Override
			public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

			}

			@Override
			public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

			}

			@Override
			public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

			}

			@Override
			public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

			}

			@Override
			public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

			}

			@Override
			public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

			}

			@Override
			public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

			}

			@Override
			public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

			}

			@Override
			public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

			}

			@Override
			public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

			}

			@Override
			public void updateClob(int columnIndex, Reader reader) throws SQLException {

			}

			@Override
			public void updateClob(String columnLabel, Reader reader) throws SQLException {

			}

			@Override
			public void updateNClob(int columnIndex, Reader reader) throws SQLException {

			}

			@Override
			public void updateNClob(String columnLabel, Reader reader) throws SQLException {

			}

			@Override
			public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
				return null;
			}

			@Override
			public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
				return null;
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}
		};

		return rs;
	}

	private String readFileFromResourcePath(String inputFile) throws IOException {

		URL url = Resources.getResource(inputFile);
		String nestedDocument = Resources.toString(url, Charsets.UTF_8);
		return nestedDocument;
	}
}