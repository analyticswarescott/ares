package com.aw.platform.monitoring;

import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.Platform;
import com.aw.platform.monitoring.kafka.KafkaStreamStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Platform status for a tenant
 *
 *
 *
 */
public class SystemTenantStatus extends DefaultTenantStatus {
 public static final Logger logger = LoggerFactory.getLogger(SystemTenantStatus.class);

	@Override
	public void collect(ZkAccessor zk, Platform platform, Tenant tenant, DocumentHandler docs) throws IOException {

		if (!tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			throw new RuntimeException(" invalid use of SystemTenantStatus for tenant " + tenant.getTenantID());
		}

		//remember which tenant we represent
		this.tenant = tenant;

		this.streamStatus = new HashSet<StreamStatus>();
		this.globalstreamStatus = new HashSet<StreamStatus>();

		try {


			//for each stream def, get the status
			for (Document streamDoc : docs.getDocumentsOfType(DocumentType.STREAM_TENANT)) {
				StreamStatus status = new KafkaStreamStatus();
				//create the stream def
				StreamDef def = streamDoc.getBodyAsObject();

				//collect status for the stream def - skip non-system topics for the system tenant
				if (!def.isSystem()) {
					logger.debug("skipping non-system stream def for system tenant: " + def.getProcessorId());
				} else {
					status.collect(zk, platform, tenant, def);
					//add the status
					streamStatus.add(status);
				}
			}

			//add global streams for system tenant
			for (Document streamDoc : docs.getDocumentsOfType(DocumentType.STREAM_GLOBAL)) {
				StreamStatus status = new KafkaStreamStatus();
				//create the stream def
				StreamDef def = streamDoc.getBodyAsObject();
				status.collect(zk, platform, tenant, def);
				//add the status
				globalstreamStatus.add(status);
			}


		} catch (Exception e) {
			throw new IOException("error collecting tenant status for " + tenant.getTenantID(), e);
		}
	}

	public Collection<StreamStatus> getGlobalStreamStatus() { return this.globalstreamStatus;  }
	public void setGlobalstreamStatus(Collection<StreamStatus> globalstreamStatus) {
		this.globalstreamStatus = globalstreamStatus;
	}
	protected Collection<StreamStatus> globalstreamStatus = new ArrayList<>();

	private String type="System";

}

