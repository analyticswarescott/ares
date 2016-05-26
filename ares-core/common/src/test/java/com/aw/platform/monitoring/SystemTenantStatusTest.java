package com.aw.platform.monitoring;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.structure.Hive;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SystemTenantStatusTest extends AbstractKafkaZkUnitTest {

	@Test
	public void test() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		DocumentHandler docs = new TestDocumentHandler();

		//create bogus tenant
		Tenant tenant = new Tenant("0");

		int streamCount = 0;
		for (Document doc : docs.getDocumentsOfType(DocumentType.STREAM_GLOBAL)) {

			StreamDef stream = doc.getBodyAsObject();

			//only populate Tenant
			if (stream.getSourceTopic().get(0) == Topic.TENANT) {

				//initialize kafka/zk for each stream with some offsets specific to each stream
				setupKafkaFor(tenant, stream, 25L);
				break;

			}

		}

		SystemTenantStatus status = new SystemTenantStatus();

		ZkAccessor zk = new DefaultZkAccessor(getPlatform(), Hive.TENANT);
		status.collect(zk, getPlatform(), tenant, docs);

		//test converting to/from json
		String strJson = JSONUtils.objectToString(status);
		status = JSONUtils.objectFromString(strJson, SystemTenantStatus.class);

		assertEquals(4, status.getStreamStatus().size());
		assertEquals(1, status.getGlobalStreamStatus().size());

		//only Tenant topic should have offset, and it should be 25
		long offset = 0L;
		for (StreamStatus streamStatus : status.getGlobalStreamStatus()) {

			if (streamStatus.getTopicStatus().containsKey(Topic.TENANT)  ) {
				List<TopicPartitionStatus> curStatus = streamStatus.getTopicStatus().get(Topic.TENANT);
				if (curStatus.size() > 0) {

					offset = curStatus.get(0).getLatestProcessed().getPosition();
				}


				break;
			}

		}
		assertEquals(25L, offset);

	}

}
