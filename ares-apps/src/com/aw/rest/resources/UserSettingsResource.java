package com.aw.rest.resources;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.aw.common.rest.security.SecurityAware;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.util.Statics;

import io.swagger.annotations.Api;

/**
 * Handles endpoints for managing a user's settings.  This API hides the fact that the user's settings
 * are stored in a {@link Document} body.
 */
@Api
@Path(Statics.REST_VERSION + "/user_settings")
public class UserSettingsResource extends DocumentResourceBase implements SecurityAware {

	@Inject @com.google.inject.Inject
	public UserSettingsResource(Provider<DocumentHandler> docs) {
		super(docs);
	}

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.USER_SETTINGS;
	}

    @Override
	protected void checkUserPermission(@Nullable String documentName, DocumentOperation operation) throws WebApplicationException {
		super.checkUserPermission(documentName, operation);
		if (!getUserID().equals(documentName) || operation == DocumentOperation.DELETE) {
			throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
		}
	}

}
