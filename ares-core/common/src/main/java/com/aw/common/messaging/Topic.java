package com.aw.common.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Known message topics within DG's platform
 *
 *
 *
 */
public enum Topic {

	/**
	 * Used in provisioning tenants - deprecated
	 */
	TENANT(false, "Tenant"),

	/**
	 * Action topic, all actions in the system go to this topic
	 */
	ACTION(true),

	/**
	 * All created incidents in the system
	 */
	INCIDENT(true),

	/**
	 * events from the agent
	 */
	MACHINE_EVENT(true),

	/**
	 * incoming bundles
	 */
	BUNDLE(true),

	/**
	 * A reference to the bundle in the storage layer
	 */
	BUNDLE_REF(true),

	/**
	 * A reference to the scan data in the storage layer
	 */
	SCAN_REF(true),

	/**
	 * Any topic that begins with RULE will represent firings of a rule in the detection framework
	 */
	RULE(true),

	/**
	 * edr scan component data - static executable files on disk - this maps to a unity data type name
	 */
	ON_DISK_EXECUTABLE(true),

	/**
	 * edr scan component data - windows registry - this maps to a unity data type name
	 */
	WINDOWS_REGISTRY(true),

	/**
	 * edr scan component data - windows log - this maps to a unity data type name
	 */
	WINDOWS_LOG(true),

	/**
	 * edr scan component data - active network connections (i.e. netstat) - this maps to a unity data type name
	 */
	NETWORK_CONNECTION(true),

	/**
	 * alarms for a tenant, will be inserted into the alarm index
	 */
	ALARM(true),

	/**
	 * edr scan component data - processes actively running at the time of the scan - this maps to a unity data type name
	 */
	RUNNING_PROCESS(true),

	/**
	 * Windows WMI data
	 */
	WMI_DATA(true),

	/**
	 * System error topic
	 */
	ERRORS(true),

	/**
	 * status of streams within spark
	 */
	TOPIC_STATUS(true),

	/**
	 * topic for iterable where files ready for processing will be located
	 */
	READY_FOR_ARCHIVE(true),

	/**
	 * topic for archived files compressed
	 */
	ARCHIVED_FILES(true),
	/**
	 * performance stats per node
	 */
	PERF_STAT(true);


	/**
	 * Create a global topic
	 */
	private Topic() {
		this(false);
	}

	/**
	 * Create a topic that may be tenant scoped if tenantScoped=true
	 *
	 * @param tenantScoped Whether the topic is tenant scoped
	 */
	private Topic(boolean tenantScoped) {
		m_tenantScoped = tenantScoped;
	}

	/**
	 * Create a topic and explicitly set the name
	 *
	 * @param tenantScoped Whether the topic is tenant scoped
	 */
	private Topic(boolean tenantScoped, String explicitName) {
		m_tenantScoped = tenantScoped;
		m_explicitName = explicitName;
	}

	/**
	 * @return The kafka topic name
	 */
	public String getTopicName() {
			return name().toLowerCase();
	}

	@Override
	public String toString() {
		if (m_explicitName != null) {
			return m_explicitName;
		}
		else {
			return name().toLowerCase();
		}
	}

	@JsonCreator
	public static Topic fromString(String topic) {
		return Topic.valueOf(topic.toUpperCase());
	}


	/**
	 * @return Whether this topic is tenant scoped - if true, there is a topic per tenant - if false, it is a global topic in the platform
	 */
	public boolean isTenantScoped() { return m_tenantScoped;}

	/**
	 * Build a topic string for this message
	 *
	 * TODO: pass tenants instead of strings
	 *
	 * @param message
	 * @return
	 */
	public static String toTopicString(String tenantID, Topic topic) {

		String ret = null;

		if (topic.isTenantScoped()) {
			ret = tenantID + "_" + topic;
		}

		else {
			ret = topic.toString();
		}

		return ret;

	}
	private boolean m_tenantScoped;
	private String m_explicitName;

}
