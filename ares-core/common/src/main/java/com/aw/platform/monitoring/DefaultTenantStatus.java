package com.aw.platform.monitoring;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

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

/**
 * Platform status for a tenant
 *
 *
 *
 */
public class DefaultTenantStatus implements TenantStatus {
 public static final Logger logger = LoggerFactory.getLogger(DefaultTenantStatus.class);

	@Override
	public void collect(ZkAccessor zk, Platform platform, Tenant tenant, DocumentHandler docs) throws IOException {

		//remember which tenant we represent
		this.tenant = tenant;

		this.streamStatus = new HashSet<StreamStatus>();

		try {

			//for each stream def, get the status
			for (Document streamDoc : docs.getDocumentsOfType(DocumentType.STREAM_TENANT)) {

				StreamStatus status = new KafkaStreamStatus();

				//create the stream def
				StreamDef def = streamDoc.getBodyAsObject();
				if (def.isTenant()) {
					//collect status for the stream def
					status.collect(zk, platform, tenant, def);
					//add the status
					streamStatus.add(status);
				}
			}

		} catch (Exception e) {
			throw new IOException("error collecting tenant status for " + tenant.getTenantID(), e);
		}

	}

	public Tenant getTenant() { return this.tenant;  }
	protected Tenant tenant;

	public Collection<StreamStatus> getStreamStatus() { return this.streamStatus;  }
	protected Collection<StreamStatus> streamStatus;

	private String type="Tenant";


}

