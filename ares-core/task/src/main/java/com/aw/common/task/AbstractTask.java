package com.aw.common.task;

import java.time.Instant;
import java.util.UUID;

import com.aw.common.tenant.Tenant;

/**
 * Default abstract base class for tasks
 *
 *
 *
 */
public abstract class AbstractTask implements Task {

	public UUID getGuid() { return this.guid;  }
	public void setGuid(UUID guid) { this.guid = guid; }
	private UUID guid;

	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	public Instant getStartTime() { return this.startTime; }
	public void setStartTime(Instant startTime) { this.startTime = startTime; }
	private Instant startTime;



}
