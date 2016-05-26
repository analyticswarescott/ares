package com.aw.document;

import static org.junit.Assert.assertEquals;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.Tag;

/**
 * Created by scott on 04/11/15.
 */
public class DocumentTest {

    @Test
    public void DocumentTest() throws Exception {

        Document doc_generic = new Document(TestDocuments.TEST_DOC_DEF_GENERIC);
		doc_generic.setOpSequence(1);

		Document newDoc = new Document(doc_generic.toString());
		assertEquals(1, newDoc.getOpSequence());
    }

	@Test
	public void testDocumentTags() throws Exception {

		final int numTags = 10;

		Document doc = new Document(DocumentType.PLATFORM, "test", "testing", "0", "test", new JSONObject());
		for (int i = 0; i < numTags; i++) {
			doc.addTag(Tag.valueOf("testing" + i));
		}
		assertEquals(numTags, doc.getTags().size());

		// Make sure all doc tags make it through the serialization/deserialization process
		Document serializedDoc = new Document(doc.toJSON());
		assertEquals(numTags, serializedDoc.getTags().size());

	}

	@Test
	public void testInitialize() throws Exception {

		Document doc = new Document();
		doc.setName("test name");
		doc.setDisplayName("test display name");
		BodyType object = new BodyType();
		doc.initialize(object);

		assertEquals("test name", object.boundName);
		assertEquals("test display name", object.boundDisplayName);

	}

	class BodyType {

		@DocName
		public String getBoundName() { return this.boundName;  }
		public void setBoundName(String boundName) { this.boundName = boundName; }
		private String boundName;

		@DocDisplayName
		public String getBoundDisplayName() { return this.boundDisplayName;  }
		public void setBoundDisplayName(String boundDisplayName) { this.boundDisplayName = boundDisplayName; }
		private String boundDisplayName;

	}

}
