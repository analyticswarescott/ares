package com.aw.document.body;

import static org.junit.Assert.*;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.document.Document;
import com.aw.document.TestDocumentHandler;
import com.aw.document.TestDocuments;

public class BodyInstanceTest {



	@Test
	public void bodyInstance() throws Exception {

		Document doc_typed = new Document(TestDocuments.TEST_DOC_DEF_TYPED);
        Document doc_inittable = new Document(TestDocuments.TEST_DOC_DEF_INITIALIZABLE);
		Document doc_generic = new Document(TestDocuments.TEST_DOC_DEF_GENERIC);

		Object ot = doc_typed.getBodyAsObject();
        Object oi = doc_inittable.getBodyAsObject(new TestDocumentHandler());
        Object og = doc_generic.getBodyAsObject();

		assertEquals("body type inference not correct", "com.aw.document.body.TestBodyClass",  ot.getClass().getName());
        assertEquals("body type inference not correct", "com.aw.document.body.TestBodyClassInitializable",  oi.getClass().getName());
		assertEquals("generic type not as expected",  JSONObject.class, og.getClass());

	}

}
