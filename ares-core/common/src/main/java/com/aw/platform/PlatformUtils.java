package com.aw.platform;

import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.roles.*;
import com.google.common.base.Preconditions;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Settings Provider
 */
public class PlatformUtils {

	public static final Logger logger = org.slf4j.LoggerFactory.getLogger(PlatformUtils.class);

	private static final String DEFAULT_KAFKA_PRODUCER_PARAMS = "{\n" +
		"      \"request.required.acks\": \"1\",\n" +
		"      \"serializer.class\": \"kafka.serializer.StringEncoder\",\n" +
		"      \"metadata.broker.list\": \"V(kafka_broker_list_host_and_port)\"\n" +
		"    }";

	public static String getSparkSubmitURL(Platform platform) throws Exception {
		verifyPlatform(platform);

		final StringBuilder spList = new StringBuilder();

		int host = 0;
		for (PlatformNode sp : getSparkMasterNodes(platform)) {
			host++;
			if (spList.length() > 0) {
				spList.append(",");
			}

			if (host == 1) {
				spList.append("spark://");
			}

			spList.append(sp.getHost())
				.append(":")
				.append(sp.getSettingInt(SparkMaster.SPARK_REST_PORT));
		}
		logger.info("using spark rest URL from platform " + spList);
		return spList.toString();

	}

	public static String getSparkMaster(Platform platform) {
		verifyPlatform(platform);

		PlatformNode spark = platform.getNode(NodeRole.SPARK_MASTER);
		if (spark != null) {
			return spark.getHost() + ":" + spark.getSetting(SparkMaster.SPARK_MASTER_PORT);
		} else {
			throw new PlatformStateException(" SPARK_MASTER node not found in platform ");
		}

	}

	public static Properties getKafkaProducerParams(Platform platform) {
		verifyPlatform(platform);
		try {
			JSONObject defaultProducerParams = new JSONObject(DEFAULT_KAFKA_PRODUCER_PARAMS);

			Properties kafkaProducerParams = new Properties();
			kafkaProducerParams.put("metadata.broker.list", getKafkaBrokerList(platform));
			kafkaProducerParams.put("serializer.class", defaultProducerParams.getString("serializer.class"));
			kafkaProducerParams.put("partitioner.class", "com.aw.utils.kafka.client.RoundRobinPartitioner");
			kafkaProducerParams.put("request.required.acks", defaultProducerParams.getString("request.required.acks"));//def.get("request.required.acks"), "1"));

			return kafkaProducerParams;
		} catch (JSONException e) {
			throw new RuntimeException("Unable to create kafka producer params", e);
		}

	}


	public static List<PlatformNode> getHadoopNameNodes(Platform platform) {
		verifyPlatform(platform);
		//get NameNode list
		List<PlatformNode> nameNodes = platform.getNodes(NodeRole.HDFS_NAME);
		if (nameNodes.size() < 1 || nameNodes.size() > 2) {
			throw new PlatformStateException(" HDFS name node count incorrect.  Expected 1 or to and found " + nameNodes.size());
		}

		return nameNodes;

	}

	public static String getHadoopNameNodeList(Platform platform) {
		verifyPlatform(platform);

		final StringBuilder nnList = new StringBuilder();
		for (PlatformNode nn : getHadoopNameNodes(platform)) {
			if (nnList.length() > 0) {
				nnList.append(",");
			}
			nnList.append(nn.getHost());
		}

		return nnList.toString();

	}

	public static String getHadoopClusterName(Platform platform) {
		verifyPlatform(platform);

		return platform.getNode(NodeRole.HDFS_NAME).getSetting(HdfsName.HA_CLUSTER_NAME);
	}

	public static String getHadoopJournalNodeList(Platform platform) {
		verifyPlatform(platform);

		List<PlatformNode> journalNodes = platform.getNodes(NodeRole.HDFS_JOURNAL);
		if (journalNodes.size() < 1) {
			throw new PlatformStateException(" HDFS Journal node count incorrect.  Expected at least 1 found " + journalNodes.size());
		}
		final StringBuilder jnList = new StringBuilder();
		for (PlatformNode jn : journalNodes) {
			if (jnList.length() == 0) {
				jnList.append("qjournal://");	// prefix the entire connection string
			} else {
				jnList.append(";");
			}
			jnList
				.append(jn.getHost())
				.append(":")
				.append(jn.getSettingInt(HdfsJournal.PORT));
		}
		jnList.append("/dgCluster");
		return jnList.toString();
	}

	public static String getZKQuorum(Platform platform) {
		verifyPlatform(platform);

		return getZkConnectString(platform);
	}

	/**
	 * Utility method to get the zk connect string given a platform
	 *
	 * @param platform The platform
	 * @return The zk connect string - will be empty if no zk nodes defined
	 */
	public static String getZkConnectString(Platform platform) {
		verifyPlatform(platform);

		List<PlatformNode> zooNodes = platform.getNodes(NodeRole.ZOOKEEPER);
		if (zooNodes.size() < 1) {
			throw new PlatformStateException(" Zookeeper node count incorrect.  Expected at least 1 found " + zooNodes.size());
		}
		final StringBuilder zkList = new StringBuilder();
		for (PlatformNode zk : zooNodes) {
			if (zkList.length() > 0) {
				zkList.append(",");
			}
			zkList
				.append(zk.getHost())
				.append(":")
				.append(zk.getSettingInt(Zookeeper.PORT));
		}

		return zkList.toString();
	}


	public static List<PlatformNode> getSparkMasterNodes(Platform platform) {
		verifyPlatform(platform);

		//get Spark Master Node list
		List<PlatformNode> nodes = platform.getNodes(NodeRole.SPARK_MASTER);
		if (nodes.size() < 1) {
			throw new PlatformStateException(" Spark Master node count incorrect.  Expected 1 or to and found " + nodes.size());
		}

		return nodes;

	}

	public static String getSparkMasterList(Platform platform) {
		verifyPlatform(platform);

		final StringBuilder spList = new StringBuilder();
		for (PlatformNode sp : getSparkMasterNodes(platform)) {
			if (spList.length() > 0) {
				spList.append(",");
			}
			spList
				.append(sp.getHost())
				.append(":")
				.append(sp.getSettingInt(SparkMaster.SPARK_MASTER_PORT));
		}

		return spList.toString();

	}

	public static List<PlatformNode> getKafkaBrokerNodes(Platform platform) {
		verifyPlatform(platform);

		//get Kafka Node list
		List<PlatformNode> nodes = platform.getNodes(NodeRole.KAFKA);
		if (nodes.size() < 1) {
			throw new PlatformStateException(" Kafka broker node count incorrect.  Expected 1 or more and found " + nodes.size());
		}

		return nodes;

	}

	public static String getKafkaBrokerList(Platform platform) {
		verifyPlatform(platform);

		final StringBuilder kbList = new StringBuilder();
		for (PlatformNode k : getKafkaBrokerNodes(platform)) {
			if (kbList.length() > 0) {
				kbList.append(",");
			}
			kbList.append(k.getHost()).append(":").append(k.getSettingInt(Kafka.PORT));
		}

		return kbList.toString();

	}

	private static void verifyPlatform(Platform platform) {
		Preconditions.checkArgument(platform != null, "Platform is required!");
	}

	public static boolean isRest(Platform  platform) {

		PlatformNode node = platform.getNode(EnvironmentSettings.getHost());

    	//this only waits on a REST instance for now
    	return (node != null && node.getRoles().contains(NodeRole.REST));

	}


	//is this a single node, single DB install -- if so can not require certain commands
	public static boolean isSingleNodeSingleDB(Platform platform) {

		if (platform.getNodes(NodeRole.CONFIG_DB_MASTER).size() == 0) {
			throw new PlatformStateException(" CONFIG_DB_MASTER role not present in platform");
		}
		if (platform.getNodes(NodeRole.CONFIG_DB_WORKER).size() == 0) {
			throw new PlatformStateException(" CONFIG_DB_WORKER role not present in platform");
		}

		if (platform.getNodes(NodeRole.CONFIG_DB_WORKER) .size() == 1) {
			PlatformNode worker = platform.getNode(NodeRole.CONFIG_DB_WORKER);
			PlatformNode master = platform.getNode(NodeRole.CONFIG_DB_MASTER);

			if (master.getSettingInt(ConfigDbMaster.MASTER_DB_PORT) ==
				worker.getSettingInt(ConfigDbWorker.WORKER_DB_PORT) && master.getHost().equals(worker.getHost())) {
				return true;
			}
		}

		return false;
	}
}
