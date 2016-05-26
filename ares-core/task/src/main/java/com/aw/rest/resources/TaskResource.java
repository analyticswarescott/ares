package com.aw.rest.resources;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.TaskDef;
import com.aw.common.task.TaskExecutor;
import com.aw.common.task.TaskStatus;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskNotFoundException;
import com.aw.util.Statics;

/**
 * REST interface for tasks
 *
 *
 *
 */
@Path(Statics.REST_VERSION + "/tasks")
public class TaskResource implements TaskExecutor {

	private LocalTaskContainer container;

	@Inject @com.google.inject.Inject
	public TaskResource(LocalTaskContainer container) {

		this.container = container;

	}

	/**
	 * Get status of all tasks
	 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<TaskStatus> getStatus() throws Exception {

    	//get status of tasks that are executing
    	return container.getTaskStatus();

    }

	/**
	 * Get status of a particular task
	 */
    @GET
    @Path("{guid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TaskStatus getStatus(@PathParam("guid") UUID guid) throws TaskException {

    	try {
        	return container.getStatus(guid);
    	} catch (TaskNotFoundException e) {
    		e.printStackTrace();
    		throw new WebApplicationException(Response.status(HttpStatus.SC_NOT_FOUND).entity(e.getMessage()).build());
    	}

    }

    /**
     * Execute a new task for the given tenant, return the guid for it
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UUID executeTask(TaskDef taskDef) throws Exception {

    	//return the overall container status
    	return container.executeTask(taskDef);

    }

}
