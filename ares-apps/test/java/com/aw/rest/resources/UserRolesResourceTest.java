package com.aw.rest.resources;

/**
 * @author jhaight
 */
public class UserRolesResourceTest extends DocumentResourceTestBase {

	private UserRoleResource resource;

	@Override
	protected void initializeDocumentResource() {
		resource = new UserRoleResource(documentHandlerProvider);
	}

	@Override
	protected DocumentResourceBase getDocumentResource() {
		return resource;
	}
}
