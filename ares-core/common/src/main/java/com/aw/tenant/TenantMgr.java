package com.aw.tenant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.util.es.ESKnownIndices;
import com.aw.platform.restcluster.RestCluster;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.DriverStatus;
import com.aw.common.tenant.Tenant;
import com.aw.common.tenant.TenantProvisioningException;
import com.aw.common.util.es.ESClient;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentMgr;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.google.common.collect.ImmutableList;

/**
 * Manages tenant details such as provisioning
 */
public class TenantMgr {

    private static final Logger logger = Logger.getLogger(TenantMgr.class);

    //TODO: use zookeeper or central non-doc DB to manage data about driver state
    //TODO: for now manage locally to allow shutdown
    protected  static HashMap<String, DriverStatus> _drivers = new HashMap<String, DriverStatus>();

    private Provider<DocumentHandler> docs;
    private Provider<DocumentMgr> docMgr;
    private Provider<Platform> platform;
    private Provider<PlatformMgr> platformMgr;
	private Provider<RestCluster> restCluster;

    /**
     * platform resources that must be provisioned - current configuration db is not considered one of these as
     * it must be provisioned before anything else can be provisioned.
     */
    private static final List<Provisioner> TENANT_PLATFORM_PROVISIONERS = ImmutableList.of(
    	new KafkaProvisioner(),
    	new ElasticsearchProvisioner(),
		new ZookeeperProvisioner(),
		new HadoopProvisioner()
    );

    @Inject @com.google.inject.Inject
    public TenantMgr(Provider<DocumentHandler> docs, Provider<Platform> platform,
					 Provider<DocumentMgr> docMgr, Provider<PlatformMgr> platformMgr, Provider<RestCluster> restCluster) {
    	this.docs = docs;
    	this.docMgr = docMgr;
    	this.platform = platform;
    	this.platformMgr = platformMgr;
		this.restCluster = restCluster;
	}

    public void unProvisionTenant(String tenantID) throws Exception{

    	try {

        	unprovisionPlatformServices(Tenant.forId(tenantID), TENANT_PLATFORM_PROVISIONERS);

    	} finally {

    		//finally remove from the configuration database
	        unProvisionDocDB(tenantID);

	        //regardless of what happens, finally remove it from the system DB
            docs.get().deleteDocument(DocumentType.TENANT, tenantID);

    	}

    }

    private void unprovisionPlatformServices(Tenant tenant, List<Provisioner> provisioners) throws Exception {

    	//start the unprovisioning threads
    	CompletionService<Provisioner> service = getProvisioningCompletionService();
    	for (Provisioner provisioner : TENANT_PLATFORM_PROVISIONERS) {
        	logger.info("unprovisioning " + provisioner.getRole() + " for tenant " + tenant.getTenantID());
    		service.submit(() -> unprovision(tenant, provisioner));
    	}

    	Exception exception = null;
    	for (int x=0; x<TENANT_PLATFORM_PROVISIONERS.size(); x++) {

    		try {
        		service.take().get();
        	} catch (Exception e) {
        		exception = e;
        		logger.error("error unprovisioning " + tenant.getTenantID(), e);
    		}

    	}

    	//bubble up the most recent exception if any exceptions occur during unprovisioning
    	if (exception != null) {
    		throw exception;
    	}

    }

    /**
     * uses a provisioner to provision, handling thread security
     *
     * @param tenant the tenant to provision
     * @param provisioner the provisioner to use
     * @return the provisioner used
     * @throws Exception if anything went wrong
     */
    private Provisioner provision(Tenant tenant, Provisioner provisioner) throws Exception {

    	SecurityUtil.setThreadSystemAccess();
    	Impersonation.impersonateTenant(tenant);

    	try {
        	provisioner.provision(tenant, this);
    	} finally {
    		Impersonation.unImpersonate();
    	}

    	return provisioner;

    }

    /**
     * uses a provisioner to unprovision, handling thread security
     *
     * @param tenant the tenant to unprovision
     * @param provisioner the provisioner to use
     * @return the provisioner used
     * @throws Exception if anything went wrong
     */
    private Provisioner unprovision(Tenant tenant, Provisioner provisioner) throws Exception {

    	SecurityUtil.setThreadSystemAccess();
    	Impersonation.impersonateTenant(tenant);

    	try {
        	provisioner.unprovision(tenant, this);
    	} finally {
    		Impersonation.unImpersonate();
    	}

    	return provisioner;

    }

    protected void unProvisionDocDB(String tenantID) throws Exception{

    	//for now we won't actually remove existing tenant config dbs - issues in derby, and this allows us to keep data for a tenant
    	//if we need to recreate it
    }

    public void provisionTenant(Tenant tenant) throws Exception {

        String tenantID = tenant.getTenantID();

        if (tenantID == null) {
            throw new Exception("Cannot create a tenant without a tid.");
        }

        if ( Tenant.SYSTEM_TENANT_ID.equals(tenantID) ) {

        	//provision system tenant
        	provisionSystemTenant();
        	return;

        }

        if (docs.get().tenantExists(tenantID)) {
            throw new Exception("Cannot create tenant ID " + tenantID + ", it already exists.");
        }

        // impersonate the tenant being spawned
        Impersonation.impersonateTenant(tenantID);

        //if anything goes wrong here, just unprovision the doc DB
        try {

            logger.info("provisioning doc DB for tenant " + tenantID);
            provisionDocDB(tenant);

            logger.info("provisioning platform services  for tenant " + tenantID);
			provisionPlatformServices(tenant);

			logger.info("provisioning complete for " + tenantID);

		}

        catch (Exception ex) {
            throw new TenantProvisioningException(" error provisioning tenant", ex);
        }

        finally {
    		Impersonation.unImpersonate();
        }

		//if everything went right, now create persistent record -- effectively calling "commit" on the tenant
        //save tenant record to system (Tenant 0) db
        try {
            docMgr.get().persistToSysDB(tenant);
        }

        catch (Exception ex) {

            logger.error(" persisting of tenant record to sys DB failed");
            throw ex;

        }


    }

    public String getTenant(String tenantID) throws Exception {
        logger.debug("Getting tenant: " + tenantID);

        Document doc = docs.get().getDocument(DocumentType.TENANT, DocumentType.TENANT.toString() + ":" + tenantID);
        return doc.toJSON();

    }

    private void provisionSystemTenant() throws Exception {

    	//check elasticsearch
    	ESClient client = new ESClient(platform);

    	//TODO: automatically do this using the topic enum


    	if (!client.indexExists(ESKnownIndices.ERRORS, Instant.now())) {

    		logger.info("provisioning elasticsearch system index: " + ESKnownIndices.ERRORS);
    		client.applyIndexMappings(ESKnownIndices.ERRORS, docs.get(), platform.get(), Instant.now());

    	}

    	//create the incident index
    	if (!client.indexExists(ESKnownIndices.INCIDENTS, Instant.now())) {

    		logger.info("provisioning elasticsearch system index: " + ESKnownIndices.INCIDENTS);
    		client.applyIndexMappings(ESKnownIndices.INCIDENTS, docs.get(), platform.get(), Instant.now());

    	}

    	if (!client.indexExists(ESKnownIndices.STATUS, Instant.now())) {

    		logger.info("provisioning elasticsearch system index: " + ESKnownIndices.STATUS);
    		client.applyIndexMappings(ESKnownIndices.STATUS, docs.get(), platform.get(), Instant.now());

		}

    	new KafkaProvisioner().provision(Tenant.SYSTEM, this);
		new ZookeeperProvisioner().provision(Tenant.SYSTEM, this);
		new HadoopProvisioner().provision(Tenant.SYSTEM, this);

    }

    private void provisionDocDB(Tenant tenant) throws Exception {

        docMgr.get().addDocHandler(tenant, true);

    }

    private void provisionPlatformServices(Tenant tenant) throws Exception {

    	//provision kafka - create topic(s) we'll need for this tenant

    	CompletionService<Provisioner> service = getProvisioningCompletionService();

    	for (Provisioner provisioner : TENANT_PLATFORM_PROVISIONERS) {
        	logger.info("provisioning " + provisioner.getRole() + " for tenant " + tenant.getTenantID());
    		service.submit(() -> provision(tenant, provisioner));
    	}

    	List<Provisioner> provisioned = new ArrayList<>();
    	Exception exception = null;
    	for (int x=0; x<TENANT_PLATFORM_PROVISIONERS.size(); x++) {

    		try {
    			provisioned.add(service.take().get());
    		} catch (Exception e) {
    			logger.error("error provisioning tenant " + tenant + ", will unprovision", e);
    			exception = e;
    		}

    	}

    	//if there was an exception, unprovision
    	if (exception != null) {
			unprovisionPlatformServices(tenant, provisioned);
			logger.error("error during provisioning, unprovisioning all provisioned resources");
			throw exception;

		}

    }

    /**
     * @param tenantID
     * @throws Exception
     */
    public void restartTenantStreamingServices(String tenantID) throws Exception {

    }

    private CompletionService<Provisioner> getProvisioningCompletionService() {

    	//N threads where N is the number of things we are provisioning
    	ExecutorService executor =  Executors.newCachedThreadPool();

    	//create the service
        CompletionService<Provisioner> services = new ExecutorCompletionService<>(executor);

    	return services;

    }

    public static int provisionCount = 0; //for testing

    public void restartAllTenantsStreamingServices() throws Exception {
        Collection<Document> allTenants = getAllTenants();
        logger.debug("Restarting streaming services for all tenants.");

        if (allTenants.size() == 0) {
            // empty
            logger.warn("No tenants found.");
            return;
        }

        for (Document tenant : allTenants) {
            String tenantID = tenant.getName();
            logger.info("restarting for " + tenantID);
            restartTenantStreamingServices(tenantID);
        }
    }

    public boolean stopTenantStreamingServices(String tenantID) throws Exception {
        logger.info("Stopping streaming services for tenant ID " + tenantID);
         //TODO: implement stop using driver ID hash from startup -- will need to be persisted in tenant doc
        return false;
    }

    public Collection<Document> getAllTenants() throws Exception {
        return docs.get().getAllTenants();
    }

    public JSONArray getAllTenantIDs() throws Exception {
        JSONArray allTenantIDs = new JSONArray();
        Collection<Document> allTenants = docs.get().getAllTenants();
        for (Document tenantDoc : allTenants) {
            allTenantIDs.put(tenantDoc.getName());
        }
        return allTenantIDs;
    }

    //the following are resource methods used by provisioners

    /**
     * @return the document handler for the current tenant
     */
    DocumentHandler getDocs() { return docs.get(); }

    /**
     * @return the platform
     */
    Platform getPlatform() { return platform.get(); }

    /**
     * @return the platform manager
     */
    PlatformMgr getPlatformMgr() { return platformMgr.get(); }

	RestCluster getRestCluster() { return restCluster.get(); }

}
