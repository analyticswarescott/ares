
package com.aw.rest.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;

import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.exceptions.DocumentNotFoundException;
import com.aw.util.Statics;
import com.aw.utils.jaxrs.PATCH;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * GROUP api Created by sjeanbaptiste
 *
 */
@Singleton
@Path(Statics.REST_VERSION + "/groups")
public class GroupResource extends DocumentResourceBase {

	private Provider<DocumentHandler> docs;

	@Inject @com.google.inject.Inject
	public GroupResource(Provider<DocumentHandler> docs) {
		super(docs);
		this.docs = docs;
	};

	@Override
	protected DocumentType getDocumentType() {
		return DocumentType.DOCUMENT_GROUP;
	}

	@PATCH
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public DocumentEnvelope updateDocumentGroup(
			@NotNull @PathParam("name") String name,
			String payload) throws Exception {

		try {

			// build the document instance
			Document document = toDocument(payload);

			if(!document.getDocumentType().equals(DocumentType.DOCUMENT_GROUP)) {
				throw new WebApplicationException(
						Response.status(HttpStatus.BAD_REQUEST_400).entity("Only " + DocumentType.DOCUMENT_GROUP.toString() + " can be updated here.").build());
			}
			// force name to the one from the path - the path dictates the
			// document name not the payload
			document.setName(name);

			// now update it
			return this.docs.get().updateDocument(document);

		} catch (DocumentNotFoundException e) {

			// if a document isn't found it's a standard 404
			throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

		} catch (JsonProcessingException e) {

			// if a json parsing exception occurs, we'll blame it on the client
			// and return the message
			throw new WebApplicationException(
					Response.status(HttpStatus.BAD_REQUEST_400).entity(e.getMessage()).build());

		}

	}

	public DocumentEnvelope delete(
			@NotNull @PathParam("name") String name
			) throws Exception {

		try {

			return this.docs.get().deleteGroup(DocumentType.DOCUMENT_GROUP, name);

		} catch (DocumentNotFoundException e) {

			// if a document isn't found it's a standard 404
			throw new WebApplicationException(HttpStatus.NOT_FOUND_404);

		} catch (JsonProcessingException e) {

			// if a json parsing exception occurs, we'll blame it on the client
			// and return the message
			throw new WebApplicationException(
					Response.status(HttpStatus.BAD_REQUEST_400).entity(e.getMessage()).build());

		}

	}
}
