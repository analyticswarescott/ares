package com.aw.rest.resources;

/**
 * @author jhaight
 */
public class SimpleRuleResourceTest extends DocumentResourceTestBase {
	private SimpleRuleResource resource;

	@Override
	protected void initializeDocumentResource() {
		resource = new SimpleRuleResource(documentHandlerProvider);
	}

	@Override
	protected DocumentResourceBase getDocumentResource() {
		return resource;
	}
}
