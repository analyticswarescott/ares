package com.aw.unity;

import com.aw.common.Tag;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentException;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentListener;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;
import com.aw.unity.json.JSONDataTypeRepositoryTest;
import com.aw.unity.json.JSONFieldRepositoryTest;
import com.aw.unity.json.JSONUnityInstance;
import com.aw.unity.json.JSONUnityInstanceTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestDocumentHandler implements DocumentHandler {

	public static final String TENANT = "test_tenant";

	public TestDocumentHandler() throws Exception {
		 m_dataTypeRepository = new Document(DOC_DATA_TYPE_REPOSITORY);
		 m_fields = new Document(DOC_FIELDS);
		 m_instance = new Document(DOC_INSTANCE);
	}

	@Override
	public DocumentEnvelope createDocument(Document doc) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public DocumentEnvelope updateDocument(Document doc) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public void updateListeners(Document doc, Operation op) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public void applyDocumentVerbatim(Document document) throws DocumentException {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public boolean isBootstrapping() {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public void setBootstrapping(boolean isBootstrapping) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Document getDocument(DocumentType docType, String docName) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Document getDocument(String docID) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public boolean documentExists(DocumentType docType, String docName) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public boolean documentExists(String docID) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Document deleteDocument(DocumentType docType, String docName) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public ArrayList<Document> getDocumentsOfType(DocumentType docType) throws Exception {
		switch (docType) {
			case UNITY_DATATYPE_REPO : return new ArrayList<Document>(Arrays.asList(m_dataTypeRepository));
			case UNITY_FIELD_REPO : return new ArrayList<Document>(Arrays.asList(m_fields));
			case UNITY_INSTANCE : return new ArrayList<Document>(Arrays.asList(m_instance));
			default: throw new UnsupportedOperationException("Document type " + docType + " not supported in this test document handler");
		}
	}

	@Override
	public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Collection<Document> getAllTenants() throws Exception {
		Collection<Document> ret = new ArrayList<Document>();
		ret.add(new Document(TENANT));
		return ret;
	}


	@Override
	public boolean tenantExists(String tid) throws Exception {
		return tid.equals(TENANT);
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType type, String grouping) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public void addListener(DocumentListener listener) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public boolean removeListener(DocumentListener listener) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public void initForTenant(boolean doBootstrap) throws Exception {
	}

	@Override
	public Collection<Tag> getTags() {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	@Override
	public DocumentTree getDocumentTree(DocumentType documentType) {
		throw new UnsupportedOperationException("Not implemented in test");
	}

	//the documents
	private Document m_dataTypeRepository;
	private Document m_fields;
	private Document m_instance;

	/**
{
  "display_name": "test",
  "description" : "test",
  "author": "aw",
  "body":
*/
	private static final String DOC_HEADER =
			"{\n" +
			"  \"display_name\": \"test\",\n" +
			"  \"description\" : \"test\",\n" +
			"  \"author\": \"aw\",\n" +
			"  \"body\": ";
	private static final String DOC_FOOTER =
			"}";

	//our data type repository
	private static final String DOC_DATA_TYPE_REPOSITORY =
			DOC_HEADER +
			JSONDataTypeRepositoryTest.JSON +
			DOC_FOOTER;

	//our field repository
	private static final String DOC_FIELDS =
			DOC_HEADER +
			JSONFieldRepositoryTest.FIELD_JSON +
			DOC_FOOTER;

	//our unity instance
	private static final String DOC_INSTANCE =
			DOC_HEADER +
			JSONUnityInstanceTest.JSON +
			",\"body_class\": \"" + JSONUnityInstance.class.getName() + "\"" +
			DOC_FOOTER;

	@Override
	public DocumentEnvelope deleteGroup(DocumentType forValue, String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	public DBMgr getDBMgr() { return this.dbMgr;  }
	public void setDBMgr(DBMgr dbMgr) { this.dbMgr = dbMgr; }
	private DBMgr dbMgr;

}
