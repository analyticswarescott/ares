package com.aw.rest.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;

import io.swagger.annotations.Api;

/**
 * Workspace API
 *
 *
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/workspaces")
public class WorkspaceResource extends DocumentResourceBase {

	@Inject @com.google.inject.Inject
	public WorkspaceResource(Provider<DocumentHandler> docs) {
		super(docs);
	}

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.WORKSPACE;
	}

}
