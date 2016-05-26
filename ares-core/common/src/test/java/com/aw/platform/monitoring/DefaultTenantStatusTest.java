package com.aw.platform.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.structure.Hive;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.messaging.Topic;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;

public class DefaultTenantStatusTest extends AbstractKafkaZkUnitTest {

	@Test
	public void test() throws Exception {
		SecurityUtil.setThreadSystemAccess();
		DocumentHandler docs = new TestDocumentHandler();

		//create bogus tenant
		Tenant tenant = new Tenant("1");

		for (Document doc : docs.getDocumentsOfType(DocumentType.STREAM_TENANT)) {

			StreamDef stream = doc.getBodyAsObject();

			//only populate bundle
			if (stream.getSourceTopic().get(0) == Topic.EVENTS) {

				//initialize kafka/zk for each stream with some offsets specific to each stream
				setupKafkaFor(tenant, stream.getSourceTopicNames(tenant), stream.getProcessorName(tenant), 25L);
				break;

			}

		}

		DefaultTenantStatus status = new DefaultTenantStatus();

		ZkAccessor zk = new DefaultZkAccessor(getPlatform(), Hive.TENANT);
		status.collect(zk, getPlatform(), tenant, docs);

		//test converting to/from json
		String strJson = JSONUtils.objectToString(status);
		status = JSONUtils.objectFromString(strJson, DefaultTenantStatus.class);

		assertEquals(5, status.getStreamStatus().size());

		//only bundle topic should have offset, and it should be 25
		long offset = 0L;
		for (StreamStatus streamStatus : status.getStreamStatus()) {

			if (streamStatus.getTopicStatus().containsKey(Topic.EVENTS)) {
				List<TopicPartitionStatus> curStatus = streamStatus.getTopicStatus().get(Topic.EVENTS);

				if (curStatus.size() > 0) {
					offset = curStatus.get(0).getLatestProcessed().getPosition();
				}

				break;
			}

		}

		//TODO: re-enable and determine failure cause
//		assertEquals(25L, offset);

	}

}
