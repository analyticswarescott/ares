package com.aw;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentMgr;
import com.aw.document.LocalDocumentHandler;
import com.aw.util.Statics;

import java.io.File;

public class TestDocumentHandler extends LocalDocumentHandler {

	public static final String CONF_PATH = "../../conf";
	public static final String PROD_DOCS_PATH = CONF_PATH + "/" + DocumentMgr.DEFAULTS_DIR;
	public static final String UNIT_TEST_DOCS_PATH = "./src/test/resources/docs";

	public TestDocumentHandler() throws Exception {


			super(Statics.getRDP());

	}

}
