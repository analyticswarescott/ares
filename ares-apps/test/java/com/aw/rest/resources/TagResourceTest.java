package com.aw.rest.resources;

import com.aw.common.Tag;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * @author jhaight
 */
@RunWith(MockitoJUnitRunner.class)
public class TagResourceTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Mock
	private Provider<DocumentHandler> documentHandlerProvider;
	@Mock
	private DocumentHandler documentHandler;

	@InjectMocks
	private TagResource tagResource;

	@Before
	public void setup() {
		when(documentHandlerProvider.get()).thenReturn(documentHandler);
	}

	@Test
	public void getTagsTest() {
		Collection<Tag> tags = new ArrayList<>();
		tags.add(Tag.valueOf("tag1"));
		tags.add(Tag.valueOf("tag2"));
		tags.add(Tag.valueOf("tag3"));
		when(documentHandler.getTags()).thenReturn(tags);
		Collection<Tag> returnTags = tagResource.get();
		assertSame(tags, returnTags);
	}

	@Test
	public void getDocumentTypeTagsTest() {
		Collection<Tag> tags = new ArrayList<>();
		tags.add(Tag.valueOf("tag1"));
		tags.add(Tag.valueOf("tag2"));
		tags.add(Tag.valueOf("tag3"));
		when(documentHandler.getTagsForDocumentType(DocumentType.SIMPLE_RULE)).thenReturn(tags);
		Collection<Tag> returnTags = tagResource.get("simple_rule");
		assertSame(tags, returnTags);
	}

	@Test
	public void getDocumentTypeTagsNullTypeTest() {
		expect400Error();
		tagResource.get(null);
	}

	@Test
	public void getDocumentTypeTagsEmptyTypeTest() {
		expect400Error();
		tagResource.get("");
	}

	@Test
	public void getDocumentTypeTagsInvalidTypeTest() {
		expect400Error();
		tagResource.get("not_a_thing");
	}

	private void expect400Error() {
		exception.expect(WebApplicationException.class);
		exception.expectMessage("HTTP 400 Bad Request");
	}
}
