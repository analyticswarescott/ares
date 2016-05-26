package com.aw.common.tenant;

import com.aw.platform.Setting;

public enum TenantSetting implements Setting {

	/**
	 * time unit for time slicing of elasticsearch indexes
	 */
	ES_TIME_UNIT,

	/**
	 * shards for elasticsearch dlp event index
	 */
	ES_EVENT_SHARDS,

	/**
	 * shard count for elasticsearch edr scan index
	 */
	ES_SCAN_SHARDS,

	/**
	 * Replication in elasticsearch
	 */
	ES_REPLICATION,

	/**
	 * Replication in kafka
	 */
	KAFKA_REPLICATION;

	public String toString() { return name().toLowerCase(); }

}
