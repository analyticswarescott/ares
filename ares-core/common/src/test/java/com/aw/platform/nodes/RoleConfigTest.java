package com.aw.platform.nodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;

public class RoleConfigTest {

	@Test
	public void testDocument() throws Exception {

		TestDocumentHandler docs = new TestDocumentHandler();
		Document doc = docs.getDocument(DocumentType.CONFIG_3P, "postgresql_conf");
		ConfigTemplate template = doc.getBodyAsObject(ConfigTemplate.class);
		assertEquals("config_db/postgresql.conf", template.getFilePath());

	}

}
