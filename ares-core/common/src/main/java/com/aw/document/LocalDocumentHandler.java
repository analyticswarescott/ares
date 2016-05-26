package com.aw.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.aw.common.Tag;
import com.aw.common.tenant.Tenant;
import com.aw.document.exceptions.DocumentException;
import com.aw.document.exceptions.DocumentNotFoundException;
import com.aw.util.ListMap;


/**
 * A local document handler that uses the local file system for its data. Some operations are not available
 * or not implemented the way a true document handler would implement them. This handler is meant for special
 * cases like testing and bootstrapping of the platform.
 *
 *
 */
public class LocalDocumentHandler extends AbstractDocumentHandler {

	public LocalDocumentHandler(String basePath, Object... docs) throws Exception {
		super(null, null); //no manager needs as updates aren't allowed

		m_basePath = basePath;

		for (int x=0; x<docs.length; x+=2) {

			//get the next type+name
			DocumentType type = (DocumentType)docs[x];
			String name = String.valueOf(docs[x+1]);

			Document doc = new Document(read(type, name));
			doc.setName(name);

			//store our docs
			m_docs.add(type, doc);

		}

	}

	@Override
	public Tenant getTenant() {
		return Tenant.SYSTEM;
	}

	/**
	 * Load all docs from the base path. The rules are:
	 *
	 * <li>Directory names are document types
	 * <li>Filenames without extension are document name
	 *
	 * @param basePath
	 */
	public LocalDocumentHandler(String basePath) throws Exception {
		super(null, null); //no manager needs as updates aren't allowed

		m_basePath = basePath;
	}

	@Override
	public void applyDocumentVerbatim(Document document) throws DocumentException {
	}

	@Override
	public List<Document> getDocumentsOfType(DocumentType docType) throws Exception {
		checkLoaded(docType);
		return m_docs.get(docType);
	}

	@Override
	public Collection<Document> getDocumentsOfTypeWithGrouping(DocumentType type, String grouping) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public List<Document> getDocumentsOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfType(DocumentType docType) throws Exception {
		return new ArrayList<>(m_docs.get(docType));
	}

	@Override
	public List<DocumentEnvelope> getEnvelopesOfTypeWithTags(DocumentType docType, Collection<Tag> tags) {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	protected Document createDocumentInDB(Document doc, boolean forceName) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	protected Document updateDocumentInDB(Document doc) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public void reset() throws Exception {
		throw new UnsupportedOperationException("local document handler is read only");
	}

	@Override
	protected Document deleteDocumentFromDBPermanent(DocumentType docType, String docName) throws Exception {
		return null;
	}

	@Override
	public  Document getDocument(DocumentType docType, String docName) throws Exception {
		setBootstrapping(true); //disable consistency checking
		return super.getDocument(docType, docName);
	}

	@Override
	protected Document getDocumentFromDB(DocumentType docType, String docName) throws Exception {
		checkLoaded(docType);
		for (Document doc : m_docs.get(docType)) {
			if (doc.getName().equals(docName)) {
				return doc;
			}
		}
		throw new DocumentNotFoundException("could not find document " + docType + "/" + docName);
	}

	@Override
	public boolean documentExists(DocumentType docType, String docName) throws Exception {
		checkLoaded(docType);
		for (Document doc : m_docs.get(docType)) {
			if (doc.getName().equals(docName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean documentVersionExists(DocumentType docType, String docName, int version) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public boolean documentExists(String docID) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public Document getDocument(String docID) throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public Collection<Document> getAllTenantsFromDB() throws Exception {
		return null;
	}

	@Override
	public Collection<Document> getAllTenants() throws Exception {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public Collection<Tag> getTags() {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public Collection<Tag> getTagsForDocumentType(DocumentType documentType) {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	@Override
	public DocumentTree getDocumentTree(DocumentType documentType) {
		throw new UnsupportedOperationException("not supported by local document handler");
	}

	private void checkLoaded(DocumentType type) throws IOException {

		//if already there, no-op
		if (m_docs.containsKey(type)) {
			return;
		}

		//else read them all in
		readAll(type, new File(m_basePath, type.name().toLowerCase()));

		//set to empty list if nothing was loaded
		if (!m_docs.containsKey(type)) {
			m_docs.put(type, new ArrayList<Document>());
		}

	}

	//load all documents of this type from this directory
	private void readAll(DocumentType type, File parent) throws IOException {
		for (File f : parent.listFiles()) {

			try {

				//load the doc
				Document doc = new Document(FileUtils.readFileToString(f));

				//set the type
				doc.setType(type);

				//set the name
				doc.setName(FilenameUtils.removeExtension(f.getName()));

				//add the doc
				m_docs.add(type, doc);

			} catch (Exception e) {
				throw new IOException("while reading type=" + type + " file=" + f.getName(), e);
			}

		}
	}

	/**
	 * @return The contents of the default config doc given the type and name
	 */
	private String read(DocumentType type, String name) throws IOException {

		checkLoaded(type);

		//read from the test json topic that should contain the data we need
		String json = FileUtils.readFileToString(new File(m_basePath + type.name().toLowerCase() + File.separator + name + ".json"));
		return json;

	}

	private String m_basePath = ".";

	//test documents
	private ListMap<DocumentType, Document> m_docs = new ListMap<>();

}
