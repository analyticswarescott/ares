package com.aw.utils.tests;

import kafka.admin.AdminUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaZookeeperServiceWrapper {

    public static final Logger logger = LoggerFactory.getLogger(KafkaZookeeperServiceWrapper.class);
    private int kafkaPort;
    private int zooPort;
    private EmbeddedKafkaCluster embeddedKafkaCluster;
    private EmbeddedZookeeper embeddedZookeeper;
    private static KafkaZookeeperServiceWrapper wrapper;
    private boolean running;
    private String topic;

    private KafkaZookeeperServiceWrapper(int kafkaPort, int zooPort) {
        this.kafkaPort = kafkaPort;
        this.zooPort = zooPort;
    }

    public void stop() {
        embeddedKafkaCluster.shutdown();
        embeddedZookeeper.shutdown();
        running = false;
    }

    public void start() {
        List<Integer> kafkaPorts = new ArrayList<Integer>();
        kafkaPorts.add(this.kafkaPort);

        try {
            embeddedZookeeper = new EmbeddedZookeeper(this.zooPort);
            embeddedZookeeper.startup();

            Properties p = new Properties();
            p.setProperty("delete.topic.enable", "true");
            embeddedKafkaCluster = new EmbeddedKafkaCluster(embeddedZookeeper.getConnection(), p, kafkaPorts);
            embeddedKafkaCluster.startup();

            // pause to make sure kafka is up
            Thread.sleep(2000);

            logger.debug("### Embedded Kafka cluster broker list: " + embeddedKafkaCluster.getBrokerList());
            logger.debug("### Embedded Zookeeper connection: " + embeddedZookeeper.getConnection());

            running = true;
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public void createTopic(String topic) {
        if ( !topicExists(topic)) {
            ZkClient zk = new ZkClient(getZooKeeperURL(), 12000, 12000, ZKStringSerializer$.MODULE$);
            AdminUtils.createTopic(zk, topic, 1, 1, new Properties());
            zk.close();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void feedTopic(String topic, String key, String message) throws Exception {
        if ( !topicExists(topic)) {
            throw new Exception("Topic " + topic + " doesn't exist.");
        }
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:" + kafkaPort );
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig kafkaProducerConfig = new ProducerConfig(props);
        Producer<String, String> kafkaClient = new Producer<>(kafkaProducerConfig);
        KeyedMessage<String, String> data = new KeyedMessage<>(topic, key, message);
        kafkaClient.send(data);
        kafkaClient.close();
    }

    public void feedTopic(String message) throws Exception {
        if ( topic == null ) {
            throw new Exception("Please specify a topic (either at creation time of the kafka service wrapper) " +
                    "or explicitly call one of the methods that specify topic.");
        }
        feedTopic(topic, "", message);
    }

    public void feedTopic(String topic, JSONObject message)  throws Exception {
        feedTopic(topic, "", message.toString());
    }

    public void feedTopic(String topic, String key, JSONObject message) throws Exception {
        feedTopic(topic, key, message.toString());
    }

    public boolean topicExists(String topic) {
        ZkClient zk = new ZkClient(getZooKeeperURL(), 12000, 12000, ZKStringSerializer$.MODULE$);
        boolean b = AdminUtils.topicExists(zk, topic);
        zk.close();
        return b;
    }

    public static KafkaZookeeperServiceWrapper create() {
        return create(null);
    }

    public static KafkaZookeeperServiceWrapper create(String topic) {
        if (wrapper == null) {
            wrapper = new KafkaZookeeperServiceWrapper(9092, 2181);
            wrapper.setTopic(topic);
        }
        return wrapper;
    }

    // todo write tests for this
    public static void main(String[] s) throws Exception {
        KafkaZookeeperServiceWrapper kafkaZookeeperServiceWrapper = KafkaZookeeperServiceWrapper.create();
        kafkaZookeeperServiceWrapper.start();

        kafkaZookeeperServiceWrapper.createTopic("foo_foo");
        kafkaZookeeperServiceWrapper.createTopic("foo_foo");

        kafkaZookeeperServiceWrapper.feedTopic("foo_foo", "", new JSONObject());

//        kafkaZookeeperServiceWrapper.stop();
    }

    public String getZooKeeperURL() {
        return "localhost:" + zooPort;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}