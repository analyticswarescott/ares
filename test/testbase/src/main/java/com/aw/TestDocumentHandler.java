package com.aw;

import com.aw.document.LocalDocumentHandler;
import com.aw.util.Statics;

public class TestDocumentHandler extends LocalDocumentHandler {


	public TestDocumentHandler() throws Exception {
		super(Statics.RELATIVE_DOCS_PATH);
	}

}
