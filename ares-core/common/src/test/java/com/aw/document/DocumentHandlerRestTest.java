package com.aw.document;

import com.aw.common.Tag;
import com.aw.common.inject.TestProvider;
import com.aw.platform.Platform;
import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentHandlerRestTest {

	@Mock
	Platform platform;

	@Spy
	DocumentHandlerRest documentHandlerRest = spy(new DocumentHandlerRest("0", new TestProvider<Platform>(platform)));

	@Test
	public void getTagsTest() throws Exception {
		doReturn("http://basepath/documents").when(documentHandlerRest).getBasePath();
		doReturn("[\"tag1\", \"tag2\", \"tag3\"]").when(documentHandlerRest).getString("http://basepath/documents/tags/list/all?body=true&tenant=0");
		Collection<Tag> returnTags = documentHandlerRest.getTags();
		assertEquals(3, returnTags.size());
		assertTrue(returnTags.contains(Tag.valueOf("tag1")));
		assertTrue(returnTags.contains(Tag.valueOf("tag2")));
		assertTrue(returnTags.contains(Tag.valueOf("tag3")));
	}

	@Test(expected = RuntimeException.class)
	public void getTagsThrowsExceptionTest() throws Exception {
		doReturn("http://basepath/documents").when(documentHandlerRest).getBasePath();
		doThrow(HttpException.class).when(documentHandlerRest).getString("http://basepath/documents/tags/list/all?body=true&tenant=0");
		documentHandlerRest.getTags();
	}

	@Test
	public void getTagsByDocumentTypeTest() throws Exception {
		doReturn("http://basepath/documents").when(documentHandlerRest).getBasePath();
		doReturn("[\"tag1\", \"tag2\", \"tag3\"]").when(documentHandlerRest).getString("http://basepath/documents/tags/list/simple_rule?body=true&tenant=0");
		Collection<Tag> returnTags = documentHandlerRest.getTagsForDocumentType(DocumentType.SIMPLE_RULE);
		assertEquals(3, returnTags.size());
		assertTrue(returnTags.contains(Tag.valueOf("tag1")));
		assertTrue(returnTags.contains(Tag.valueOf("tag2")));
		assertTrue(returnTags.contains(Tag.valueOf("tag3")));
	}

	@Test(expected = RuntimeException.class)
	public void getTagsByDocumentTypeWithExceptionTest() throws Exception {
		doReturn("http://basepath/documents").when(documentHandlerRest).getBasePath();
		doThrow(HttpException.class).when(documentHandlerRest).getString("http://basepath/documents/tags/list/simple_rule?body=true&tenant=0");
		documentHandlerRest.getTagsForDocumentType(DocumentType.SIMPLE_RULE);
	}
}