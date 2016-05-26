package com.aw.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.zookeeper.ZooKeeper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.util.ListMap;

import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;

/**
 * Fetch test data from kafka. The point of this tool is to make it easy to grab data from all of the
 * kafka topics for a given tenant. The output of this tool, if configured to do so, will be a set
 * of .json files - one for each of the tenant's topics. The tenant name is stripped from the filenames
 * for brevity.
 *
 * <li>If using the command line interface, -list-tenants will list the tenants on the kafka broker.
 *
 * <p>Example command line:</p>
 *
 *  <code>java -cp "lib/*" com.aw.util.KafkaTenantDump -broker dev-dg8-01:9202 -out ../../test/resources/topicdata -zoo dev-dg8-01:2181</code>
 *
 *
 *
 */
public class KafkaTenantDump implements Runnable {

	/**
	 * tenant pattern for topics - used to pull tenants
	 */
	private static final Pattern TENANT_TOPIC_PATTERN = Pattern.compile("(\\S+?)_\\S+");

	/**
	 * list tenants
	 */
	public static final String LIST = "list";
	public static final String LIST_DESC = "list available tenants";

	/**
	 * Path to brokers on zookeeper
	 */
	public static final String BROKER_PATH = "/brokers/ids";

	private static final int FETCH_SIZE = 100000000;

	/**
	 * Character at the end of a tenant ID on kafka
	 */
	public static final String TENANT_SEPARATOR = "_";

	//zookeeper broker properties we read
	public static final String HOST = "host";
	public static final String PORT = "port";

	/**
	 * host:port of kafka broker
	 */
	public static final String BROKER = "broker";
	public static final String BROKER_DESC = "kafka broker host:port";
	public static final String BROKER_ARG = "host:port";

	/**
	 * Output path to write to
	 */
	public static final String OUT = "out";
	public static final String OUT_DESC = "output directory for topic data - defaults to .";
	public static final String OUT_ARG = "path";

	/**
	 * Tenant ID to pull
	 */
	public static final String TENANT = "tenant";
	public static final String TENANT_DESC = "tenant id used in topic names to dump";
	public static final String TENANT_ARG = "id";

	/**
	 * zookeeper host:port
	 */
	public static final String ZOO = "zoo";
	public static final String ZOO_DESC = "the zookeeper to communicate with";
	public static final String ZOO_ARG = "host:port";

	/**
	 * force new data
	 */
	public static final String FORCE = "force";
	public static final String FORCE_DESC = "force dump - overwrite existing json for a topic";

	public static final int CONSUMER_TIMEOUT = 10000;
	public static final int CONSUMER_BUFFER_SIZE = 2048 * 1024;
	public static final String CONSUMER_CLIENT_ID = "tenantDump";

	private static final byte[] EOL = "\n".getBytes();

	/**
	 * Configured from a command line
	 *
	 * @param cli The command line interface
	 * @throws Exception If anything goes wrong
	 */
	public KafkaTenantDump(CommandLine cli) throws Exception {

		initialize(cli);

	}

	/**
	 * No initialization performed, intended to be initialized as a bean in this case
	 */
	public KafkaTenantDump() {
	}

	protected void initialize(CommandLine cli) throws Exception {

		//set zookeeper info
		String zoo = cli.getOptionValue(ZOO);
		if (zoo == null) {
			zoo = "dev-dg8-01:2181";
//			throw new Exception("Must supply zookeeper host:port");
		}
		setZoo(zoo);

		//set whether the request is to list tenants
		setListTenants(cli.hasOption(LIST));

		//get tenant, optional parameter
		setTenant(cli.getOptionValue(TENANT, null));

		//set the output path if provided
		setOutPath(cli.getOptionValue(OUT, getOutPath()));

		//make sure the output path exists
		new File(getOutPath()).mkdirs();

		//force a dump over existing json
		setForced(cli.hasOption(FORCE));

	}

	public void run() {

		try {

			//if we're listing tenants just do that
			if (getListTenants()) {
				listTenants();
			}

			//else we're actually dumping data
			else {

				System.out.println("Scanning kafka for data..");

				//if forced, delete existing data here
				if (isForced()) {

					//delete all files if we're forcing an update
					File[] files = new File(getOutPath()).listFiles();
					for (File file : files) {
						file.delete();
					}

				}

				for (String tenant : getTenants()) {
					dumpForTenant(tenant);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();

			//let the entity doing the fetch handle this
			setException(e);
		}

	}

	private void dumpForTenant(String tenant) throws Exception {

		//get all topics we need - 1 stream per tenant
		Map<String, List<PartitionMetadata>> partitions = getPartitionsForTenant(tenant);

		//dump for each partition
		for (Map.Entry<String, List<PartitionMetadata>> entry : partitions.entrySet()) {

			//get the stuff we need from the map
			String topic = entry.getKey();

			for (PartitionMetadata partition : entry.getValue()) {

				//try to process data for this topic
				if (processPartition(topic, partition)) {

					//only need data from one partition per topic
					break;

				}

			}

		}

	}

	/**
	 * @param topic The topic to process
	 * @param partition The partition to process
	 * @return Whether any data was dumped
	 * @throws Exception If anything goes wrong
	 */
	private boolean processPartition(String topic, PartitionMetadata partition) throws Exception {

		//make sure there's a leader
		if (partition.leader() == null) {
			return false;
		}
		SimpleConsumer consumer = new SimpleConsumer(partition.leader().host(), partition.leader().port(), CONSUMER_TIMEOUT, CONSUMER_BUFFER_SIZE, CONSUMER_CLIENT_ID);

		try {

			//return whether we dumped the partition
			boolean ret = false;

			//if empty, continue
			long firstOffset =  getOffset(consumer, topic, partition.partitionId(), kafka.api.OffsetRequest.EarliestTime(), CONSUMER_CLIENT_ID);
			long lastOffset = getOffset(consumer, topic, partition.partitionId(), kafka.api.OffsetRequest.LatestTime(), CONSUMER_CLIENT_ID);
			if (lastOffset == firstOffset) {
				ret = false;
			}

			else {

				System.out.println("Found data in topic " + topic + " partition " + partition.partitionId() + " firstOffset=" + firstOffset + " lastOffset=" + lastOffset + ", dumping..");
				dumpData(consumer, topic, partition.partitionId(), firstOffset);
				System.out.println("Done.");

			}

			return ret;

		} finally {
			consumer.close();
		}

	}

	/**
	 * Dump data to the given file for the given topic/partition using the given consumer at the given offset
	 *
	 * @param file The file to dump to
	 * @param consumer The consumer to use
	 * @param topic The topic to dump
	 * @param partition The partition to dump
	 * @param offset The offset to dump from
	 */
	private void dumpData(SimpleConsumer consumer, String topic, int partition, long offset) throws Exception {

		long count = 0L;

		boolean fetch = true;
		while (fetch) {

			kafka.api.FetchRequest req = new FetchRequestBuilder()
					.clientId(CONSUMER_CLIENT_ID)
					.addFetch(topic, partition, offset, FETCH_SIZE)
					.build();

			kafka.javaapi.FetchResponse res = consumer.fetch(req);

			//this is just a simple tool, no tolerance for errors
			if (res.hasError()) {
				throw new Exception("Error fetching data: " + res.errorCode(topic, partition));
			}

			ByteBufferMessageSet msgSet = res.messageSet(topic, partition);

			System.out.println("writing out, offset=" + offset + " result size: " + msgSet.sizeInBytes());

			//else dump the data

			//dump as a json array - this will make it easy to send each message through a test
			Iterator<MessageAndOffset> iter = msgSet.iterator();

			//stop fetching when the result is empty
			fetch = iter.hasNext();

			while (iter.hasNext()) {
				MessageAndOffset msgAndOffset = iter.next();

				count++;

				ByteBuffer payload = msgAndOffset.message().payload();
				byte[] data = new byte[payload.limit()];
				payload.get(data);

				FileOutputStream fOut = null;
				try {

					//create the file we'll write to
					File file = new File(getOutPath(), topic.substring(topic.indexOf(TENANT_SEPARATOR) + 1) + "_" + count + ".json");
					fOut = new FileOutputStream(file);

					fOut.write(data);
					fOut.write(EOL);

				} finally {

					if (fOut != null) {
						//close the file
						fOut.flush();
						fOut.close();
					}

				}

				offset = msgAndOffset.nextOffset();
			}

			System.out.println("offset now" + offset);

		}
	}

	public static long getOffset(SimpleConsumer consumer, String topic, int partition, long time, String clientName) throws Exception {

		TopicAndPartition tap = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> reqInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		reqInfo.put(tap, new PartitionOffsetRequestInfo(time, 1));

		OffsetRequest request = new OffsetRequest(reqInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);

		if (response.hasError()) {
			throw new Exception("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
		}

		long[] offsets = response.offsets(topic, partition);
		return offsets[0];

	}

	/**
	 * Get partitions for the given list of topics
	 *
	 * @param topics
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<PartitionMetadata>> getPartitions(Collection<String> topics) throws Exception {

		//keep track of what we've found
		List<String> topicsLeft = new ArrayList<String>(topics);

		//return list of partitions
		ListMap<String, PartitionMetadata> ret = new ListMap<String, PartitionMetadata>();

		while (topicsLeft.size() > 0) {

			JSONArray brokers = getAllBrokers();
			for (int x=0; x<brokers.length(); x++) {

				//get the metadata
				SimpleConsumer consumer = null;

				try {

					consumer = connectSimpleConsumer(brokers.getJSONObject(x));
					TopicMetadataRequest tmReq = new TopicMetadataRequest(topicsLeft);
					TopicMetadataResponse tmRes = consumer.send(tmReq);

					//find ones for each topic
					for (TopicMetadata tmData : tmRes.topicsMetadata()) {

						//if there is a partition and we haven't hit this topic yet, store the metadata
						if (tmData.partitionsMetadata().size() > 0 && topicsLeft.remove(tmData.topic())) {
							ret.put(tmData.topic(), tmData.partitionsMetadata());
							break;
						}

					}

				} finally {
					if (consumer != null) {
						consumer.close();
					}
				}
			}

		}

		System.out.println("Found " + ret.size() + " partitions for " + topics.size() + " topics");

		//throw an exception if we couldn't find partitions
		if (topicsLeft.size() > 0) {
			throw new Exception("Couldn't find a partition for topics: " + topicsLeft);
		}

		return ret;

	}

	private SimpleConsumer connectSimpleConsumer(JSONObject broker) throws Exception {

		//get the host/port
		String host = broker.getString(HOST);
		int port = broker.getInt(PORT);

		//create a consumer to try to get all of the partition metadata we need
		SimpleConsumer consumer = connectSimpleConsumer(host, port);
		return consumer;

	}

	private Map<String, List<PartitionMetadata>> getPartitionsForTenant() throws Exception {

		//get any tenant if not specified
		String tenant = getTenant();
		if (tenant == null) {
			tenant = getTenants().iterator().next();
		}

		return getPartitionsForTenant(tenant);

	}

	private Map<String, List<PartitionMetadata>> getPartitionsForTenant(String tenant) throws Exception {

		Set<String> topics = getTopicsForTenant(tenant);
		return getPartitions(topics);

	}

	/**
	 * @param tenant The tenant whose topics are being requested
	 * @return The list of topic names for the provided tenant
	 * @throws Exception
	 */
	private Set<String> getTopicsForTenant(String tenant) throws Exception {

		//first get all topics
		Set<String> ret = getAllTopics();

		//remove any that aren't for the requested tenant
		Iterator<String> iter = ret.iterator();
		while (iter.hasNext()) {
			if (!iter.next().startsWith(tenant)) {
				iter.remove();
			}
		}

		return ret;

	}

	/**
	 * Output (for now to stdout) a list of tenants as a json array
	 *
	 * @throws Exception If anything goes wrong
	 */
	private void listTenants() throws Exception {

		//output the tenants as a json array
		Set<String> tenantList = getTenants();
		JSONArray out = new JSONArray(tenantList);
		System.out.println(out.toString(4));

	}

	/**
	 * @return all tenants from kafka based on topics
	 * @throws Exception
	 */
	private Set<String> getTenants() throws Exception {

		Set<String> ret = new HashSet<String>();

		//pull out tenants - up to first "_" in our topic names
		for (String topic : getAllTopics()) {

			//pull the tenant ID
			Matcher matcher = TENANT_TOPIC_PATTERN.matcher(topic);
			if (matcher.find()) {
				ret.add(matcher.group(1));
			}

		}

		//return the set of tenant names
		return ret;

	}

	private Set<String> getAllTopics() throws Exception {

		Set<String> ret = new HashSet<String>();

		//connect to the first broker we find
		SimpleConsumer consumer = connectSimpleConsumer();

		try {

			//send the request for topics
			TopicMetadataRequest req = new TopicMetadataRequest(new ArrayList<String>());
			TopicMetadataResponse res = consumer.send(req);

			//pull out tenants - up to first "_" in our topic names
			for (TopicMetadata topic : res.topicsMetadata()) {

				ret.add(topic.topic());

			}

			//return the set of tenant names
			return ret;

		} finally {

			//close the consumer when done
			consumer.close();

		}


	}

	private SimpleConsumer connectSimpleConsumer() throws Exception {


		JSONObject brokerCfg = getABrokerCfg();

		String host = brokerCfg.getString(HOST);
		int port = brokerCfg.getInt(PORT);

		return connectSimpleConsumer(host, port);

	}

	private SimpleConsumer connectSimpleConsumer(String host, int port) throws Exception {
		SimpleConsumer consumer = new SimpleConsumer(host, port, CONSUMER_TIMEOUT, CONSUMER_BUFFER_SIZE, CONSUMER_CLIENT_ID);
		return consumer;
	}

	private JSONObject getABrokerCfg() throws Exception {

		return getAllBrokers().getJSONObject(0);

	}

	/**
	 * @return The list of brokers from zookeeper
	 * @throws Exception If anything goes wrong
	 */
	private JSONArray getAllBrokers() throws Exception {

		JSONArray ret = new JSONArray();

		ZooKeeper zk = getZooKeeper();
		List<String> ids = zk.getChildren(BROKER_PATH, false);

		if (ids.size() == 0) {
			throw new Exception("No kafka brokers found");
		}

		for (String id : ids) {

			//add each broker to the array
			String brokerData = new String(zk.getData(BROKER_PATH + "/" + ids.get(0), false, null));
			JSONObject brokerCfg = new JSONObject(brokerData);
			ret.put(brokerCfg);

		}

		return ret;

	}

	public static void main(String[] args) {

		//create the initial cli options etc
		DumpOptions options = new DumpOptions();
		KafkaTenantDump dump = null;

		System.out.println("args: " + Arrays.toString(args));

		try {

			CommandLine commandLine = options.parse(args);

			//fetch the data
			dump = new KafkaTenantDump(commandLine);
			dump.run();

			if (dump.getException() != null) {
				dump.getException().printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("tenantDump", options);
		}

		//always try to clean up
		if (dump != null) {
			try {
				dump.cleanup();
			} catch (Exception e) {
				System.err.println("Error cleaning up");
				e.printStackTrace(System.err);
			}
		}

		//non-daemon threads running, just end the process now
		System.exit(0);

	}

	public void cleanup() throws Exception {
		if (m_zooKeeper != null) {
			m_zooKeeper.close();
			m_zooKeeper = null;
		}
	}

	/**
	 * @return the zookeeper, creates one if it doesn't exist yet
	 * @throws Exception
	 */
	public synchronized ZooKeeper getZooKeeper() throws Exception {
		if (m_zooKeeper == null) {
			m_zooKeeper = new ZooKeeper(m_zoo, 10000, null);
		}
		return m_zooKeeper;
	}
	public void setZooKeeper(ZooKeeper zooKeeper) { m_zooKeeper = zooKeeper; }
	private ZooKeeper m_zooKeeper;

	/**
	 * @return if we're just listing the tenants
	 */
	public boolean getListTenants() { return m_listTenants; }
	public void setListTenants(boolean listTenants) { m_listTenants = listTenants; }
	private boolean m_listTenants = false;

	/**
	 * @return If any exceptions occur during the fetch
	 */
	public Exception getException() { return m_exception; }
	public void setException(Exception exception) { m_exception = exception; }
	private Exception m_exception;

	/**
	 * @return ZooKeeper connection info, host:port
	 */
	public String getZoo() { return m_zoo; }
	public void setZoo(String zoo) { m_zoo = zoo; }
	private String m_zoo;

	/**
	 * @return The specific tenant to dump
	 */
	public String getTenant() { return m_tenant; }
	public void setTenant(String tenant) { m_tenant = tenant; }
	private String m_tenant;

	/**
	 * @return The output directory
	 */
	public String getOutPath() { return m_outPath; }
	public void setOutPath(String outPath) { m_outPath = outPath; }
	private String m_outPath = ".";

	/**
	 * @return Whether any existing data should be replaced, defaults to false (existing data on disk will be kept)
	 */
	public boolean isForced() { return m_forced; }
	public void setForced(boolean forced) { m_forced = forced; }
	private boolean m_forced = false;

	/**
	 * Command line options for this tool
	 */
	public static class DumpOptions extends Options {

		/**
		 * serial version UID
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("static-access")
		public DumpOptions() {

			Option listTenants = OptionBuilder
					.withDescription(LIST_DESC)
					.create(LIST);

			//build the options
			Option broker = OptionBuilder
					.withDescription(BROKER_DESC)
					.withArgName(BROKER_ARG)
					.create(BROKER);

			Option host = OptionBuilder
						.withDescription(OUT_DESC)
						.withArgName(OUT_ARG)
						.create(OUT);

			Option port = OptionBuilder
						.withDescription(TENANT_DESC)
						.withArgName(TENANT_ARG)
						.create(TENANT);

			Option zoo = OptionBuilder
						.withDescription(ZOO_DESC)
						.withArgName(ZOO_ARG)
						.create(ZOO);

			Option force = OptionBuilder
						.withDescription(FORCE_DESC)
						.create(FORCE);

			 //add the options
			addOption(broker);
			addOption(host);
			addOption(port);
			addOption(listTenants);
			addOption(zoo);
			addOption(force);

		}

		public CommandLine parse(String[] args) throws Exception {
			return new BasicParser().parse(this, args);
		}

	}

}
