package com.aw.utils.kafka;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.platform.Platform;

/**
 * Checkpoints locations in a kafka topic to zookeeper
 *
 *
 *
 */
public class TopicCheckpointer {

    private final AtomicReference<Collection<Offset>> offsetRanges = new AtomicReference<>(new ArrayList<>());

    public TopicCheckpointer(Tenant tenant, String checkpointId) {
    	this.tenant = tenant;
    	this.checkpointId = checkpointId;
	}

    /**
     * @param offsets set the offsets to store
     */
    protected void setOffsets(Collection<Offset> offsets) {
    	offsetRanges.get().clear();
    	offsetRanges.get().addAll(offsets);
    }

	public Collection<Offset> getOffsetRanges() {
		return offsetRanges.get();
	}

    public void addOffset(Offset offset) {
    	offsetRanges.get().add(offset);
    }

    public void reset() {
    	offsetRanges.set(null);
    }

    protected void writeOffsetsOnSuccess( ZkAccessor zk, Platform platform) throws Exception {

    	Impersonation.impersonateTenant(tenant);

    	//for now print them to see them
        for (Offset o : offsetRanges.get()) {

            //write the fact that we completed a set of work on data from the topic
            KafkaOffsetUtils.writeSuccess(zk, platform, tenant, getCheckpointId(), o.getSourceTopic(), o.getPartitionId(), o.getNextOffset(), o.getStartTime(), Instant.now());

        }

    }

	public Tenant getTenant() { return tenant; }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

    public String getCheckpointId() { return checkpointId; }
	public void setCheckpointId(String checkpointId) { this.checkpointId = checkpointId; }
	private String checkpointId;

}
