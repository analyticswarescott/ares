package com.aw.rest.resources;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.aw.common.MockProvider;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;

import uk.co.datumedge.hamcrest.json.SameJSONAs;

/**
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class GroupResourceTest {

	private String REMOVABLE_PATTERN = "\"version_date[^,]+,";

	@Mock
	private MockProvider<DocumentHandler> documentHandlerProvider;

	@Mock
	private DocumentHandler documentHandler;

	@InjectMocks
	private GroupResource groupResource = new GroupResource(documentHandlerProvider);


	@Test
	public void testGetDocumentTree () throws Exception {

		String tenant = "0";

		String inputFile = "com/aw/document/document_groups.json";
		DocumentTree expectedDocumentTree = DocumentTestUtility.buildDocumentTreeFromFile(inputFile);

		DocumentHandler dh = mock(DocumentHandler.class);

		when(documentHandlerProvider.get()).thenReturn(dh);
		when(dh.getDocumentTree(DocumentType.WORKSPACE)).thenReturn(expectedDocumentTree);

		String documentType = "workspace";

		DocumentTree documentTree = new WorkspaceResource(documentHandlerProvider).getDocumentTree(tenant);

		String actualJSONString = documentTree.toString().replaceAll(REMOVABLE_PATTERN, "");

		String expectedJSON = expectedDocumentTree.toJSON().toString().replaceAll(REMOVABLE_PATTERN, "");

		assertThat(actualJSONString, SameJSONAs.sameJSONAs(expectedJSON).allowingAnyArrayOrdering());
	}


}