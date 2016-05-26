/*
package com.aw.compute.streams.processor;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.messaging.Topic;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.util.JSONHandler;
import com.aw.common.util.JSONStreamUtils;
import com.aw.common.util.ResourceManager;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.processor.edr.WindowsLogParser;
import com.aw.compute.streams.processor.edr.WmiParser;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.unity.dg.CommonField;
import com.aw.unity.json.DefaultJSONDataTypeResolver;
import com.aw.util.DateUtil;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

*/
/**
 * Process edr scan zips from hdfs
 *
 *
 *
 *//*

public class EDRProcessor implements HDFSFileProcessor, JSONHandler, Dependent {

	private static final long serialVersionUID = 1L;

	static final Logger logger = Logger.getLogger(EDRProcessor.class);

	//holds the guid for a piece of data from an edr scan
	public static final String DG_GUID = "dg_guid";

	public static final String DG_SCAN_ID = "dg_sid";

	public Topic getDestTopic() { return destTopic; }
	private Topic destTopic;

	*/
/**
	 * The data that is currently being processed
	 *//*

	public enum FileType {

		*/
/**
		 * for now static data is just one topic, may be multiples eventually - note these topics
		 * map to data types in unity
		 *//*

		STATIC_DATA(Topic.ON_DISK_EXECUTABLE, "staticdata"),

		WINDOWS_REGISTRY(Topic.WINDOWS_REGISTRY, "registryinfo.json"),

		WINDOWS_LOGS(Topic.WINDOWS_LOG, "evtlog_"),

		NETWORK_CONNECTIONS(Topic.NETWORK_CONNECTION, "netstat"),

		WMI_DATA(Topic.WMI_DATA, "_wmic_"),

		RUNNING_PROCESSES(Topic.RUNNING_PROCESS, "activedata.json");

		private FileType(Topic topic, String substring) {
			m_topic = topic;
			m_substring = substring;
		}

		*/
/**
		 * @return The substring within the file name that identifies the file type
		 *//*

		public String getSubstring() { return m_substring; }
		private String m_substring;

		public Topic getTopic() { return m_topic; }
		private Topic m_topic;

	}

	@Override
	public void init(StreamDef streamDef) throws ProcessorInitializationException {
		destTopic = streamDef.getDestTopic();
	}

	@Override
	public void processFile(FileInputMetadata metadata, InputStream in) throws Exception {
		logger.trace("EDR Processor processing file: " + metadata.getFilename() + " for tenant " + metadata.getTenantID() + " using thread " + Thread.currentThread().getName());

		//set our metadata accordingly
		//m_metadata = metadata;

		EDRProcessingContext pc = new EDRProcessingContext(metadata);

		pc.setProducer(getProducer());
		pc.setZipIn(new ZipArchiveInputStream(new BufferedInputStream(in)));
		pc.setScanId(metadata.getGuid());

		//keep track of anything we don't process
		List<String> unknownFiles = new ArrayList<String>();

		ArchiveEntry data = pc.getZipIn().getNextEntry();

		while (data != null) {

			pc.setCurFile(data);
			pc.setFileType(evaluateFile(pc));

			//don't do anything if we can't determine the file type
			if (pc.getFileType() != null) {

				//handle the file
				processFile(pc);

			}

			//next
			data = pc.getZipIn().getNextEntry();

		}

		if (unknownFiles.size() > 0) {
			logger.info("unknown files not handled: " + unknownFiles);
		}

	}

	private void processFile(EDRProcessingContext pc) throws Exception {

		//process the file based on the type of data in it
		switch (pc.getFileType()) {

			case STATIC_DATA:
				JSONStreamUtils.processArrayElements("OnDiskExecutables", pc.getZipIn(), this, pc);
				break;

			case WINDOWS_REGISTRY:
				JSONStreamUtils.processArrayElements("RegistryInfo", pc.getZipIn(), this, pc);
				break;

			case NETWORK_CONNECTIONS:
				//TODO: implement this
				break;

			case RUNNING_PROCESSES:
				JSONStreamUtils.processArrayElements("Running", pc.getZipIn(), this, pc);
				break;

			case WINDOWS_LOGS:
				processWindowsLog(pc);
				break;

			case WMI_DATA:
				processWmi(pc);
				break;

		}

	}

	protected void log(String log) {

		getDependency(PlatformMgr.class).handleLog(log, NodeRole.SPARK_WORKER);

	}

	private Producer<String, String> getProducer() throws Exception {
		return ResourceManager.KafkaProducerSingleton.getInstance(getDependency(Platform.class));
	}

	@Override
	public void handleJson(String json, long ordinal, Object context) throws Exception {

		EDRProcessingContext pc = (EDRProcessingContext) context;

		//only process if we have a valid file type
		if (pc.getFileType() == null) {
			return;
		}

		JSONObject object = new JSONObject(json);

		switch (pc.getFileType()) {
			case STATIC_DATA:
				fixTimestampsInExecutables(object, pc);
				break;
			default:
				break;
		}

		addJson(object, pc);

	}

	*/
/**
	 * The data is ready to go at this point, minus the guid and unity type which will be injected into the
	 * json object.
	 *
	 * @param object The JSON to add to the system
	 *//*

	private void addJson(JSONObject object, EDRProcessingContext pc) throws Exception {

		putCommon(object, pc);

		//send the message to kafka on the appropriate topic based on our current state
		KeyedMessage<String, String> message = new KeyedMessage<String, String>(
			Topic.toTopicString(pc.getMetadata().getTenantID(), pc.getTopic())
				, pc.getMetadata().getTenantID(), object.toString());
		sendMessage(message, pc);

	}

	*/
/**
	 * Put the common properties for all scan elements into the json before sending it along
	 *
	 * @param object The new json data we need to populate with common scan element properties
	 * @throws Exception If anything goes wrong
	 *//*

	private void putCommon(JSONObject object, EDRProcessingContext pc) throws Exception {

		//add our time based on the file
		object.put(CommonField.DG_TIME.toString(), pc.getCurFile().getLastModifiedDate().getTime());

		//add an id - random uuid for now
		object.put(DG_GUID, pc.getScanId() + "-" + pc.getCurFile().getName() + "-" + pc.nextId());

		//set type - for edr data, the topic is the data type
		object.put(DefaultJSONDataTypeResolver.DG_UNITY_TYPE, pc.getFileType().getTopic().toString());

		//add the scan id
		object.put(DG_SCAN_ID, pc.getScanId());

	}

	//fix the crazy hex timestamps: 1d0d6b72c9b37eb and the valid from/to stufff
	private void fixTimestampsInExecutables(JSONObject object, EDRProcessingContext pc) throws Exception {

		JSONObject timestamps = object.optJSONObject("Timestamps");

		//don't do anything if it's not there
		if (timestamps == null) {
			return;
		}

		//change every key
		Iterator<String> keys = timestamps.keys();
		while (keys.hasNext()) {

			String key = keys.next();
			String oldKey = timestamps.getString(key);

			//parse in the hex
			long time = Long.parseLong(oldKey, 16);

			//output the final value
			timestamps.put(key, DateUtil.fromMSToUnixTime(time));

		}

		//fix digital cert timestamps
		JSONArray array = object.optJSONArray("DigitalCertificate");
		if (array != null) {
			for (int x=0; x<array.length(); x++) {

				JSONObject cert = array.getJSONObject(x);

				//from
				String from = cert.optString("ValidFrom", null);
				if (from != null) {
					cert.put("ValidFrom", pc.CERTIFICATE_TIMESTAMP.parse(from).getTime());
				}

				//to
				String to = cert.optString("ValidTo", null);
				if (to != null) {
					cert.put("ValidTo", pc.CERTIFICATE_TIMESTAMP.parse(to).getTime());
				}

			}
		}

	}

	*/
/**
	 * Actually send the message
	 *//*

	protected void sendMessage(KeyedMessage<String, String> message, EDRProcessingContext pc) throws Exception {
		pc.getProducer().send(message);
	}

	private FileType evaluateFile(EDRProcessingContext pc) throws Exception {

		FileType ret = null;

		if (pc.getCurFile() != null) {

			//do our if to look for matching substrings
			for (FileType fileType : FileType.values()) {

				if (pc.getCurFile().getName().toLowerCase().contains(fileType.getSubstring())) {
					ret = fileType;
					break;
				}

			}

		}

		//return the file type or null if we couldn't determine the type
		return ret;

	}

	//parse the wmi data and add the json messages
	private void processWmi(EDRProcessingContext pc) throws Exception {

		WmiParser parser = new WmiParser(pc.getCurFile(), pc.getZipIn());
		addJson(parser, pc);

	}

	//parse the windows event log data and add the json messages
	private void processWindowsLog(EDRProcessingContext pc) throws Exception {

		WindowsLogParser parser = new WindowsLogParser(pc.getCurFile(), pc.getZipIn());
		addJson(parser, pc);

	}

	*/
/**
	 * Add all of the json data from this source
	 *
	 * @param source the source of json objects to ingest
	 * @throws Exception if anything goes wrong
	 *//*

	private void addJson(Iterable<JSONObject> source, EDRProcessingContext pc) throws Exception {
		for (JSONObject data : source) {
			if (data != null) {
				addJson(data, pc);
			}
		}
	}

	*/
/**
	 * A single-thread-compatible processing context for a single edr scan
	 *//*

	public class EDRProcessingContext{

		public final SimpleDateFormat CERTIFICATE_TIMESTAMP = new SimpleDateFormat("MM/dd/yyyy");

		public EDRProcessingContext(FileInputMetadata meta) {
			m_metadata = meta;
		}

		*/
/**
		 * @return The next unique id for a piece of data from a single scan - should only be called from single thread
		 *//*

		public int nextId() {
			return ++m_id;
		}
		private int m_id;

		public Topic getTopic() { return m_fileType.getTopic(); }

		public FileType getFileType() { return m_fileType; }
		public void setFileType(FileType fileType) { m_fileType = fileType; }
		private FileType m_fileType = null;

		//current metadata
		public FileInputMetadata getMetadata() { return m_metadata; }
		public void setMetadata(FileInputMetadata metadata) { m_metadata = metadata; }
		private FileInputMetadata m_metadata = null;

		//the kafka producer
		public Producer<String, String> getProducer() { return m_producer; }
		public void setProducer(Producer<String, String> producer) { m_producer = producer; }
		private Producer<String, String> m_producer = null;

		//the scan id being processed
		public String getScanId() { return m_scanId; }
		public void setScanId(String scanId) { m_scanId = scanId; }
		private String m_scanId = null;

		//current zip archive stream being processed
		public ZipArchiveInputStream getZipIn() { return m_zipIn; }
		public void setZipIn(ZipArchiveInputStream zipIn) { m_zipIn = zipIn; }
		private ZipArchiveInputStream m_zipIn;

		//the current file being processed within the current scan
		public ArchiveEntry getCurFile() { return m_curFile; }
		public void setCurFile(ArchiveEntry curFile) { m_curFile = curFile; }
		private ArchiveEntry m_curFile;

	}

}


*/
