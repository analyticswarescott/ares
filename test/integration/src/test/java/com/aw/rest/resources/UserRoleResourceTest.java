package com.aw.rest.resources;

/**
 * @author jhaight
 */
public class UserRoleResourceTest extends DocumentResourceTestBase {
	@Override
	protected String getBasePath() {
		return com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_roles";
	}
}
