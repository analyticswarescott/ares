package com.aw.document;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.aw.common.Tag;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.document.exceptions.DocumentException;
import com.aw.document.exceptions.DocumentSecurityException;

public class AbstractDocumentHandlerTest {

	@Test(expected=DocumentSecurityException.class)
	public void testTenantsDontMatchOnUpdate() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		DocHandler handler = spy(new DocHandler());

		//existing from another tenant
		Document existing = mock(Document.class);
		doReturn("msp").when(existing).getTenantID();
		doReturn("current_user").when(existing).getAuthor();

		//new version from this tenant
		Document updated = mock(Document.class);

		//wire things up
		doReturn("current_tenant").when(handler).getTenantID();
		doReturn("current_user").when(handler).getUserID();
		doReturn(existing).when(handler).getExisting(any(Document.class));

		//make sure we get the tenant exception
		handler.prepareForUpdate(updated);

	}

	private class DocHandler extends AbstractDocumentHandler {

		public DocHandler() {
			super(null, null);
		}

		@Override
		public List<Document> getDocumentsOfType(DocumentType docType) throws Exception {
			return null;
		}

		@Override
		public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
			return null;
		}

		@Override
		public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
			return null;
		}

		@Override
		public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
			return null;
		}

		@Override
		public Collection<Tag> getTags() {
			return null;
		}

		@Override
		public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
			return null;
		}

		@Override
		public DocumentTree getDocumentTree(DocumentType documentType) {
			return null;
		}

		@Override
		public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType type, String grouping)
				throws Exception {
			return null;
		}

		@Override
		public void applyDocumentVerbatim(Document document) throws DocumentException {
		}

		@Override
		public void reset() throws Exception {
		}

		@Override
		protected Document createDocumentInDB(Document doc, boolean forceName) throws Exception {
			return null;
		}

		@Override
		protected Document updateDocumentInDB(Document doc) throws Exception {
			return null;
		}

		@Override
		protected Document deleteDocumentFromDBPermanent(DocumentType docType, String docName) throws Exception {
			return null;
		}

		@Override
		protected Document getDocumentFromDB(DocumentType docType, String docName) throws Exception {
			return null;
		}

		@Override
		public boolean documentExists(DocumentType docType, String docName) throws Exception {
			return false;
		}

		@Override
		public boolean documentVersionExists(DocumentType docType, String docName, int version) throws Exception {
			return false;
		}

		@Override
		public boolean documentExists(String docID) throws Exception {
			return false;
		}

		@Override
		public Document getDocument(String docID) throws Exception {
			return null;
		}

		@Override
		public Collection<Document> getAllTenantsFromDB() throws Exception {
			return null;
		}

	}
}
