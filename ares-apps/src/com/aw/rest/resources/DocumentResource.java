package com.aw.rest.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import com.aw.common.Tag;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;
import com.aw.document.SequencedDocumentHandler;
import com.aw.document.action.Operation;
import com.aw.document.exceptions.DocumentNotFoundException;
import com.aw.rest.form.DocumentsBulkEditForm;
import com.aw.rest.validation.DocumentsBulkEditFormValidator;
import com.aw.utils.jaxrs.PATCH;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/documents")
public class DocumentResource extends RestResourceBase {
	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DocumentResource.class);

	private Provider<DocumentHandler> docs;
	private Provider<DocumentMgr> docMgr;

	@Inject
	public DocumentResource(Provider<DocumentHandler> docs, Provider<DocumentMgr> docMgr) {
		this.docs = docs;
		this.docMgr = docMgr;
	}

	/**
	 * @return All document of a given type. Returns the active version for each document name. If body=true, full bodies are returned - body defaults to false.
	 * @throws Exception If anything goes wrong
	 */
    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Document",
    notes = "Returns a list of the requested documents of a certain type, with or without body",
    response = DocumentEnvelope.class,
    responseContainer = "List")
    public List<? extends DocumentEnvelope> get(
    		@ApiParam(value = "the type of requested documents", required = true) @PathParam("type") String type,
	        @ApiParam(value = "whether or not the body is requeseted", required = false) @DefaultValue("false") @QueryParam("body") boolean body,
			@ApiParam(value = "the ID of the tenant to impersonate", required = false) @DefaultValue("0") @QueryParam("tenant") String tenantID
            ) throws Exception {


		Impersonation.impersonateTenant(tenantID);

		try {

			List<? extends DocumentEnvelope> ret = null;

			//if requesting a body, provide it
	    	if (body) {
	    		ret = this.docs.get().getDocumentsOfType(toType(type));
	    	}

	    	//else no body, just envelopes
	    	else {
	    		//visibility handled by the doc handler for this user
	    		ret = this.docs.get().getEnvelopesOfType(toType(type));
	    	}

	    	return ret;

		} finally {

			Impersonation.unImpersonate();

		}

    }

    /**
     * Get a specific document by name
     *
     * @param name The semantic name of the document
     * @return The envelope
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get specific document",
    notes = "Get a specific document by name",
    response = Document.class)
    public Document get(
    		@ApiParam(value = "the type of the requested document", required = true) @PathParam("type") String type,
    		@ApiParam(value = "the semantic name of the document", required = true) @PathParam("name") String name,
    		@ApiParam(value = "the ID of the tenant to impersonate") @DefaultValue("0") @QueryParam("tenant")
    		String tenantID
    		) throws Exception {

		boolean unimpersonate = Impersonation.impersonateTenant(tenantID);

		try {

    		//get the doc by semantic name
        	return this.docs.get().getDocument(toType(type), name);

    	} catch (DocumentNotFoundException e) {

    		//standard 404 situation
    		throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

    	} finally {

    		if (unimpersonate) {
    			Impersonation.unImpersonate();
    		}

    	}

    }

    /**
     * Get tags for a specific document type.  Added /list within the URI to disambiguate the resource from the
	 * /{doc}/{name} handler.
     */
    @GET
    @Path("/tags/list/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Document Type Tags",
    notes = "Get tags for a specific document type.  Added /list within the URI to disambiguate the resource from the /{doc}/{name} handler.",
    response = Tag.class,
    responseContainer = "Collection")
    public Collection<Tag> getDocumentTypeTags(@ApiParam() @PathParam("type") String type) throws Exception {
		if (type == null || type.length() == 0) {
			return Collections.emptyList();
		} else if (type.equals("all")) {
			return this.docs.get().getTags();
		} else {
			final DocumentType documentType = toType(type);
			return this.docs.get().getTagsForDocumentType(documentType);
		}
	}

    /**
     * Gets documents of a specific type, grouped by its grouping document.  Documents that do not have a grouping
	 * property will be the top-level document.  Those that do will be tied to a top-level group and can be found in
	 * the group_documents array.
     */
    @GET
    @Path("/{type}/tree")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a document tree from a document type",
    notes = "All elements in the created document tree is a document envelope",
    response = DocumentTree.class)
	public DocumentTree getDocumentTree(
			@ApiParam(value = "the document type for this tree", required = true) @PathParam("type") String type,
			@ApiParam(value = "the unique id for the tenant", required = true) @DefaultValue("0") @QueryParam("tenant") String tenantID) throws Exception {
		final DocumentType documentType = toType(type);
		return this.docs.get().getDocumentTree(documentType);
	}

    /**
     * Get a specific document by name
     *
     * @return The envelope
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
    	//TODO: create a tenant array for this call
    	return Response.status(200).entity(JSONUtils.objectToString(this.docs.get().getAllTenants())).build();
    }

    private DocumentType toType(String strType) throws WebApplicationException {
    	DocumentType type = null;
    	try {
        	type = DocumentType.forValue(strType);
        } catch (Exception e) {

    		//bad document type
        	throw new WebApplicationException(Response
        			 		.status(HttpStatus.BAD_REQUEST_400)
        			 		.entity("invalid document type: " + strType)
        			 		.build());

    	}
    	return type;
    }



	/**
	 * check if a tenant ID exists
	 *
	 * @return The envelope
	 * @throws Exception If anything goes wrong
	 */
	@GET
	@Path("/tenants/exists/{tenant_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response tenantExists(@PathParam("tenant_id") String tid) throws Exception {
		//TODO: create a tenant array for this call
		return Response.status(200).entity(this.docs.get().tenantExists(tid)).build();
	}



	/**
     * Update a document for the given tenant, or tenant 0 if tenant is not provided
     *
     * @param type
     * @param name
     * @param tenantID
     * @param document
     * @throws Exception
     */
    @PUT
    @Path("/{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DocumentEnvelope update(@PathParam("type") String type, @PathParam("name") String name,
    				   @DefaultValue("0") @QueryParam("tenant") String tenantID, String document) throws Exception {

		Impersonation.impersonateTenant(tenantID);

		try {

			Document doc = new Document(document);
			return this.docs.get().updateDocument(doc);

		} finally {

			Impersonation.unImpersonate();

		}

    }

	/**
     * Create a document for the given tenant, or tenant 0 if tenant is not provided
     *
     * @param type
     * @param name
     * @param tenantID
     * @param document
     * @throws Exception
     */
    @POST
    @Path("/{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DocumentEnvelope create(@PathParam("type") String type, @PathParam("name") String name,
    				   @DefaultValue("0") @QueryParam("tenant") String tenantID, String document) throws Exception {

		Impersonation.impersonateTenant(tenantID);

		try {

			Document doc = new Document(document);
			return this.docs.get().createDocument(doc);

		} finally {

			Impersonation.unImpersonate();

		}

    }





	/**
	 * accept and apply a mirror operation from a peer
	 *
	 * @param tenant
	 * @param operation
	 * @param document
	 * @throws Exception
	 */
	@POST
	@Path("/mirror/{operation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response applyMirrorOperation(@QueryParam("tenant") String tenantID,
								  @PathParam("operation") String operation, String document) throws Exception {

		Impersonation.impersonateTenant(tenantID);

		try {

			//ensure we have this doc handler (DB) locally -- in case we missed a tenant mirror, which will cause chatter but no harm
			Tenant t = new Tenant(tenantID);
			//add a doc handler but do not bootstrap it ever
			this.docMgr.get().addDocHandler(t, false);

			Document doc = new Document(document);

			DocumentHandler handler = this.docs.get();
			if (handler instanceof SequencedDocumentHandler) {

				//mirror if supported
				((SequencedDocumentHandler)this.docs.get()).acceptMirrorUpdate(doc, Operation.valueOf(operation));

			} else {

				throw new Exception("document mirroring not supported by the document store on this node");

			}

			return Response.ok("mirrored").build();

		} finally {

			Impersonation.unImpersonate();

		}

	}





	/**
	 * Gets a document using op sequence key to allow peers to sync
	 * @param type
	 * @param name
	 * @param tenantID
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("/{op_sequence_key}/{op_sequence}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document getBySequence(@PathParam("op_sequence_key") String op_sequence_key, @PathParam("op_sequence") Long op_sequence, @DefaultValue("0") @QueryParam("tenant") String tenantID) throws Exception {

		boolean unimpersonate = Impersonation.impersonateTenant(tenantID);

		try {

			DocumentHandler docs = this.docs.get();
			if (docs instanceof SequencedDocumentHandler) {

				//get the document by sequence if supported
				return ((SequencedDocumentHandler)docs).getDocumentBySequence(op_sequence_key, op_sequence);

			} else {

				throw new Exception("document sequencing not supported by the configuration database on this node");

			}

		} catch (DocumentNotFoundException e) {

			//standard 404 situation
			throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

		} finally {

			if (unimpersonate) {
				Impersonation.unImpersonate();
			}

		}

	}



	/**
	 * Allows properties of multiple docs to be edited simultaneously.
	 * @param type The document type for this request.
	 * @param bulkEditForm The doc names and properties to edit.
	 * @return An OK response if everything succeeds.
	 * @throws Exception If invalid or documents don't exist.
     */
	@PATCH
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response bulkEditDocs(@PathParam("type") String type, DocumentsBulkEditForm bulkEditForm) throws Exception {

		final DocumentType documentType = DocumentType.forValue(type);

		// Validate the form submission
		new DocumentsBulkEditFormValidator().validate(bulkEditForm);

		// No validation errors, edit each document
		for (String docName : bulkEditForm.getDocumentNames()) {
			final Document document = this.docs.get().getDocument(documentType, docName);

			if (bulkEditForm.isRemoveGrouping()) {
				document.setGrouping(null);
			}
			if (StringUtils.isNotBlank(bulkEditForm.getGrouping())) {
				document.setGrouping(bulkEditForm.getGrouping());
			}
			if (bulkEditForm.getTagsToRemove() != null) {
				bulkEditForm.getTagsToRemove().forEach(t -> document.removeTag(Tag.valueOf(t)));
			}
			if (bulkEditForm.getTags() != null) {
				bulkEditForm.getTags().forEach(t -> document.addTag(Tag.valueOf(t)));
			}
			if (bulkEditForm.getReadPermission() != null) {
				document.setReadPerm(bulkEditForm.getReadPermission());
			}
			if (bulkEditForm.getWritePermission() != null) {
				document.setWritePerm(bulkEditForm.getWritePermission());
			}
			this.docs.get().updateDocument(document);

		}

		return Response.ok().build();
	}


	/**
     * Delete a document
     *
     * @param type
     * @param name
     * @param tenantID
     * @param document
     * @throws Exception
     */
    @DELETE
    @Path("/{type}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DocumentEnvelope delete(@PathParam("type") String type, @PathParam("name") String name,
    				   @DefaultValue("0") @QueryParam("tenant") String tenantID) throws Exception {

		Impersonation.impersonateTenant(tenantID);

		try {

			return this.docs.get().deleteDocument(DocumentType.forValue(type), name);

		} finally {

			Impersonation.unImpersonate();

		}

    }

}
