package com.aw.rest.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Provider;
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

import com.aw.common.util.JSONUtils;
import com.aw.utils.jaxrs.PATCH;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentTree;
import com.aw.document.DocumentType;
import com.aw.document.exceptions.DocumentNotFoundException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Common document resource operations - extend this class if you are implementing standard CRUD
 * operations for a document type from the DocumentHandler.
 *
 *
 */
public abstract class DocumentResourceBase extends RestResourceBase {

	protected Provider<DocumentHandler> docs;

	public DocumentResourceBase(Provider<DocumentHandler> docs) {
		this.docs = docs;
	}

    /**
     * @return All known documents of the relevant type
     * @throws Exception If anything goes wrong
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Documents",
    notes = "Returns all documents of a type",
    response = DocumentEnvelope.class,
    responseContainer = "List")
    public List<DocumentEnvelope> get() throws Exception {
		checkUserPermission(null, DocumentOperation.READ);

    	//visibility handled by the doc handler for this user
    	return this.docs.get().getEnvelopesOfType(getDocumentType());

    }

	/**
	 * Get all documents for the documentType
	 *
	 * @param tenantID The tenant ID
	 */
	@GET
    @Path("/tree")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a document tree from a document type",
    notes = "All elements in the created document tree is a document envelope",
    response = DocumentTree.class)
	public DocumentTree getDocumentTree(
			@ApiParam(value = "the unique id for the tenant", required = true) @DefaultValue("0") @QueryParam("tenant") String tenantID
	) throws Exception {
		checkUserPermission(null, DocumentOperation.READ);

		final DocumentType documentType = getDocumentType();
		DocumentHandler dh = this.docs.get();
		return dh.getDocumentTree(documentType);
	}


	/**
     * Get a specific document by name
     *
     * @param name The semantic name of the document
     * @return The envelope
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/{name : [^\\/]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Document",
    notes = "Returns a document with the given name of a type",
    response = Document.class)
    public Document get(
    		@ApiParam(value = "the name of the document to return", required = true) @PathParam("name") String name
	) throws Exception {
		checkUserPermission(name, DocumentOperation.READ);

    	try {

    		//get the doc by semantic name
			return this.docs.get().getDocument(getDocumentType(), name);

    	} catch (DocumentNotFoundException e) {

    		//standard 404 situation
    		throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

    	}

    }

    /**
     * Create a new document
     *
     * @param payload The document json
     * @return The envelope of the created document
     * @throws Exception If anything goes wrong
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Document",
    notes = "Adds a new document from the added document",
    response = DocumentEnvelope.class)
    public DocumentEnvelope add(@ApiParam(value = "the document to add", required = true) String payload) throws Exception {

		checkUserPermission(null, DocumentOperation.CREATE);
		//check the payload
    	checkPayload(payload);

    	try {

    		//get the document
    		Document document = toDocument(payload);

    		//just pass it on
        	return this.docs.get().createDocument(document);

    	} catch (JSONException e) {

    		//if a json parsing exception occurs, we'll blame it on the client
    		throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity(e.getMessage()).build());

    	}

    }

    /**
     * Update the document
     */
    @PUT
    @Path("/{name : [^\\/]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Document",
    notes = "Updates a document with the given name",
    response = DocumentEnvelope.class)
    public DocumentEnvelope update(
    		@ApiParam(value = "the name of the document to update", required = true) @PathParam("name") String name,
    		@ApiParam(value = "the updated document", required = true) String payload
    		) throws Exception {
		checkUserPermission(name, DocumentOperation.UPDATE);
    	try {

    		//build the document instance
    		Document document = toDocument(payload);

    		//force name to the one from the path - the path dictates the document name not the payload
    		document.setName(name);

    		//now update it
        	return this.docs.get().updateDocument(document);

    	} catch (DocumentNotFoundException e) {

    		//if a document isn't found it's a standard 404
    		throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

    	} catch (JSONException e) {

    		//if a json parsing exception occurs, we'll blame it on the client and return the message
    		throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity(e.getMessage()).build());

    	}


    }

    /**
     * Delete a document by name
     */
    @DELETE
    @Path("/{name : [^\\/]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Document",
    notes = "Deletes the document with the given name",
    response = DocumentEnvelope.class)
    public DocumentEnvelope delete(
    		@ApiParam(value = "the name of the document to delete", required = true) @PathParam("name") String name
    		) throws Exception {
		checkUserPermission(name, DocumentOperation.DELETE);

    	try {

    		//now update it
        	return this.docs.get().deleteDocument(getDocumentType(), name);

    	} catch (DocumentNotFoundException e) {

    		//if a document isn't found it's a standard 404
    		throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

    	}
    }

	/**
	 * Patch one field in the document
	 * @param name The name of the document
	 * @param replace Whether or not to replace entire object or just overlay
	 * @param payload JSON payload
	 * @return The document envelope
	 * @throws Exception If anything goes wrong
	 */
	@PATCH
	@Path("/{name : [^\\/]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Patch Document",
		notes = "Patches the document with the given name",
		response = DocumentEnvelope.class)
	public DocumentEnvelope patch(
		@ApiParam(value = "the name of the document to patch", required = true) @PathParam("name") String name,
		@ApiParam(value = "replace entire object, or just overlay", required = false) @DefaultValue("false") @QueryParam("replace") boolean replace,
		@ApiParam(value = "a JSON payload") String payload
	) throws Exception {
		return patch(name, null, replace, payload);
	}

	/**
	 * Patch one field in the document
	 * @param name The name of the document
	 * @param path The path of the JSON object in the form of /a/path/to/parameter
	 * @param replace Whether or not to replace entire object or just overlay
	 * @param payload JSON payload
	 * @return The document envelope
	 * @throws Exception If anything goes wrong
     */
	@PATCH
	@Path("/{name : [^\\/]+}/{path : [^\\/][^?]*}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Patch Document",
		notes = "Patches the document with the given name",
		response = DocumentEnvelope.class)
	public DocumentEnvelope patch(
		@ApiParam(value = "the name of the document to patch", required = true) @PathParam("name") String name,
		@ApiParam(value = "the path of the JSON object in the form of /a/path/to/parameter", required = false) @PathParam("path") String path,
		@ApiParam(value = "replace entire object, or just overlay", required = false) @DefaultValue("false") @QueryParam("replace") boolean replace,
		@ApiParam(value = "a JSON payload") String payload
	) throws Exception {
		checkUserPermission(name, DocumentOperation.UPDATE);

		checkPath(path, replace);
		if (path != null && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1); // remove last character
		}
		try {
			Document document = docs.get().getDocument(getDocumentType(), name);
			checkPayload(payload);
			Object patchObject = getObjectFromJsonString(payload, replace);
			Document updatedDocument;
			if (path != null && path.length() != 0) {
				updatedDocument = changeJsonField(document, path, patchObject, replace);
			} else {
				updatedDocument = overlayTopLevelDocument(document, (JSONObject)patchObject);
			}
			return docs.get().updateDocument(updatedDocument);
		} catch (DocumentNotFoundException e) {
			throw new WebApplicationException(e, HttpStatus.NOT_FOUND_404);
		} catch (JSONException | JsonMappingException e) {
			throw new WebApplicationException(e, HttpStatus.BAD_REQUEST_400);
		}
	}

    /**
     * Build a document from json, performing the necessary checks
     *
     * @param payload The raw json string
     * @return the document
     * @throws Exception If anything goes wrong
     */
    protected Document toDocument(String payload) throws Exception {

    	//first check the payload
    	checkPayload(payload);

    	//create the document
		Document ret;
		try {
			ret = new Document(payload);
		}
		catch (JSONException | IOException e) {
			throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity("Malformed JSON").build());
		}

		//check it
		checkType(ret.getDocumentType());

    	//everything looks good, return it
    	return ret;

    }

    //basic check of the payload json
    protected void checkPayload(String payload) throws WebApplicationException {

    	//make sure there's a payload at least
    	if (payload == null || payload.length() == 0) {
    		throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity("document content missing from request").build());
    	}

    }

    /**
     * Make sure the document type is correct
     * @param type The document type
     * @throws WebApplicationException If the document type is wrong
     */
		protected void checkType(DocumentType type) throws WebApplicationException {

			//make sure the document type is correct at least
			if (type != getDocumentType()) {
				throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity(
						"document type wrong, expected: " + getDocumentType().name().toLowerCase() +
								", found: " + (type == null ? "null" : type.name().toLowerCase()))
						.build());
			}

		}

	private void checkPath(String path, boolean update) throws WebApplicationException {
		if (update && (path == null || path.length() == 0)) {
			throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400)
				.entity("Cannot replace top level document").build());
		}
	}

	private Document overlayTopLevelDocument(Document document, JSONObject patchObject) throws Exception {
		JSONObject jsonDocument = new JSONObject(document.toJSON());
		JSONUtils.overlay(patchObject, jsonDocument);
		return new Document(jsonDocument);
	}

	private Object getObjectFromJsonString(String jsonString, boolean replace) throws Exception {
		Object newObject;
		if (replace && jsonString.startsWith("\"") && jsonString.length() > 1) {
			newObject = jsonString.substring(1, jsonString.length() -1);
		} else if (replace && jsonString.startsWith("[")) {
			newObject = new JSONArray(jsonString);
		}
		else if (jsonString.startsWith("{")) {
			newObject = new JSONObject(jsonString);
		}
		else {
			throw new JSONException("Invalid JSON Payload");
		}
		return newObject;
	}

	private Document changeJsonField(Document document, String fieldPath, Object newValue, boolean replace) throws Exception {
		JSONObject documentJson = new JSONObject(document.toJSON());
		String[] fields = null;
		if (fieldPath != null) {
			fields = fieldPath.split("/");
		}
		changeJsonField(documentJson, fields, 0, newValue, replace);
		return new Document(documentJson);
	}

	private JSONObject changeJsonField(JSONObject json, String[] fieldPath, int fieldPathPosition, Object newValue, boolean replace) throws Exception {
		if (fieldPath.length == fieldPathPosition + 1) { // if last field
			if (replace) {
				json.put(fieldPath[fieldPathPosition], newValue);
			}
			else {
				JSONUtils.overlay((JSONObject)newValue, json.getJSONObject(fieldPath[fieldPathPosition]));
			}
		} else {
			if (!json.has(fieldPath[fieldPathPosition])) {
				json.put(fieldPath[fieldPathPosition], new JSONObject());
			}
			JSONObject modifiedJson = changeJsonField(json.getJSONObject(fieldPath[fieldPathPosition]), fieldPath, fieldPathPosition + 1, newValue, replace);
			json.put(fieldPath[fieldPathPosition], modifiedJson);
		}
		return json;
	}

	/**
	 * Verifies if a user has appropriate permissions to do the operation they are attempting
	 * @param documentName The name of the document being accessed, can be null to signify all documents
	 * @param operation The operation being performed on the document
	 * @throws WebApplicationException If the user does not have the correct permissions
	 */
	protected void checkUserPermission(@Nullable String documentName, DocumentOperation operation) throws WebApplicationException { }

    /**
     * @return The specific document type for this endpoint
     */
    protected abstract DocumentType getDocumentType();

	protected enum DocumentOperation {
		CREATE,
		READ,
		UPDATE,
		DELETE
	}

}
