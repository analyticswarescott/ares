package com.aw.rest.resources;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.aw.common.rest.security.TenantAware;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.QueryFactory;
import com.aw.unity.QueryFactory.QueryFormat;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityResults;
import com.aw.unity.UnityRunner;
import com.aw.unity.UnityRunner.ResponseFormat;
import com.aw.unity.UnityRunner.State;
import com.aw.unity.UnityRunnerStatus;
import com.aw.unity.query.Query;

import io.swagger.annotations.Api;

/**
 * Unity REST interface
 *
 *
 */
@Api
@Singleton
@Path(com.aw.util.Statics.REST_VERSION + "/unity")
public class UnityResource extends RestResourceBase implements TenantAware {

	static final Logger logger = Logger.getLogger(UnityResource.class);

	private Provider<UnityInstance> unityProvider;
	private Provider<DocumentHandler> docProvider;

	@Inject @com.google.inject.Inject
	public UnityResource(Provider<UnityInstance> unityProvider, Provider<DocumentHandler> docProvider) {

		this.unityProvider = unityProvider;
		this.docProvider = docProvider;

	}

    /**
     * @return All fields known to this unity instance
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/fields")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Field[] getFields() throws Exception {

        //return the fields
        return this.unityProvider.get().getMetadata().getFieldRepository().getFields();

    }

    /**
     * @return All datatypes known to this unity instance
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/datatypes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DataType[] getDatatypes() throws Exception {

		//return the data types
		return this.unityProvider.get().getMetadata().getDataTypeRepository().getDataTypes();

    }

    /**
     * @return All datatypes known to this unity instance
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/resolvers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JSONArray getResolvers() throws Exception {

		//return the resolvers
    	List<Document> repos = this.docProvider.get().getDocumentsOfType(DocumentType.UNITY_DATATYPE_REPO);

		//merge all data type repositories
		JSONObject masterRepo = JSONUtils.merge(repos);

		//return the resolvers
		return masterRepo.getJSONArray("resolvers");

    }

    /**
     * Run an asynchronous query and return the guid for it
     *
     * @param queryMetaStr The query json
     * @return The query results
     * @throws Exception If anything goes wrong
     */
    @POST
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UnityRunner performUnityQuery( String queryMetaStr ) throws Exception {

    	if (queryMetaStr == null) {
    		throw new WebApplicationException(HttpStatus.BAD_REQUEST_400);
    	}

    	try {

        	//build the query
            Query query = QueryFactory.newQuery(queryMetaStr, QueryFormat.JSON, this.unityProvider.get());

            //run the query
            return this.unityProvider.get().execute(query);

    	} catch (Exception e) {
    		logger.error("error running query: " + new JSONObject(queryMetaStr).toString(4), e);
        	throw new Exception("unity query:\n\n" + new JSONObject(queryMetaStr).toString(4) + "\n\n", e);
    	}
    }

    /**
     * Checks the status of a query
     *
     * @param guid The query guid to check
     * @return The status of the query
     * @throws Exception If anything goes wrong
     */
    @GET
    @Path("/query/{guid}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UnityRunnerStatus getStatus(@PathParam("guid") String guid) throws Exception {

    	//get the runner
    	UnityRunner runner = this.unityProvider.get().getRunner(readGuid(guid));

    	//make sure we have one
    	checkMissing(runner);

    	//build the status
    	return new UnityRunnerStatus(runner);

    }

    /**
     * Gets the results of a completed query. If the query is not complete yet,
     * this call will return BAD_REQUEST.
     *
     * @param guid The guid of the query
     * @return The query results, if completed
     * @throws Exception if anything goes wrong
     */
    @GET
    @Path("/query/{guid}/results")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UnityResults getResults(@PathParam("guid") String guid) throws Exception {

    	//get the runner
    	UnityRunner runner = this.unityProvider.get().getRunner(readGuid(guid));

    	//make sure we have one
    	checkMissing(runner);

    	//make sure the query is complete
    	if (runner.getState() != State.COMPLETE) {
    		throw new WebApplicationException(Response.status(HttpStatus.BAD_REQUEST_400).entity("query is not complete yet").build());
    	}

    	//get the results
    	return runner.getResults(ResponseFormat.UNITY_ROWS);

    }

}
