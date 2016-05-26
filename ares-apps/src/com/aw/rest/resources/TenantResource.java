package com.aw.rest.resources;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.tenant.TenantMgr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api
@Path(com.aw.util.Statics.REST_VERSION + "/tenant")
public class TenantResource extends RestResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(TenantResource.class);

    @Context
    ServletContext _context; //get startup info from web.xml

    private Provider<DocumentHandler> docs;
    private TenantMgr tenantMgr;

    @Inject @com.google.inject.Inject
    public TenantResource(Provider<DocumentHandler> docs, TenantMgr tenantMgr) {
    	this.docs = docs;
    	this.tenantMgr = tenantMgr;
	}

    @GET
    @Path("/{tenant_id}/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Tenant Nodes",
    notes = "Returns the tenant nodes for the specified tenant ID",
    response = Document.class)
    public Response getTenantNodes(
            @ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID
    ) throws Exception {
        if ( !"0".equals(tenantID) ) {
            throw new UnsupportedOperationException("Only tenant 0 is supported for now");
        }

        Document document = this.docs.get().getDocument(DocumentType.PLATFORM, Platform.LOCAL);
        return Response.ok(document.toJSON()).build();
    }

    @PUT
    @Path("/{tenant_id}/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Tenant Nodes",
    notes = "Updates the tenant nodes for the specified tenant ID",
    response = DocumentEnvelope.class)
    public Response updateTenantNodes(
    		@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID,
    		@ApiParam(value = "the tenant nodes information", required = true) String platformSpecStr
    ) throws Exception {
        if ( !"0".equals(tenantID) ) {
            throw new UnsupportedOperationException("Only tenant 0 is supported for now");
        }

        JSONObject platformSpecJSON = new JSONObject(platformSpecStr);
        Document document = new Document(platformSpecJSON);
        DocumentEnvelope updatedDocument = this.docs.get().updateDocument(document);
        return Response.ok().entity(updatedDocument.toJSON()).build();
    }

    @PUT
    @Path("/{tenant_id}/nodes/{node_name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Tenant Nodes",
    notes = "Updates the tenant nodes for the specified tenant ID and specifed node",
    response = DocumentEnvelope.class)
    public Response updateTenantNodes(
    		@ApiParam(value = "the tenant ID", required = true) @PathParam("tenant_id") String tenantID,
    		@ApiParam(value = "the node name", required = true) @PathParam("node_name") String nodeName,
    		@ApiParam(value = "the tenant nodes information", required = true) String nodeSpectStr
    ) throws Exception {
        if ( !"0".equals(tenantID) ) {
            throw new UnsupportedOperationException("Only tenant 0 is supported for now");
        }

        JSONObject nodeSpec = new JSONObject(nodeSpectStr);

        Document platformDocument = this.docs.get().getDocument(DocumentType.PLATFORM, Platform.LOCAL);
        JSONObject platformNodes = platformDocument.getBody().getJSONObject("nodes");
        platformNodes.putOpt(nodeName, nodeSpec);
        DocumentEnvelope updatedDocument = this.docs.get().updateDocument(platformDocument);
        return Response.ok().entity(updatedDocument.toJSON()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Tenant",
    notes = "Creates a new tenant")
    public Response createTenant(
            @ApiParam(value = "the new tenant data", required = true) String tenantData
    ) throws Exception {
    	Tenant tenant = new Tenant(new JSONObject(tenantData.toString()));
        logger.warn("Provisioning tenant: " + tenant.getTenantID());
        tenantMgr.provisionTenant(tenant);
        return Response.ok().build();
    }


    @DELETE
    @Path("/{tenant_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete Tenant",
    notes = "Deletes an existing tenant")
    public Response deleteTenant(
    		@ApiParam(value = "the tenant ID to delete", required = true) @PathParam("tenant_id") String tenantID
    ) throws Exception {

        logger.warn("un-Provisioning tenant: " + tenantID);
        tenantMgr.unProvisionTenant(tenantID);
        return Response.ok().build();
    }


    //Ops endpoint to get list of provisioned tenants
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Tenants",
    notes = "Gets all tenants",
    response = Tenant.class,
    responseContainer = "Array")
    public Response getTenants(
    ) throws Exception {
        Collection<Document> tenants = tenantMgr.getAllTenants();

        JSONArray ret = new JSONArray();
        for (Document tenant : tenants) {
            JSONObject tDoc = tenant.getBody();
            ret.put(tDoc);
        }
        return Response.ok(ret).build();
    }


    @GET
    @Path("/{tenant_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenant(
            @PathParam("tenant_id") String tenant_id
    ) throws Exception {
        return Response.ok(tenantMgr.getTenant(tenant_id)).build();
    }

    @PUT
    @Path("/{tenant_id}/services/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restartTenantStreamingServices(
            @PathParam("tenant_id") String tenantID
    ) throws Exception {
    	//TODO: refactor static TenantMgr
    	tenantMgr.restartTenantStreamingServices(tenantID);
        return Response.ok().build();
    }

    @PUT
    @Path("/services/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restartAllTenantsStreamingServices() throws Exception {
    	//TODO: refactor static TenantMgr
    	tenantMgr.restartAllTenantsStreamingServices();
        return Response.ok().build();
    }

    @PUT
    @Path("/{tenant_id}/services/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopTenantStreamingServices(
            @PathParam("tenant_id") String tenantID
    ) throws Exception {
    	//TODO: refactor static TenantMgr
    	tenantMgr.stopTenantStreamingServices(tenantID);
        return Response.ok().build();
    }


}
