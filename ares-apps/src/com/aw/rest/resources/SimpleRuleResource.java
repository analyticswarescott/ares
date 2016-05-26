package com.aw.rest.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;

import io.swagger.annotations.Api;

@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/simple_rules")
public class SimpleRuleResource extends DocumentResourceBase {

	@Inject @com.google.inject.Inject
	public SimpleRuleResource(Provider<DocumentHandler> docs) {
		super(docs);
	}

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.SIMPLE_RULE;
	}

}
