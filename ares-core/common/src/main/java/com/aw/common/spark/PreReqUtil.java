package com.aw.common.spark;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.aw.common.util.JSONUtils;
import com.aw.document.DocumentType;
import com.aw.platform.PlatformUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.restcluster.PlatformController;
import com.aw.platform.roles.Kafka;
import com.aw.utils.kafka.KafkaTopics;

import com.google.common.collect.Sets;

import kafka.admin.AdminUtils;
import kafka.common.TopicExistsException;

/**
 * Methods to ensure pre-requisites are in place for drivers and procesors
 */
public class PreReqUtil {

    public static final Logger logger = LoggerFactory.getLogger(PreReqUtil.class);


    public static boolean provisionTopicsTolerateErrors(Platform platform, PlatformController controller, List<Document> streamDefs) {
        try {

            provisionTopics(platform, controller, streamDefs);
            return true;
        } catch (Exception ex) {
            logger.error(" topic provisioning error " + ex.getMessage());
            return false;
        }
    }

    public static void unProvisionTopics(Platform platform, List<Document> streamDefs) throws Exception {
        Set<String> sourceTopics = Sets.newHashSet();

        for (Document doc : streamDefs) {
            logger.debug("Processing " + doc.getDisplayName());

            try {

                StreamDef streamDef = doc.getBodyAsObject();
                sourceTopics.addAll(streamDef.getSourceTopicNames(Tenant.forId(doc.getTenantID())));

            } catch (Exception e) {

                logger.warn("ignoring invalid source def form for " + doc.getDocumentType() + " / " + doc.getName());

            }

        }

        for (String topic : sourceTopics) {
            KafkaTopics.deleteTopic(platform, topic);
        }
    }

    public static void provisionTopics(Platform platform, TenantAware tenantAware, List<Document> streamDefs) throws Exception {
        // provision topics


        Set<String> sourceTopics = Sets.newHashSet();

        for (Document doc : streamDefs) {

            StreamDef streamDef = null;

            try {
                streamDef = doc.getBodyAsObject();
				if (doc.getDocumentType() == DocumentType.STREAM_GLOBAL) {
					//System.out.println(" ============================ >>>> GLOBAL STREAM detected: " + doc.getName());
					streamDef.setisGlobal(true);
				}
            } catch (Exception e) {
                //log bad stream defs and continue
                logger.error("error loading stream def for " + tenantAware.getTenantID() + "/" + doc.getDocumentType() + "/" + doc.getName() + "/v" + doc.getVersion() + " : " + e.getMessage());
                continue; //skip handling this doc in this case
            }

			if (tenantAware.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
				if (streamDef.isGlobal()) {
					//System.out.println(" ============================ >>>> GLOBAL STREAM detected: " + JSONUtils.objectToString(streamDef));
					int bp = 0;
				}
				if (streamDef.isSystem()) {
					//System.out.println(" ============================ >>>> SYSTEM TENANT STREAM topic create: " + sourceTopics);
					sourceTopics.addAll(streamDef.getSourceTopicNames(Tenant.forId(tenantAware.getTenantID())));
				}
				else {
					logger.warn("skipping topic creation for tenant stream not marked as system " + doc.getName());
				}
			}
			else {
				if (streamDef.isTenant()) {
					sourceTopics.addAll(streamDef.getSourceTopicNames(Tenant.forId(tenantAware.getTenantID())));
				}
				else {
					logger.warn("skipping topic creation for tenant stream not marked as tenant " + doc.getName());
				}
			}

        }

        for (String topic : sourceTopics) {
			logger.info(" ============================ >>>> CREATING TOPIC: " + topic);
            createTopic(platform, topic);
        }


    }

    //TODO: add partitioning support
    public static void createTopic(Platform platform, String topic) throws Exception {
        //provision the topic set

        int replication = getReplicationFactor(platform);
        int partitions = getPartitioning(platform);

        ZkClient zk = null;
        try {
            SecurityUtil.setThreadSystemAccess();

            zk = new ZkClient(PlatformUtils.getZKQuorum(platform), 12000, 12000, ZKStringSerializer$.MODULE$);


			logger.warn("Provisioning topic " + topic + ": ");
            if (AdminUtils.topicExists(zk, topic)) {
                logger.warn("Topic " + topic + " already exists.");
            } else {
                try {
                    AdminUtils.createTopic(zk, topic, partitions, replication, new Properties());
                } catch (kafka.admin.AdminOperationException ex) {
                    //try multiple times? //TODO: examine
                    Thread.sleep(1000);
                    AdminUtils.createTopic(zk, topic, partitions, replication, new Properties());
                }
                logger.info("Topic " + topic + " created.");
            }
        } catch (TopicExistsException e) {
            logger.error("Topic " + topic + " already exists.", e);
        } finally {
            if (zk != null) {
                zk.close();
            }
        }
    }


    private static int getReplicationFactor(Platform platform) {

		//default replication target is 3 unless set in Kafka role global settings
		int desiredReplication = 3;
		int checkSetting = platform.getNode(NodeRole.KAFKA)
			.getSettingInt(Kafka.DEFAULT_REPLICATION);
		if (checkSetting != -1) {
			desiredReplication = checkSetting;
		}
		//TODO: do we need to allow override of replication settings per tenant, or just partitioning?

        //check number of configured data nodes, if < desired, set replication = # nodes
        // TODO: confirm that we can set to available rather than confirmed nodes since Kafka will round-robin the replicas if a node is down
        int availableNodes = platform.getNodes(NodeRole.KAFKA).size();
        if (availableNodes < desiredReplication) {
            logger.warn(" Kafka replication set to available node count of " + availableNodes
                    + " rather than target of " + desiredReplication);
            return availableNodes;
        }
        else {
            return desiredReplication;
        }

    }

    private static int getPartitioning(Platform platform) {

		//TODO: this is likely based on tenant sizing
		List<PlatformNode> kafkaNodes = platform.getNodes(NodeRole.KAFKA);
        return kafkaNodes.size();

    }
}


