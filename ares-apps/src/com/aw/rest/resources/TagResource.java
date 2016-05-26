package com.aw.rest.resources;

import com.aw.common.Tag;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author jhaight
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/tags")
public class TagResource extends RestResourceBase {

	private Provider<DocumentHandler> documentHandler;

	@Inject
	public TagResource(Provider<DocumentHandler> documentHandler) {
		this.documentHandler = documentHandler;
	}

	/**
	 * Gets all the tags on all documents
	 * @return all tags
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Tags",
		notes = "Gets all the tags on all documents",
		response = Tag.class,
		responseContainer = "Collection")
	public Collection<Tag> get() {
		return documentHandler.get().getTags();
	}

	/**
	 * Gets all tags on a single document type
	 * @param type The document type
	 * @return All the tags on a single document type
     */
	@GET
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Document Type Tags",
		notes = "Gets all tags on a single document type",
		response = Tag.class,
		responseContainer = "Collection")
	public Collection<Tag> get(@ApiParam(value = "the document type", required = true) @PathParam("type") String type) {
		DocumentType docType;
		try {
			docType = DocumentType.forValue(type);
		} catch (Exception e) {
			throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity("invalid document type").build());
		}
		return documentHandler.get().getTagsForDocumentType(docType);
	}
}
