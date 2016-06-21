package com.aw.platform.nodes;

import static org.junit.Assert.assertEquals;

import com.aw.util.Statics;
import org.junit.Test;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;

public class ConfigTemplateTest {

/*	@Test
	public void testFromDocument() throws Exception {

		System.setProperty(EnvironmentSettings.Setting.CONF_DIRECTORY.name(), Statics.RELATIVE_DOCS_PATH);
		TestDocumentHandler docs = new TestDocumentHandler();
		Document doc = docs.getDocument(DocumentType.CONFIG_3P, "postgresql_conf");
		ConfigTemplate template = doc.getBodyAsObject(ConfigTemplate.class);
		assertEquals("config_db/postgresql.conf", template.getFilePath());

	}*/

}
