package com.aw.rest.resources;

import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import io.swagger.annotations.Api;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Path;

/**
 * @author jhaight
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/user_roles")
public class UserRoleResource extends DocumentResourceBase {

	@Inject
	public UserRoleResource(Provider<DocumentHandler> docs) {
		super(docs);
	}

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.USER_ROLE;
	}
}
