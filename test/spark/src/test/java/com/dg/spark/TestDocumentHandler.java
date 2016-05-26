package com.aw.spark;

import com.aw.document.DocumentHandlerLocal;

public class TestDocumentHandler extends DocumentHandlerLocal {

	static final String RELATIVE_DOCS_PATH = "../../dg2/conf/defaults";

	public TestDocumentHandler() throws Exception {
		super(RELATIVE_DOCS_PATH);
	}

}
