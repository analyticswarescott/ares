package com.aw.rest.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;

import io.swagger.annotations.Api;

/**
 * HUD api
 *
 *
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/huds")
public class HUDResource extends DocumentResourceBase {

	@Inject
	public HUDResource(Provider<DocumentHandler> docs) {
		super(docs);
	}

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.HUD;
	}

}
