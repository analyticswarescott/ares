package com.aw.compute.streams.drivers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.system.structure.Hive;
import com.aw.common.util.JSONUtils;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import org.apache.commons.lang.ObjectUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContextState;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.spark.DriverDef;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.DriverInitializationException;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.processor.framework.ActiveStream;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import com.aw.util.ListMap;
import com.aw.utils.kafka.KafkaOffsetUtils;

/**
 * The master driver class. This class will phone home for stream configurations that are meant for it. As it finds
 * new processors it will spawn new streams for them.
 *
 *
 */
public class KafkaDriver implements Driver, Dependent {
	public static final Logger logger = LoggerFactory.getLogger(KafkaDriver.class);


	public static final String HEARTBEAT_OK = "registered";
	public static final String STOP_REQUESTED = "stop";

	public static int default_parallelism = 10;

	private boolean isStopping = false;

	ScheduledExecutorService daemons = Executors.newScheduledThreadPool(2);
	private PlatformMgr platformMgr;
	private Provider<DocumentHandler> docs;
	private PlatformClient client;


	private ZkAccessor tenantZkAccessor;  //TODO: add to injector


	public static void main(String[] args) throws Exception {
		//for testing

		if (args.length > 1) {
			String dbVendor = args[1];
			System.setProperty("DB_VENDOR", dbVendor);
		}

		KafkaDriver d = ComputeInjector.get().getInstance(KafkaDriver.class);
		d.initialize(args);
		d.start();
	}

	public KafkaDriver() {
		this(
			ComputeInjector.get().getInstance(PlatformMgr.class),
			ComputeInjector.get().getProvider(DocumentHandler.class)
		);
	}

	@Inject @com.google.inject.Inject
	public KafkaDriver(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		this.platformMgr = platformMgr;
		this.docs = docs;
		this.rest = new ComputeRestClient(platformMgr.getPlatform());
	}

	@Override
	public void initialize(String... arguments) throws Exception {


	/*	if (arguments.length != 1) {
			throw new DriverInitializationException("Usage: \"<driver_name> " + Arrays.toString(arguments));
		}*/

		//driver name is the first argument
		driverRootName = arguments[0];



		SecurityUtil.setThreadSystemAccess();

		//get def

		m_def = docs.get().getDocument(DocumentType.STREAM_DRIVER, driverRootName).getBodyAsObject();



	}

	protected SparkConf createSparkConf() {
		return new SparkConf();
	}

	@Override
	public void start() throws Exception {

		//TODO: add to injector
		tenantZkAccessor = new DefaultZkAccessor(platformMgr.getPlatform(), Hive.TENANT);

		SparkConf sparkConf = createSparkConf();

		String defPara = sparkConf.get("spark.default.parallelism");
		if (defPara != null) {
			default_parallelism = Integer.parseInt(defPara);
		}

		//create the spark context (not streaming, but the overall spark context)
		m_sc = new JavaSparkContext(sparkConf);

		//get the driver properties from the spark context
		m_driverID = m_sc.env().executorId();
		m_driverName = m_sc.appName();

		//TODO: is a timeout needed here, and how would we know what is appropriate
		while (!register()) {
			logger.warn(" registration not complete, trying again in 1s ");
			Thread.sleep(1000);
		}

		logger.info("Driver " + m_driverName + " running and registered");

		//setup registration heartbeat every 3 seconds //TODO: add this to driver def as a setting
		Heartbeater hb = new Heartbeater();
		daemons.scheduleAtFixedRate(() -> hb.run(), m_def.getHeartbeatInterval(), m_def.getHeartbeatInterval(), TimeUnit.SECONDS);


		//start polling for streams to start
		Handshaker hs = new Handshaker();
		daemons.scheduleAtFixedRate(() -> hs.run(), m_def.getWorkPollDelay(), m_def.getWorkPollInterval(), TimeUnit.SECONDS);

		//wait for signal to apply work
		waitForWork();

	}

	/**
	 * Wait until the handshaker has work for us
	 */
	void waitForWork() throws Exception {

		do {

			try {

				//wait for work
				Streams update = streamUpdates.take();

				//stop, apply, start
				stopStreamingContext();
				applyUpdate(update);
				currentStreams = update;
				startStreamingContext();

			} catch (Exception e) {
				getClient().logError(e, NodeRole.SPARK_WORKER);
			}

		} while (running);

	}

	/**
	 * Once we get a unit of work
	 *
	 * @throws Exception
	 */
	private void startStream(Tenant tenant, StreamDef streamDef) throws Exception {

		//depending on whether it's a single tenant or global, we will use either GlobalProcessorFunction or TenantProcessorFunction

		Impersonation.impersonateTenant(tenant);
		try {
			//verify stream def settings
			KafkaOffsetUtils.verify(tenantZkAccessor, platformMgr.getPlatform(), tenant, streamDef.getProcessorName(tenant), streamDef.getSourceTopicNames(tenant)); //TODO: clean this verify method up

			//create the active stream and store it
			ActiveStream stream = createStream(tenantZkAccessor, tenant, streamDef);
			m_streams.put(streamDef.getProcessorName(tenant), stream);
		} finally {
			Impersonation.unImpersonate();
		}



	}

	/**
	 * Apply a new set of streams
	 *
	 * @param update The updated streams for this driver
	 * @throws Exception If anything goes wrong
	 */
	void applyUpdate(Streams update) throws Exception {

		//start streaming context

		m_jssc = new JavaStreamingContext(m_sc, Durations.seconds(m_def.getBatchIntervalSeconds()));

		for (Map.Entry<Tenant, List<StreamDef>> entry : update.getTenantToStreams().entrySet()) {

			for (StreamDef def : entry.getValue()) {

				try {
					long l = System.currentTimeMillis();
					startStream(entry.getKey(), def);
					long elapsed =  System.currentTimeMillis();
					logger.debug("DEBUG: elapsed time to define stream " + elapsed);
				} catch (Exception e) {
					platformMgr.handleException(e, NodeRole.SPARK_WORKER);
				}

			}

		}

	}

	void stopStreamingContext() throws Exception {

		//stop if we are running
		if (m_jssc != null) {

			logger.info("stopping streams");
			if (m_jssc.getState() == StreamingContextState.ACTIVE || m_jssc.getState() == StreamingContextState.INITIALIZED) {
				long l = System.currentTimeMillis();
				m_jssc.stop(false, true);
				m_jssc.awaitTermination();
				long elapsed =  System.currentTimeMillis();
				logger.debug("DEBUG: elapsed time to start streaming context " + elapsed);

			} else {

				//force a stop if we are in an unexpected state and log it
				logger.warn(" unexpected streaming context state is " + m_jssc.getState().toString());
				m_jssc.stop(false, false);

			}

		}

	}

	void startStreamingContext() throws Exception {
		long l = System.currentTimeMillis();
		logger.info("starting streams");
		long elapsed =  System.currentTimeMillis();
		logger.debug("DEBUG: elapsed time to start streaming context " + elapsed);
		m_jssc.start();

	}

	/**
	 * Create an active stream from a stream def
	 *
	 * @param streamDef The stream def holding the properties for our spark streaming job
	 * @return The active stream
	 * @throws Exception If anything goes wrong
	 */
	private ActiveStream createStream(ZkAccessor zk, Tenant tenant, StreamDef streamDef) throws Exception {

		try {

			ActiveStream activeStream = new ActiveStream(zk, platformMgr, tenant, streamDef);
			activeStream.start(m_jssc);
			registerProcessor(tenant, streamDef);
			return activeStream;

		}
		catch (Exception ex) {
			throw new ProcessorInitializationException(" processor could not be started " + ex.getMessage(), ex);
		}

	}

	protected void stopProcessing() {

		logger.warn(" stopping JSSC due to stop instruction receipt");
		m_jssc.stop(false, true);
		logger.warn(" stopping Spark Context (driver) due to stop instruction receipt");
		m_sc.stop();
		daemons.shutdownNow();
		System.exit(0);



	}

	protected boolean register() throws Exception{

		try {

			DriverRegistrationResponse result =  rest.register(driverRootName, m_driverName, m_driverID);

			logger.debug("register result was ------------- " + result);

			if (result == DriverRegistrationResponse.STOP) {
				isStopping = true;
				stopProcessing();
				return true;
			}
			else {
				return true;
			}

		}
		catch (DriverInitializationException dex) {
			logger.warn("error trying to register " + dex.getMessage());
			return false;
		}
		catch (Exception ex) {
			platformMgr.handleException(ex, NodeRole.SPARK_MASTER);
			return false;
		}



	}

	private PlatformClient getClient() {
		if (client == null) {
			client = new PlatformClient(platformMgr.getPlatform());
		}
		return client;
	}

	protected void registerProcessor(Tenant tenant, StreamDef streamDef) throws Exception{

		rest.registerProcessor(m_driverName, streamDef.getProcessorName(tenant));

	}

	private class ContextHolder implements Runnable {


		@Override
		public void run() {

			//start/re-start processing
			m_jssc.start();

			logger.info("* streaming context started : thread : " + Thread.currentThread().getName());
			m_jssc.awaitTermination();
			logger.info("*** context stopped : thread :" + Thread.currentThread().getName());

		}

	}

	protected class Heartbeater implements Runnable {

		@Override
		public void run() {
			try {
				//logger.debug("HEARTBEAT to: platform REST URL " + EnvironmentSettings.getReportingServiceBaseURL());

				if (register()) {

					//if registration succeeds, re-register current streams
					ActiveStream[] streams = m_streams.values().toArray(new ActiveStream[m_streams.size()]);
					for ( ActiveStream activeStream : streams)	 {

						getDependency(PlatformMgr.class).handleLog("\n\n\n\nregistering " + activeStream.getTenant().getTenantID() + "/" + activeStream.getStreamDef().getProcessorId(), NodeRole.SPARK_MASTER);
						registerProcessor(activeStream.getTenant(), activeStream.getStreamDef());

					}

				}
			} catch (Exception e) {
				System.err.println("heartbeat failed");
				e.printStackTrace(System.out);
				logger.warn(" heatbeat failed:  error was " + e.getMessage());
			}
		}

	}


	protected class Handshaker implements Runnable, TenantAware {
      private final Logger logger = LoggerFactory.getLogger(Handshaker.class);

		public Handshaker() {

		}

		@Override
		public void run() {

			try {

				//get list of streams

				if (isStopping) {
					logger.debug(" Stop initiated. Worklist call cancelled. ");
					return;
				}

				SecurityUtil.setThreadSystemAccess();

				//spark stdout log TODO: remove this?
				logger.debug("calling REST for work");

				//impersonate and add to list
				Collection<Document> tenants = KafkaDriver.this.docs.get().getAllTenants();

				//add global streams
				List<Document> globalDefs = KafkaDriver.this.docs.get().getDocumentsOfType(DocumentType.STREAM_GLOBAL);

				//map of tenant -> stream
				ListMap<Tenant, StreamDef> streams = new ListMap<Tenant, StreamDef>();
				streams.put(Tenant.SYSTEM, toStreams(globalDefs));

				//add tenant streams
				for (Document tenantDoc : tenants) {
					Tenant tenant = Tenant.forId(tenantDoc.getName());
					for (StreamDef def : getStreams(tenant)) {
						if (tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)  && !def.isSystem()) {
							logger.debug("skipping tenant stream not marked as system " + def.getProcessorId());
						}
						else {
							streams.add(tenant, def);
						}

					}
				}

				//keep only our streams
				Streams update = new Streams(streams);

				//see if we need to do anything, if so queue it
				if (!ObjectUtils.equals(last, update)) {
					streamUpdates.add(update);
					last = update;
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error communicating with REST controller ", e);
			}


		}

		private Streams last;

	}

	/**
	 * Get streams for the tenant
	 *
	 * @param tenant The tenant whose streams are needed
	 * @return The collection of StreamDefs
	 * @throws Exception If anything goes wrong
	 */
	private Collection<StreamDef> getStreams(Tenant tenant) throws Exception {

		Collection<StreamDef> ret = new ArrayList<StreamDef>();

		//not system tenant TODO: include system tenant
		//if (!tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {

			Impersonation.impersonateTenant(tenant);

			try {

				return toStreams(KafkaDriver.this.docs.get().getDocumentsOfType(DocumentType.STREAM_TENANT));

			} finally {
				Impersonation.unImpersonate();
			}

		//}


		//return ret;

	}

	/**
	 * Convert stream documents to StreamDefs
	 *
	 * @param docs
	 * @return
	 * @throws Exception
	 */
	private List<StreamDef> toStreams(Collection<Document> docs) throws Exception {

		List<StreamDef> ret = new ArrayList<StreamDef>();
		for (Document doc : docs) {
			StreamDef def = doc.getBodyAsObject();
			if (doc.getDocumentType() == DocumentType.STREAM_GLOBAL) {
				def.setisGlobal(true);
			}

			//make sure the stream is ours
			if (def.getTargetDriver().equals(this.driverRootName)) {
				ret.add(doc.getBodyAsObject());
			}
		}

		return ret;

	}

	/**
	 * All streams that are currently active in spark
	 */
	protected HashMap<String, ActiveStream> m_streams = new HashMap<String, ActiveStream>();

	/**
	 * The spark context
	 */
	private JavaSparkContext m_sc;

	/**
	 * The spark streaming context
	 */
	private JavaStreamingContext m_jssc;

	/**
	 * The configuration for this driver
	 */
	DriverDef m_def;

	/**
	 * The ID of this driver (spark executor name)
	 */
	String m_driverID;

	/**
	 * The name of this driver (spark app name)
	 */
	String m_driverName;

	/**
	 * The name given to this driver as an argument when started
	 */
	String getDriverRootName() { return driverRootName; }
	String driverRootName;

	/**
	 * The queue of updated streams - when an update occurs, the queue will be added to and the driver
	 * will pull that work off of the queue and update the streams accordingly.
	 */
	BlockingQueue<Streams> getStreamUpdates() { return streamUpdates; }
	private BlockingQueue<Streams> streamUpdates = new ArrayBlockingQueue<>(100);

	/**
	 * The currently executing streams
	 */
	private Streams currentStreams;

	/**
	 * The REST client used to communicate with the cluster
	 */
	private ComputeRestClient rest;

	/**
	 * @param running whether the kafka driver is running
	 */
	void setRunning(boolean running) { this.running = running; }
	boolean running = true;


}
