package com.aw.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.document.jdbc.citus.CitusJDBCDocumentHandler;
import com.aw.document.jdbc.citus.CitusProvider;
import org.apache.log4j.Logger;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.exceptions.DocumentDBStateException;
import com.aw.document.jdbc.DocumentJDBCProvider;
import com.aw.document.jdbc.JDBCDocumentHandler;
import com.aw.document.jdbc.SequencedDocumentJDBCProvider;
import com.aw.document.jdbc.SequencedJDBCDocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.util.Statics;
import com.google.common.base.Preconditions;

/**
 * For lack of a better term and to coordinate naming with the rest of the system, this class will manage document handler access.
 *
 *
 *
 */
public class DocumentMgr implements TenantAware {

	static final Logger logger = Logger.getLogger(DocumentMgr.class);

	private RestCluster restCluster;
	private PlatformMgr platformMgr;
	private DocumentJDBCProvider jdbc;

	public DocumentMgr(RestCluster restCluster, PlatformMgr platformMgr, DocumentJDBCProvider jdbc) {
		this.restCluster = restCluster;
		this.platformMgr = platformMgr;
		this.jdbc = jdbc;
	}

	/**
	 * Initialize the tenant document data
	 */
	public void initDocuments() {

		try {

			if (initialized) {
				logger.warn("already initialized documents, not initializing again");
				return;
			}

			//check our platform - if no config DB, don't initialize
			if (!platformMgr.getPlatform().hasRole(NodeRole.CONFIG_DB_MASTER)) {
				logger.warn("no config db defined yet, not initializing documents");
				return;
			}

			else {
				logger.warn("platform has config db, initializing documents");
			}

			//make sure we're the system tenant
			SecurityUtil.setThreadSystemAccess();

			docHandlers.clear();

			//create the config db instance
			configDb = new DBMgr(platformMgr, jdbc);

			if (EnvironmentSettings.isAuthDisabled()) {
				logger.warn("Security is disabled because DISABLE_AUTH_VALIDATION is set true!");
			}

			//initialize existing tenant document dbs
			logger.debug("initializing existing tenants");

			initExistingTenants();


			//only do this successfully once
			initialized = true;

		} catch (Exception e) {
			throw new InitializationException("error initializing document manager", e);
		}

	}

    public void persistToSysDB(Tenant tenant) throws  Exception {

		logger.debug("about to check documentExists in common.Tenant");
        //TODO support updates to tenant metadata
        if (!getDocHandler().documentExists(DocumentType.TENANT, tenant.getTenantID())) {

			logger.debug("tenant ID : " + getTenantID() +  " not found...creating...");
        	//create the document
            Document doc = new Document(DocumentType.TENANT, tenant.getTenantID(), tenant.getLongName(), getTenantID(), Statics.DG_USER, tenant.getAsJSON());
			DocumentHandler dh = getDocHandler();

			//we are in bootstrapping mode if we are setting up the system tenant (i.e. the platform may not be functional yet)
			dh.setBootstrapping(tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID));

			try {
				dh.createDocument(doc);
			}
			finally {
				dh.setBootstrapping(false);
			}

        }
		else {
			logger.debug("tenant ID : " + getTenantID() +  " exists...not persisting...");
		}

    }

    /**
	 * Initialize existing tenants on startup
	 *
	 * @throws Exception If anything goes wrong
	 */
	public void initExistingTenants() throws Exception {

		//first initialize the system tenant

		initSystemTenant();

		//re-initialize DB pools for initialize existing non-system tenants
		List<Document> allTenants = getSysDocHandler().getDocumentsOfType(DocumentType.TENANT);

		for (Document doc : allTenants) {

			Tenant t = JSONUtils.objectFromString(doc.getBodyAsString(), Tenant.class);

			if (!t.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
				addDocHandler(t, true);
			}
		}



	}

	/**
	 * Initialize tenant 0
	 *
	 * @throws Exception
	 */
	protected void initSystemTenant() throws Exception {

		logger.debug("initializing System tenant");
		Tenant tenant = Tenant.getDefaultTenant();
		addDocHandler(tenant, true);

		logger.debug("persisting System tenant to Sys DB");
		persistToSysDB(tenant);

	}



	/**
	 * Add a document handler for the given tenant - if the tenant is new (i.e. has no data yet) it will be loaded
	 * with the default document set.
	 *
	 * @param tenant The tenant
	 * @return The document handler for this tenant
	 * @throws Exception If anything goes wrong
	 */
	public DocumentHandler addDocHandler(Tenant tenant, boolean doBootstrap) throws  Exception{

		Preconditions.checkArgument(tenant != null, "new tenant cannot be null");
		Preconditions.checkNotNull(configDb, "config db null, local document database not initialized");

		if (docHandlers.containsKey(tenant.getTenantID())) {
			logger.info("doc db for " + tenant.getTenantID() + " already exists, not recreating");
			return docHandlers.get(tenant.getTenantID());
		}

		//do the db initialize per tenant (for now)
		configDb.initDB(tenant);

		//create the doc handler
		DocumentHandler docs = null;

		//if the document handler supports op sequences, wrap it in a zk cluster document handler
		if (jdbc instanceof SequencedDocumentJDBCProvider) {

			//create a JDBC handler with the provided sql provider
			SequencedJDBCDocumentHandler handler = new SequencedJDBCDocumentHandler(this, platformMgr, (SequencedDocumentJDBCProvider)jdbc, restCluster);

			//create the zk cluster document handler with the given backing jdbc handler
			docs = new ZkClusterDocumentHandler(this, platformMgr, restCluster, handler);

		}
		else if (jdbc instanceof CitusProvider) {
			docs = new CitusJDBCDocumentHandler(this, platformMgr, jdbc, restCluster);
		}

		//else no sequencing, the database better handle it all
		else {

			docs = new JDBCDocumentHandler(this, platformMgr, jdbc);

		}

		//this allows us to create doc handlers without bootstrapping
		//   which allows treating treat new tenants as config changes and check consistency when servicing reads

		//set up the doc handler
		docs.setDBMgr(configDb);
		docs.setTenant(tenant);
		docs.initForTenant(doBootstrap);

		docHandlers.put(tenant.getTenantID(), docs);

		return docs;

	}

	/**
	 * Add the given document handler for the given tenant, if not already defined.
	 *
	 * @param tenant
	 * @param docs
	 */
	public void addDocHandler(Tenant tenant, DocumentHandler docs) {

		Preconditions.checkState(!docHandlers.containsKey(tenant.getTenantID()), "attempted to register doc handler for tenant " + tenant.getTenantID() + ", but one was already registered");
		if (docHandlers.get(tenant.getTenantID()) == null) {
			docHandlers.put(tenant.getTenantID(), docs);
		}

	}

	public void removeDocHandler(String tenantID) throws Exception {
		docHandlers.remove(tenantID);
        configDb.destroyDB(tenantID);
	}

	public DocumentHandler getDocHandler() {
		logger.debug("getting doc handler for tenant " + getTenantID());
		return getDocHandler(getTenantID());
	}

	public DocumentHandler getSysDocHandler() {
		return getDocHandler(Tenant.SYSTEM_TENANT_ID);
	}

	private Map<String, DocumentHandler> docHandlers = new HashMap<>();
	private DocumentHandler getDocHandler(String tenantID) {

		try {

			logger.debug(" doc handler count " + docHandlers.size());
			DocumentHandler ret = docHandlers.get(tenantID);

			//if we don't have a document handler for this tenant
			if (ret == null) {

				logger.debug("no doc handler for tenant " + tenantID);

				//if we don't have a reference to a config db
				if (configDb == null) {

					//and the platform has a config_db defined
					if (platformMgr.getPlatform().hasRole(NodeRole.CONFIG_DB_MASTER)) {

						//return a rest document handler
						logger.info("no internal document handler detected, creating rest handler for tenant " + getTenantID());
						ret = new DocumentHandlerRest(tenantID, platformMgr.get());
						docHandlers.put(tenantID, ret);

					}

					else {

						//else platform doesn't have a config db yet, return a local document handler for initialization
						ret = new LocalDocumentHandler(EnvironmentSettings.getConfDirectory() + "/defaults");

					}

				}

				//else we do have config docs but not for this tenant, that's an error
				else {
					throw new RuntimeException("no document handler found for tenant " + getTenantID());
				}

			}
			logger.debug("returning doc handler for tenant " + tenantID);
			return ret;

		} catch (Exception e) {
			throw new DocumentDBStateException("error getting document handler for " + tenantID, e);
		}

	}





	public void reset() throws Exception {

		if (configDb != null) {
			configDb.close();
			configDb = null;
		}

		docHandlers.clear();

		initialized = false;

	}

	public boolean isInitialized() { return initialized; }
	private boolean initialized = false;

	private DBMgr configDb;

}
