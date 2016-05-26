package com.aw.document;

import com.aw.util.Statics;

public class TestDocumentHandler extends LocalDocumentHandler {


	public TestDocumentHandler() throws Exception {
		super(Statics.RELATIVE_DOCS_PATH);
	}

	@Override
	public DocumentEnvelope updateDocument(Document doc) throws Exception {
		m_lastUpdate = doc;
		return doc;
	}

	public Document getLastUpdate() {
		return m_lastUpdate;
	}
	public Document m_lastUpdate;


}
