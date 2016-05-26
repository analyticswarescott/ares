package com.aw.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.platform.Platform;
import com.aw.platform.PlatformBuilder;
import com.aw.platform.roles.Zookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.Kafka;
import com.aw.utils.kafka.KafkaTopics;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;

public class KafkaTopicsTest extends BaseIntegrationTest {

    @Override
    protected boolean usesSpark() {
        return false;
    }

    @Override
    protected boolean startsSpark() {
        return false;
    }

    @Override
    protected boolean usesElasticsearch() {return false;}

    @Override
    protected boolean usesKafka() {return true;}

    @Test
    public void test() throws Exception {

    	deleteTopic();

    	topicList();

    	topicExists();

    	getPartitionKeys();

    }

    public void deleteTopic() throws Exception {
        String topic = createTopic();
        Thread.sleep(2000);

        assertTrue(KafkaTopics.topicExists(TestDependencies.getPlatform().get(), topic));

        KafkaTopics.deleteTopic(TestDependencies.getPlatform().get(), topic);

        Thread.sleep(2000);

        assertFalse(KafkaTopics.topicExists(TestDependencies.getPlatform().get(), topic));
    }

    public void topicList() throws Exception {
        List<String> topics = new ArrayList<>();
        topics.add(createTopic());
        topics.add(createTopic());
        topics.add(createTopic());

        Thread.sleep(2000);


		DefaultZkAccessor zk = new DefaultZkAccessor(TestDependencies.getPlatform().get(), Hive.TENANT);
        List<String> kafkaTopicList = KafkaTopics.getTopicList(zk, TestDependencies.getPlatform().get());
        assertTrue(kafkaTopicList.containsAll(topics));
    }

    public void topicExists() throws Exception {
        List<String> topics = new ArrayList<>();
        topics.add( createTopic() );
        topics.add( createTopic() );
        topics.add( createTopic() );

        Thread.sleep(2000);

        topics.stream().forEach( (topic) -> {
            try {
                assertTrue( "Expected topic " + topic + " to exist", KafkaTopics.topicExists(TestDependencies.getPlatform().get(), topic) );
            } catch (Exception e) {
                fail();
            }
        });
    }

    public void getPartitionKeys() throws Exception {
        String topic = createTopic();
        Thread.sleep(2000);
		DefaultZkAccessor zk = new DefaultZkAccessor(TestDependencies.getPlatform().get(), Hive.TENANT);

        String brokerHost = TestDependencies.getPlatform().get().getNode(NodeRole.KAFKA).getHost();
        int brokerPort = TestDependencies.getPlatform().get().getNode(NodeRole.KAFKA).getSettingInt(Kafka.PORT);

        {

            List<Integer> partitionKeys = KafkaTopics.getPartitionKeys(zk, TestDependencies.getPlatform().get(), topic);
            assertNotNull(partitionKeys);
            assertEquals(1, partitionKeys.size());
            assertEquals(Integer.valueOf(0), partitionKeys.get(0));
        }

        {
            List<Integer> partitionKeys = KafkaTopics.getPartitionKeys(zk, TestDependencies.getPlatform().get(), UUID.randomUUID().toString());
            assertNotNull(partitionKeys);
            assertEquals(0, partitionKeys.size());
        }

    }

    private String createTopic() throws Exception {
        String topic = UUID.randomUUID().toString();
        ZkClient zk = new ZkClient(PlatformUtils.getZKQuorum(TestDependencies.getPlatform().get()), 5000, 5000, ZKStringSerializer$.MODULE$);
        AdminUtils.createTopic(zk, topic, 1, 1, new Properties());

        return topic;
    }

}
