package com.aw;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.LocalDocumentHandler;
import com.aw.util.Statics;

import java.io.File;

public class TestDocumentHandler extends LocalDocumentHandler {


	public TestDocumentHandler() throws Exception {


			super(Statics.getRDP());

	}

}
