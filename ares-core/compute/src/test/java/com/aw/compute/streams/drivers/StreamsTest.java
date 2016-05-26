package com.aw.compute.streams.drivers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.util.ListMap;

public class StreamsTest {

	@Test
	public void test() throws Exception {

		DocumentHandler handler = new TestDocumentHandler();

		List<Document> streamDocs = handler.getDocumentsOfType(DocumentType.STREAM_GLOBAL);

		Streams s1 = new Streams(new ListMap<>());
		Streams s2 = new Streams(new ListMap<>());

		for (Document streamDoc : streamDocs) {

			s1.getTenantToStreams().add(Tenant.forId("1"), streamDoc.getBodyAsObject());
			s2.getTenantToStreams().add(Tenant.forId("1"), streamDoc.getBodyAsObject());

		}

		assertEquals(s1, s2);

	}

}
