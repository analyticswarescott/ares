package com.aw.platform;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.aw.common.auth.DefaultUser;
import com.aw.common.hadoop.FileAccessorBase;
import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.messaging.Messenger;
import com.aw.common.messaging.StringMessage;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.system.structure.PathResolverSystem;
import com.aw.common.system.structure.PathResolverTenant;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentListener;
import com.aw.document.DocumentType;
import com.aw.platform.exceptions.PlatformInitializationException;
import com.aw.platform.monitoring.os.OSPerfStats;
import com.aw.platform.nodes.DefaultNodeClient;
import com.aw.platform.nodes.NodeClient;
import com.aw.util.Statics;
import com.aw.utils.kafka.client.KafkaMessenger;

/**
 * Manages things to do with the platform
 *
 *
 *
 */
@Singleton
public class PlatformMgr implements DocumentListener, SecurityAware, Provider<Platform> {

	//TODO: temp
	public boolean isUpgradeInProgress = false;

	static final Logger logger = Logger.getLogger(PlatformMgr.class);

	public PlatformNode getMe() {
		PlatformNode me = getPlatform().getNode(EnvironmentSettings.getHost());

		//TODO: localhost may still be valid at the moment
		if (me == null) {
			me = getPlatform().getNode("localhost");
		}

		return me;
	}

	@Override
	public Platform get() {
		return getPlatform();
	}

	/**
	 * Create a platform without a message sender
	 */
	public PlatformMgr() {
		this((Messenger<String>)null);
	}

	/**
	 * Create a platform with the given messenger
	 *
	 * @param messenger The message sender
	 */
	public PlatformMgr(Messenger<String> messenger) {
		this.messenger = messenger;
	}

    public PlatformClient newClient() {
    	return new PlatformClient(this);
    }

    /**
	 * @return Whether the process is set up to log platform errors
	 */
	private boolean isLoggingPlatformErrors() {
		String value = System.getProperty(Statics.PROP_LOG_PLATFORM_ERRORS);
		return value == null || Boolean.parseBoolean(value);
	}

	/**
	 * Sets the current platform
	 *
	 * @param platform The platform to set.
	 */
	public void setPlatform(Platform platform) {
		currentPlatform = platform;
	}

	/**
	 * Get the current platform.
	 *
	 * @return The current platform
	 */
	public Platform getPlatform() {

		if (currentPlatform == null) {
			initialize();
		}

		return currentPlatform;

	}

    public boolean isPlatformComplete() {

        Platform p = getPlatform();

		if(p.getNode(NodeRole.CONFIG_DB_MASTER) == null) {
			logger.warn("Postgres node not found during platform validation ");
			return  false;
		}

        if (p.getNode(NodeRole.ZOOKEEPER) == null) {
            logger.warn(" Zookeeper node not found during platform validation ");
            return false;
        }

        if (p.getNode(NodeRole.KAFKA) == null) {
            logger.warn(" Kafka node not found during platform validation ");
            return false;
        }

        if (p.getNode(NodeRole.HDFS_NAME) == null) {
            logger.warn(" HDFS Name node not found during platform validation ");
            return false;
        }

        if (p.getNode(NodeRole.HDFS_DATA) == null) {
            logger.warn(" HDFS Data node not found during platform validation ");
            return false;
        }

        if (p.getNode(NodeRole.SPARK_MASTER) == null) {
            logger.warn(" Spark MASTER node not found during platform validation");
            return false;
        }

        if (p.getNode(NodeRole.SPARK_WORKER) == null) {
            logger.warn(" Spark WORKER node not found during platform validation");
            return false;
        }

        if (p.getNode(NodeRole.REST) == null) {
            logger.warn(" REST node not found during platform validation");
            return false;
        }

        return true;


    }


	/**
	 * Perform platform initialization - initialize document store, set up rest cluster, etc
	 */
	public synchronized void initialize() {

		try {
			//get from disk first
			currentPlatform = getCachedPlatform();
			updateMessenger();
		}
		catch (Exception ex) {
			throw new PlatformInitializationException("error during platform initialization", ex);
		}

	}

	/**
	 * Update the current platform document, caching it to local storage
	 *
	 * @param platformDocument The new platform document to cache
	 * @throws Exception If anything goes wrong
	 */
	public void updateCurrent(Document platformDocument) throws Exception {

		logger.warn("node updating cached platform to " + platformDocument.getName() + "/v" + platformDocument.getVersion());

		Platform platform = platformDocument.getBodyAsObject();
		currentPlatform = platform;

		synchronized (this) {
			platformClient = null; //reset platform client
		}

		//back up old platform
		File curPlatform = new File(EnvironmentSettings.getPlatformPath());

		//cache new platform document to disk
		FileUtils.write(curPlatform, platformDocument.toJSON());

		//update the messenger
		updateMessenger();

	}

	private void updateMessenger() throws Exception {

		if (currentPlatform == null) {
			logger.warn(" current platform is null... cannot initialize messenger at this time");
			return;
		}

		//update the messenger
		if (currentPlatform.getNode(NodeRole.KAFKA) != null) {

			//close existing messenger if any
			if (messenger != null) {
				messenger.close();
			}

			messenger = new KafkaMessenger(currentPlatform);

		}
	}

	public synchronized void applyChanges(Platform platform) throws Exception {

		//TODO: apply arbitrary changes to the platform

	}

	public void reset() throws Exception {

		synchronized (this) {
			currentPlatform = null;
			platformClient = null;
		}

		//reset messenger
		messenger = null;

	}

	@Override
	public void onDocumentCreated(Document document) throws Exception {
		if (document.getDocumentType() == DocumentType.PLATFORM) {
			processPlatformChange(document);
		}
	}

	@Override
	public void onDocumentDeleted(Document document) throws Exception {
		if (document.getDocumentType() == DocumentType.PLATFORM) {
			processPlatformChange(document);
		}
	}

	@Override
	public void onDocumentUpdated(Document document) throws Exception {
		if (document.getDocumentType() == DocumentType.PLATFORM) {
			processPlatformChange(document);
		}
	}

	/**
	 * Update all node local caches with the supplied
	 */
	private void updateNodeCaches(Document platformDoc) throws Exception{

		//TODO: should we generalize parallel node communication with variable return types?
		//could also be used to assemble platform status

		int nodecount = getPlatform().getNodes().size();
		//notify all nodes in parallel
		Executor executor =  Executors.newFixedThreadPool(nodecount);
		CompletionService<Boolean> comms = new ExecutorCompletionService<>(executor);

		for (PlatformNode node: getPlatform().getNodes().values()) {
			comms.submit(() -> {
				return updateNodeCache(node, platformDoc);
			});
		}

		int expected = nodecount;
		int completed = 0;
		long start = System.currentTimeMillis();

		//wait until all are done
		while (completed < expected) {
			//wait up to 10 seconds for everything to start
			if (System.currentTimeMillis() - start < 10000L) {
				break;
			};
			//get the result
			comms.take().get();
			completed++;
		}

		if ((completed < expected)) {
			//TODO: do we track issues and schedule a retry? how to handle errors
		}

	}

	private boolean updateNodeCache(PlatformNode node, Document platformDoc) throws Exception{
		try {
			//TODO: inject a node client factory
			NodeClient nc = new DefaultNodeClient(node, this);
			nc.updatePlatformCache(platformDoc);
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			logger.error(" error updating node cache on node " + node.getHost());
			return false;
		}
	}


	/**
	 * @param document The platform document
	 */
	private void processPlatformChange(Document document) throws Exception {

		//logger.error("DEBUG : platform is changed");

		//it should always be system tenant, but just in case
		if (!document.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			logger.error(" platform change detected for non-system tenant:  " + document.getTenantID());
			return;
		}


		//TODO: compare new platform with current and generate more specific events
		Platform platform = document.getBodyAsObject();
		this.currentPlatform = platform;

		updateNodeCaches(document);

		//synchronize and let anyone know that a change occurred
		synchronized (this) {

			//update our local cached platform
			updateCurrent(document);
			notifyAll();

		}



		for (PlatformListener listener : m_listeners) {

			try {
				listener.onPlatformChange(platform);
			} catch (Exception e) {
				logger.error("error during platform change event", e);
			}

		}

	}

	public void addListener(PlatformListener listener) {
		m_listeners.add(listener);
	}

	public void removeListener(PlatformListener listener) {
		m_listeners.remove(listener);
	}

	public void handlePerfStats(OSPerfStats stats) throws  Exception {

		this.messenger.send(new StringMessage(Tenant.SYSTEM, Topic.PERF_STAT, JSONUtils.objectToString(stats)));

	}


	/**
	 * Handles exceptions in a generic way when the current thread cannot do anything more with it
	 *
	 * TODO: create endpoint for this
	 *
	 * @param log The message to send to logging
	 */
	public void handleLog(String log, NodeRole origin, NodeRole... related) {
		handleLog(EnvironmentSettings.getHost(), log, origin, related);
	}

	/**
	 * Handles error, will not rethrow.
	 *
	 * @param error The error object to handle
	 */
	public void handleException(Object error, NodeRole origin, NodeRole... related) {
		try {
			handleException(error, false, origin, related);
		} catch (Exception e) {
			logger.fatal("error handling exception", e);
			e.printStackTrace();
			if (error instanceof Throwable) {
				logger.fatal("original exception during handling error", (Throwable)error);
			}
		}
	}

	public void handleError(String error, NodeRole origin, NodeRole... related) {
		try {
			handleException(error, false, origin, related);
		} catch (Exception e) {
			//no checked exception will occur here
			throw (RuntimeException)e;
		}
	}

	/**
	 * Handle an exception in a generic way in the distributed platform. This is only called from spark for now.
	 *
	 * @param error The error to handle, may be an exception instance
	 * @param rethrow If an exception and rethrow is true, a generic Exception instance will be thrown to avoid ClassNotFoundErrors in the spark framework	 *
	 * @throws Exception If rethrow is true and the error is an Exception instance
	 */
	public void handleException(Object error, boolean rethrow, NodeRole origin, NodeRole... related) throws Exception {
		handleError(EnvironmentSettings.getHost(), error, origin, related);

		//see if we need to repackage the exception into a generic type
		if (rethrow && error instanceof Exception) {

			//tell spark for its own error handling mechanism
			throw new Exception(((Exception)error).getMessage());

		}
	}

	public void handleLog(String host, String log, NodeRole origin, NodeRole... related) {
		handleMessage(false, host, log, origin, related);
	}

	public void handleError(String host, Object error, NodeRole origin, NodeRole... related) {
		handleMessage(true, host, error, origin, related);
	}

	void handleMessage(boolean error, String host, Object message, NodeRole origin, NodeRole... related) {

		try {

			//and we are the master, log it
			if (error) {

				Tenant tenant = Tenant.forId(getTenantID());
				PlatformError platformError = null;

				if (message instanceof Exception) {

					if (isLoggingPlatformErrors()) {
						logger.error("exception from " + origin + " " + host, (Exception)message);
					}

					//add to error index
					platformError = new DefaultPlatformError(tenant, new DefaultUser(getUserID()), origin, Arrays.asList(related), (Exception)message);


				}

				else {

					if (isLoggingPlatformErrors()) {
						logger.error("Error from " + origin + " " + host + ":" + message);
					}

					//add to error index - TODO: refactor to use tenant instead of tenant ID
					platformError = new DefaultPlatformError(tenant, new DefaultUser(getUserID()), origin, Arrays.asList(related), String.valueOf(message));

				}


				//send the message
				messenger.send(new StringMessage(Tenant.SYSTEM, Topic.ERRORS, platformError));

			}

			else {

				//else connect to master and log it
				logger.info("log : " + message);

			}

		} catch (Exception e) {
			//just log locally if all else fails
			logger.error("error sending log message to master rest, if original message was exception, it will follow", e);

			if (message instanceof Exception) {
				logger.error("original exception", (Exception)message);
			}
		}

	}

	private synchronized PlatformClient getPlatformClient() {
		if (platformClient == null) {
			platformClient = new PlatformClient( this);
		}
		return platformClient;
	}
	private PlatformClient platformClient = null;

	/**
	 * Send the message payload on the requested topic for the current tenant
	 *
	 * @param topic The topic to send on
	 * @param payload The payload of the message to send
	 * @throws Exception If anything goes wrong
	 */
	public void sendMessage(Topic topic, Object payload) throws Exception {
		sendMessage(topic, getTenantID(), payload);
	}

	/**
	 * Send the message payload on the requested topic for the current tenant
	 *
	 * @param topic The topic to send on
	 * @param payload The payload of the message to send
	 * @throws Exception If anything goes wrong
	 */
	public void sendMessage(Topic topic, String payload) throws Exception {
		sendMessage(topic, getTenantID(), payload);
	}

	private void sendMessage(Topic topic, String tenantID, Object payload) throws Exception {

		messenger.send(new StringMessage(Tenant.forId(tenantID), topic, payload));

	}

	private void sendMessage(Topic topic, String tenantID, String payload) throws Exception {

		messenger.send(new StringMessage(Tenant.forId(tenantID), topic, payload));

	}

	/**
	 * Send the message to a topic with the given suffix
	 *
	 * @param topic The topic to send on
	 * @param tenant The current tenant
	 * @param payload The payload of the message to send
	 * @throws Exception If anything goes wrong
	 */
	public void sendMessage(Topic topic, Tenant tenant, Object payload) throws Exception {

		messenger.send(new StringMessage(tenant, topic, payload));

	}

	public void sendMessage(Topic topic, Tenant tenant, String payload) throws Exception {

		messenger.send(new StringMessage(tenant, topic, payload));

	}

	/**
	 * Add a file to the system HDFS hive (e.g. a patch file)
	 *
	 * @param purpose The purpose of the file
	 * @param filename The filename to use
	 * @param data The data itself
	 */
	public void addSystemFile(HadoopPurpose purpose,String path, String filename, InputStream data) throws Exception {

		try {

			//create a System file writer
			FileWriter writer = new FileWriter(this);
			writer.initialize(new PathResolverSystem());

			//TODO: decide on specific parameters for tolerating fault here
			if (writer.getStatus() != FileAccessorBase.Status.DFS_UP) {
				logger.warn(" DFS is not known to be available.  Using local cache ");
			}

			writer.writeStreamToFile(purpose, new org.apache.hadoop.fs.Path(path), filename, data);



		} catch (Exception e) {

			//log it
			logger.error("error adding file " + filename + "  to System HDFS hive for purpose " + purpose, e);

			throw e;

		}

	}




	/**
	 * @param purpose The purpose containing the details for the file
     * @param tenant The tenant to which this file belongs
     * @param machineID The machine ID to which this file belongs
     * @param filename The filename to use
     * @param guid A guid related to this file
     * @param data The data itself
	 * @param sequenceNumber The sequence number of this piece of the file
	 */
    public void addFilePart(HadoopPurpose purpose, Tenant tenant, String machineID, String filename, String guid, InputStream data, int sequenceNumber) throws Exception {

    	//add the file with the sequence number at the end
    	addFile(purpose, null, tenant, machineID, FileWriter.toPartName(filename, sequenceNumber), guid, data);

    }

    /**
     * Add a complete file to the system. If topic is not null, a message will be sent to the platform telling it about the new file on the topic specified.
     *
     * @param purpose The purpose of the file
     * @param topic The topic if the platform should be told about it
     * @param tenant The tenant to which this file belongs
     * @param subPath The machine ID to which this file belongs
     * @param filename The filename to use
     * @param guid A guid related to this file
     * @param data The data itself
     */
    public void addFile(HadoopPurpose purpose, Topic topic, Tenant tenant, String subPath, String filename, String guid, InputStream data) throws Exception {

    	try {

    		//first write the file to HDFS as the tenant
    		Impersonation.impersonateTenant(tenant.getTenantID());

    		//everything for a machine goes under that parent path
    		String path = toPath(subPath);

            FileWriter w = getTenantFileWriter();

            //TODO: decide on specific parameters for tolerating fault here -- is this still needed?
            if (w.getStatus() != FileAccessorBase.Status.DFS_UP) {
                logger.info(" DFS is not known to be available.  Using local cache ");
            }
    		getTenantFileWriter().writeStreamToFile(purpose, new org.apache.hadoop.fs.Path(path), filename, data);

    		if (topic != null) {

    			sendFileMessage(purpose, topic, tenant, subPath, filename, guid, false);

    		}



    	} catch (Exception e) {

    		//log it
    		logger.error("error adding bundle for tenant " + tenant, e);

    		throw e;

    	}

    }

	private void resetTenantFileReader() throws  Exception{
		FileReader reader = new FileReader(this);
		reader.initialize(new PathResolverTenant());
		setTenantFileReader(reader);
	}

	public synchronized FileReader getTenantFileReader() throws Exception{
		if (m_fileReader == null) {
			resetTenantFileReader();
		}
		return m_fileReader;
	}

	public synchronized void setTenantFileWriter(FileWriter writer)
	{
		m_fileWriter = writer;
	}

	public synchronized void setTenantFileReader(FileReader reader)
	{
		m_fileReader = reader;
	}

	private void resetTenantFileWriter() throws Exception{
		FileWriter writer = new FileWriter(this);
		writer.initialize(new PathResolverTenant());
		setTenantFileWriter(writer);
	}


	public synchronized FileWriter getTenantFileWriter() throws Exception{
		if (m_fileWriter == null) {
			resetTenantFileWriter();
		}
		return m_fileWriter;
	}

    /**
     * Complete a file that had parts written to the platform previously.
     *
     * @param purpose The purpose of the file
     * @param topic The topic if the platform should be told about it
     * @param tenant The tenant to which this file belongs
     * @param subPath The machine ID to which this file belongs
     * @param filename The filename to use
     * @param guid A guid related to this file
     * @throws Exception If anything goes wrong
     */
    public void completeFile(HadoopPurpose purpose, Topic topic, Tenant tenant, String subPath, String filename, String guid) throws Exception {

    	//add the file with the sequence number at the end
    	sendFileMessage(purpose, topic, tenant, subPath, filename, guid, true);

    }

    private void sendFileMessage(HadoopPurpose purpose, Topic topic, Tenant tenant, String subPath, String filename, String guid, boolean hasParts) throws Exception {

    	//everything for a machine goes under that parent path
		String path = toPath(subPath);

		FileInputMetadata metadata = new FileInputMetadata(purpose, tenant.getTenantID(),subPath, path, filename, guid, hasParts);

		//send the message
		messenger.send(new StringMessage(tenant, topic, metadata.toString())); //send the message to kafka

		logger.error(" DEBUG: File message placed on topic " + topic.getTopicName() );

    }

    //build the HDFS path for a machine
    private String toPath(String machineID) {
    	return org.apache.hadoop.fs.Path.SEPARATOR + machineID;
    }

	public static Document getCachedPlatformDoc() {
		File platformFile = new File(EnvironmentSettings.getPlatformPath());

		//if neither exists, return null
		if (!platformFile.exists()) {

			logger.info("no cached platform found: " + platformFile);
			return null;
		}

		String s = "";
		try {
			s = FileUtils.readFileToString(platformFile);

			logger.debug(" Initializing platform from disk file " + platformFile.getAbsolutePath());
			Document doc = new Document(s);
			return doc;
		}
		catch (JSONException jex) {
			throw new RuntimeException("JSON Exception parsing string: '" + s + "'" , jex);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}


	/**
	 * @return The cached platform, or null if no platform is cached
	 */
    public static Platform getCachedPlatform() {

		try {

			Document doc = getCachedPlatformDoc();

			//if we can't determine platform, return null
			if (doc == null) {
				return null;
			}

			Platform p = doc.getBodyAsObject((DocumentHandler)null);
			return p;

		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}
	//the platform
	Platform currentPlatform = null;

	//platform message sender
	Messenger<String> messenger = null;

	//the listeners
	private List<PlatformListener> m_listeners = new CopyOnWriteArrayList<>();

	private FileWriter m_fileWriter;

	private FileReader m_fileReader;


}
