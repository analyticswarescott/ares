/*
package com.aw.tools;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;

import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.platform.PlatformMgr;

public class DocumentPrepopulator {

	public static void main(String[] args) throws Exception {

		String filePath = args[0];

		if(filePath == null ) {
			System.out.println("A file path is needed for document group creation.");
		}

		File file = new File(filePath);

		if(!file.exists() || file.isDirectory()) {
			System.out.println("A valid file is needed for document group creation.");
		}

		System.out.println("Populating default document groups");

		DocumentPrepopulator documentPrepopulator = new DocumentPrepopulator();

		documentPrepopulator.prepopulateDocuments(file);

		System.out.println("Done populating document groups!");
	}


	private void prepopulateDocuments(File file) throws Exception {

		String documentGroups = FileUtils.readFileToString(file);

		System.out.println(documentGroups);

		PlatformMgr platformMgr = new PlatformMgr();
		platformMgr.initialize();

		System.setProperty("DG_REPORTING_SERVICE_BASE_URL", "http://localhost:8080/");
		DocumentHandler documentHandler = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, platformMgr.getPlatform());

		JSONArray jsonArray = new JSONArray(documentGroups);
		for(int i = 0; i < documentGroups.length(); i++) {
			Document document  = new Document(jsonArray.getJSONObject(i));
			documentHandler.createDocument(document);
		}

	}

}
*/
