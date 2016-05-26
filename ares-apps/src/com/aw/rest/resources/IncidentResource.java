package com.aw.rest.resources;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.aw.action.ActionManager;
import com.aw.common.auth.User;
import com.aw.incident.*;
import com.aw.incident.Incident.State;
import com.aw.incident.action.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * Incident REST API
 *
 *
 *
 */
@Api
@Path(com.aw.util.Statics.REST_VERSION + "/incidents")
public class IncidentResource {

	/**
	 * The number of bytes to send before flushing a streaming interface
	 */
	private static final int FLUSH_AFTER_BYTES = 8192;

	private ActionManager actionManager;
	private IncidentManager incidents;

	@Inject
	public IncidentResource(ActionManager actionManager, IncidentManager incidents) {
		this.actionManager = actionManager;
		this.incidents = incidents;
	}

	/**
	 * Returns a list of incidents
	 * @param sort The method of sorting the incidents
	 * @param includeClosed Include closed incidents
	 * @return The list of incidents
	 * @throws Exception If anything goes wrong
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Incidents",
	notes = "Returns a list of incidents",
	response = Incident.class,
	responseContainer = "Collection")
	public List<Incident> getIncidents(
		@ApiParam(value = "the sorting", required = false) @QueryParam("sort") @DefaultValue("HIGH_SEVERITY_FIRST") IncidentSort sort,
		@ApiParam(value = "include closed incidents", required = false) @QueryParam("all") @DefaultValue("false") boolean includeClosed
	) throws Exception {
		return incidents.getIncidentsForCurrentUser(sort, includeClosed);
	}

	/**
	 * Returns the list of occurrences for the timeline
	 * @param guid The guid of the incident
	 * @return The list of occurrences sorted with most recent first
	 * @throws Exception If anything goes wrong
     */
	@GET
	@Path("{guid}/timeline")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Action Timeline",
	notes = "Returns the incident timeline for the given incident",
	response = IncidentAction.class,
	responseContainer = "List")
	public List<Object> getIncidentTimeline(@ApiParam(value = "the incident guid", required = true) @PathParam("guid") String guid) throws Exception {

		return incidents.getIncidentTimeline(guid);
	}

	/**
	 * Search incidents belonging to the current tenant
	 * @param searchText Text that must be found in one value of the incident to be included
	 * @param user The user assigned to the incident
	 * @param state The state of the incident
	 * @param severity The severity of the incident
	 * @param sort The way to sort the incidents
	 * @return The matching incidents
	 */
	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Search incidents",
		notes = "Returns the incident that match the sent parameters",
		response = Incident.class,
		responseContainer = "List")
	public List<Incident> searchIncidents(
		@ApiParam(value = "the search text", required = false) @QueryParam("q") String searchText,
		@ApiParam(value = "the user to search for", required = false) @QueryParam("user") String user,
		@ApiParam(value = "the status to search for", required = false) @QueryParam("status") Incident.State state,
		@ApiParam(value = "the severity to search for", required = false) @QueryParam("severity") Incident.Severity severity,
		@ApiParam(value = "the sorting", required = false) @QueryParam("sort") @DefaultValue("HIGH_SEVERITY_FIRST") IncidentSort sort
	) throws Exception {
		return incidents.searchIncidents(searchText, user, state, severity, sort);
	}

	/**
	 * Create a new incident
	 *
	 * @param incident The incident to create
	 * @throws Exception If anything goes wrong
	 */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Incident",
    notes = "Creates a new incident",
    response = Incident.class)
    public Incident createIncident(
    		@ApiParam(value = "the incident to create", required = true) Incident incident
	) throws Exception {

    	//it's manually created through the rest interface
    	incident.setCreationType(CreationType.MANUAL);
		//assign a random guid to prevent overwriting existing incidents
		incident.setGuid(UUID.randomUUID());

    	//create the action
    	CreateIncidentAction ret = new CreateIncidentAction(incident);

    	//take the action
    	actionManager.takeAction(ret);

    	return incident;

    }

	/**
	 * Updates an existing incident
	 *
	 * @param guid The guid of the incident to update
	 * @param incident The values to update
	 * @throws Exception If anything goes wrong
	 */
    @PUT
    @Path("{guid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Incident",
    notes = "Updates an existing incident",
    response = Incident.class)
    public Incident updateIncident(
    		@ApiParam(value = "the guid of the incident to update", required = true) @PathParam("guid") String guid,
    		@ApiParam(value = "the values to update", required = true) Incident incident
	) throws Exception {

    	//make sure the guid in the resource path is the one used on the incident
    	incident.setGuid(UUID.fromString(guid));

    	//create the action
    	UpdateIncidentAction ret = new UpdateIncidentAction(incident);

    	//take the action
    	actionManager.takeAction(ret);

    	return incident;

    }

	/**
	 * Create a new incident association
	 *
	 * @param guid The incident's GUID
	 * @param association The association to set
	 * @throws Exception If anything goes wrong
	 */
    @POST
    @Path("{guid}/associations")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Associate Incident",
    notes = "Create a new incident association")
    public AssociateIncidentAction associate(
    		@ApiParam(value = "the incident's GUID", required = true) @PathParam("guid") String guid,
    		@ApiParam(value = "the association to set", required = true) AssociateIncidentAction association
	) throws Exception {

    	//connect the incident to the association
    	association.setIncidentGuid(guid);

    	//take the action
    	actionManager.takeAction(association);

		return association;

    }

    /**
     * Assign an incident to someone
     *
     * @param incidentGuid The incident to assign
     * @param user The user that is getting the incident assigned to them
     * @throws Exception If anything goes wrong
     */
    @PUT
    @Path("{guid}/assignee")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Assign Incident",
    notes = "Assign an incident to someone",
    response = AssignIncidentAction.class)
    public AssignIncidentAction takeAction(
    		@ApiParam(value = "the incident to assign", required = true) @PathParam("guid") UUID incidentGuid,
    		@ApiParam(value = "the user that is getting the incident assigned to them", required = true) User user
	) throws Exception {

    	//build the action
    	AssignIncidentAction ret = new AssignIncidentAction(incidentGuid, user);

    	//take the action
    	actionManager.takeAction(ret);

    	return ret;

    }

    /**
     * Set the state of an incident
     *
     * @param incidentGuid The incident whose state will be set
     * @param state The new state for the incident
     * @throws Exception If anything goes wrong
     */
    @PUT
    @Path("{guid}/state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set Incident State",
    notes = "Set the state of an incident",
    response = SetStateIncidentAction.class)
    public SetStateIncidentAction takeAction(
    		@ApiParam(value = "the incident whose state will be set", required = true) @PathParam("guid") UUID incidentGuid,
    		@ApiParam(value = "the new state for the incident", required = true) State state
	) throws Exception {

    	//create the action
    	SetStateIncidentAction ret = new SetStateIncidentAction(incidentGuid, state);

    	//take the action
    	actionManager.takeAction(ret);

    	return ret;

    }

    /**
     * Add a comment to an incident
     *
     * @param incidentGuid The incident to add a comment
     * @param comment The comment to add
     * @throws Exception If anything goes wrong
     */
    @POST
    @Path("{guid}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add Incident Comment",
    notes = "Add a comment to an incident",
    response = CommentIncidentAction.class)
    public CommentIncidentAction takeAction(
    		@ApiParam(value = "the incident to add a comment", required = true) @PathParam("guid") UUID incidentGuid,
    		@ApiParam(value = "the comment to add", required = true) DefaultComment comment
	) throws Exception {

    	//create the action
    	CommentIncidentAction action = new CommentIncidentAction(incidentGuid, comment.getComment());

    	//take the action
    	actionManager.takeAction(action);

    	return action;

    }

	/**
	 * Add a file attachment to an incident
	 * @param incidentGuid The guid of the incident
	 * @param attachment The file to attach
	 * @param contentDisposition File metadata
	 * @return The created attachment incident
     * @throws Exception If anything goes wrong
     */
	@POST
	@Path("{guid}/attachments")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Add incident attachment",
	notes = "Add a file upload to an incident, the file must be in multipart data under the form data section \"file\"",
	response = AttachIncidentAction.class)
	public AttachIncidentAction attachFile(
		@ApiParam(value = "the guid of the incident", required = true) @PathParam("guid") UUID incidentGuid,
		@ApiParam(value = "the file to attach", required = true) @FormDataParam("file") InputStream attachment,
		@FormDataParam("file") FormDataContentDisposition contentDisposition
	) throws Exception {
		AttachIncidentAction attachAction = new AttachIncidentAction(attachment, contentDisposition.getFileName(), incidentGuid);
		actionManager.takeAction(attachAction);
		return attachAction;
	}

	/**
	 * Retrieve a file attachment
	 * @param incidentGuid The guid of the incident
	 * @param actionGuid The guid of the action
	 * @return The attachment
	 * @throws Exception If anything goes wrong
     */
	@GET
	@Path("{inc_guid}/attachments/{action_guid}")
	@ApiOperation(value = "Get incident attachment",
	notes = "Get an attachment associated with an incident",
	response = StreamingOutput.class)
	public Response getAttachment(
		@ApiParam(value = "the guid of the incident", required = true) @PathParam("inc_guid") UUID incidentGuid,
		@ApiParam(value = "the guid of the action", required = true) @PathParam("action_guid") UUID actionGuid
	) throws Exception {
		AttachIncidentAction action = (AttachIncidentAction)incidents.getIncidentAction(actionGuid.toString(), AttachIncidentAction.UNITY_TYPE);
		validateAttachmentAction(action, incidentGuid);
		DownloadIncidentAttachmentAction downloadAction = new DownloadIncidentAttachmentAction(action);
		actionManager.takeAction(downloadAction);
		InputStream file = downloadAction.getDownloadedFile();
		ContentDisposition contentDisposition = ContentDisposition.type("attachment")
			.fileName(action.getFileName()).build();
		StreamingOutput fileOutput = getStreamingOutput(file);
		return Response.ok(fileOutput).header("Content-Disposition", contentDisposition).build();
	}

	private void validateAttachmentAction(AttachIncidentAction action, UUID incidentGuid) {
		if (action == null || !action.getIncidentGuid().equals(incidentGuid.toString())) {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Attachment not found").build());
		}
	}

	private StreamingOutput getStreamingOutput(InputStream stream) {
		return outputStream -> {
			int nextDatum;
			int flushCount = FLUSH_AFTER_BYTES;
			while ((nextDatum = stream.read()) != -1) {
				outputStream.write(nextDatum);
				flushCount--;
				if (flushCount <= 0) {
					outputStream.flush();
				}
			}
			outputStream.close();
			stream.close();
		};
	}

}
