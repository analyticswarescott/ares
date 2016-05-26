package com.aw.platform.roles;

import com.aw.common.util.JSONUtils.ToLowerSerializer;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode.RoleSetting;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Elasticsearch platform settings
 *
 *
 *
 */
@JsonSerialize(using=ToLowerSerializer.class, as=String.class)
public enum Elasticsearch implements RoleSetting {

	/**
	 * an open server port for communication
	 */
	PORT,

	/**
	 * Replication factor for an index
	 */
	REPLICAS,

	/**
	 * Shard count
	 */
	SHARDS,

	/**
	 * The elasticsearch transport client port - the PORT value for elasticsearch is the rest port
	 */
	ES_TRANSPORT_PORT,


	/**
	 * The cluster name that this node should be a part of
	 */
	CLUSTER_NAME,

	/**
	 * The amount of heap to allocate
	 */
	ES_HEAP_SIZE;

	public String toString() { return name().toLowerCase(); }

	@Override
	public NodeRole getRole() { return NodeRole.ELASTICSEARCH; }

}
