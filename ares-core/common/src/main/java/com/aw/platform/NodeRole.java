package com.aw.platform;

import com.aw.platform.PlatformNode.RoleSetting;
import com.aw.platform.nodes.managers.*;
import com.aw.platform.roles.*;
import com.aw.platform.roles.HdfsData;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Available platform node roles
 */
public enum NodeRole implements SettingProvider {

  /**
   *
   */
	CONFIG_DB_MASTER (
	  (strSetting) -> ConfigDbMaster.valueOf(strSetting.toUpperCase()),
	  ConfigDBMasterRoleManager.class
  	),

	CONFIG_DB_WORKER (
		(strSetting) -> ConfigDbWorker.valueOf(strSetting.toUpperCase()),
		ConfigDBWorkerRoleManager.class
	),

	/**
	 * Our database
	 */
	ELASTICSEARCH(
		(strSetting) -> Elasticsearch.valueOf(strSetting.toUpperCase()),
		ElasticSearchRoleManager.class
	),

	/**
	 * Messaging layer
	 */
	KAFKA(
		(strSetting) -> Kafka.valueOf(strSetting.toUpperCase()),
		KafkaRoleManager.class
	),

	/**
	 * Cluster coordination
	 */
	ZOOKEEPER(
		(strSetting) -> Zookeeper.valueOf(strSetting.toUpperCase()),
		ZookeeperRoleManager.class
	),

	/**
	 * Master brain
	 */
	SPARK_MASTER(
		(strSetting) -> SparkMaster.valueOf(strSetting.toUpperCase()),
		SparkMasterRoleManager.class
	),

	/**
	 * Worker brain
	 */
	SPARK_WORKER(
		(strSetting) -> SparkWorker.valueOf(strSetting.toUpperCase()),
		SparkWorkerRoleManager.class
	),

	/**
	 * REST service
	 */
	REST(
		(strSetting) -> Rest.valueOf(strSetting.toUpperCase()),
		RestRoleManager.class
	),

	/**
	 * Storage
	 */
	HDFS_NAME(
		(strSetting) -> HdfsName.valueOf(strSetting.toUpperCase()),
		HdfsNameRoleManager.class
	),

	/**
	 * Storage
	 */
	HDFS_DATA(
		(strSetting) -> HdfsData.valueOf(strSetting.toUpperCase()),
		HdfsDataRoleManager.class
	),

	/**
	 * Storage
	 */
	HDFS_JOURNAL(
		(strSetting) -> HdfsJournal.valueOf(strSetting.toUpperCase()),
		HdfsJournalRoleManager.class
	),

	/**
	 * DG management console
	 */
	DGMC(
		(strSetting) -> com.aw.platform.roles.DGMC.valueOf(strSetting.toUpperCase()),
		null
	),

	NODE(
		(strSetting) -> com.aw.platform.roles.Node.valueOf(strSetting.toUpperCase()),
			DefaultRoleManager.class
	);

	NodeRole(SettingProvider provider, Class<? extends RoleManager> roleManager) {
		m_provider = provider;
		this.roleManager = roleManager;
	}

	@Override
	public RoleSetting settingValueOf(String str) {
		return m_provider.settingValueOf(str);
	}

	@JsonCreator
	public static NodeRole fromString(String str) {
		return NodeRole.valueOf(str.toUpperCase());
	}

	//our provider of settings
	private SettingProvider m_provider;

	//
	public Class<? extends RoleManager> getRoleManagerType() { return roleManager; }
	private Class<? extends RoleManager> roleManager;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public static NodeRole fromManager(Class<? extends RoleManager> type) {
		for (NodeRole role : values()) {
			if (role.getRoleManagerType() == type) {
				return role;
			}
		}
		return null;
	}
}

