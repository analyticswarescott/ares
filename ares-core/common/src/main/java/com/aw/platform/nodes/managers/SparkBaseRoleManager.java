package com.aw.platform.nodes.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUtils;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleConfig;
import com.aw.platform.roles.SparkMaster;
import com.aw.platform.roles.SparkWorker;

/**
 * Base class to centralize config and ENV settings for HDFS Role managers
 */
public abstract class SparkBaseRoleManager extends AbstractRoleManager {

    public static final String SPARK_ENV = "spark-env.sh";

	public static final String SPARK_ENV_TEMPLATE = "spark_env";

	public SparkBaseRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

    @Override
    public void configure() throws Exception {
		configureSparkEnv();
		configureForRole(); //this will set any opts that are only for the subclass role
    }

	protected abstract void configureForRole() throws Exception;

    private void configureSparkEnv() throws Exception{

		String env = m_roleConfig.getConfigTemplateContent(SPARK_ENV_TEMPLATE);
		Map<String, String> configs = new HashMap<String, String>();
		Map<String, Object> daemonOpts = new HashMap<String, Object>();


		Map<String, Object> envAll =  new HashMap<String, Object>();
		/*if (m_node.getRoles().contains(NodeRole.SPARK_MASTER)) {
			envAll.putAll(m_node.getSettingMap(NodeRole.SPARK_MASTER, SparkMaster.SPARK_););
		}*/
		if (m_node.getRoles().contains(NodeRole.SPARK_WORKER)) {
			Map<String, Object> worker_env = m_node.getSettingMap(SparkWorker.SPARK_ENV);

			for (String k : worker_env.keySet()) {
				envAll.put(k.toUpperCase(), worker_env.get(k));
			}
		}

		//now get and if necessary replace required daemon opts

		if (m_node.getRoles().contains(NodeRole.SPARK_MASTER)) {
			daemonOpts.putAll(m_node.getSettingMap(SparkMaster.SPARK_DAEMON_JAVA_OPTS));

		}
		if (m_node.getRoles().contains(NodeRole.SPARK_WORKER)) {
			daemonOpts.putAll(m_node.getSettingMap(SparkWorker.SPARK_DAEMON_JAVA_OPTS));
		}

		if (PlatformUtils.getSparkMasterNodes(platformMgr.getPlatform()).size() > 1) {
			setHAOpts(daemonOpts);
		}
		//add a single DAEMON_OPTS env setting with all required opts
		envAll.put("SPARK_DAEMON_JAVA_OPTS",  convertDaemonOptsToString(daemonOpts));
		envAll.put("SPARK_JAVA_OPTS", "-XX:+UseConcMarkSweepGC");



		for (String key : envAll.keySet()) {
			configs.put(key, "export " + key.toUpperCase() + "=" + envAll.get(key).toString());
		}

		String newConf = m_roleConfig.applyConfig(env, configs, RoleConfig.HASHTAG);
		m_roleConfig.saveConfig(EnvironmentSettings.getAresSparkHome()
			+ File.separatorChar + "conf" + File.separatorChar + SPARK_ENV, newConf);

    }


	private String convertDaemonOptsToString(Map<String, Object> daemonOpts) {
		String ret = "\"" ;
		for (String opt : daemonOpts.keySet()) {
			ret = ret + "-D" + opt + "=" + daemonOpts.get(opt).toString() + " ";
		}
		ret = ret + "\"";
		return ret;
	}

	private void setHAOpts(Map<String, Object> daemonOpts) throws Exception{
		//for now zookeeper connection is the only variable opt -- the other 2 are constant/default
		//spark.deploy.recoveryMode	Set to ZOOKEEPER to enable standby Master recovery mode (default: NONE).
		//spark.deploy.zookeeper.url	The ZooKeeper cluster url (e.g., 192.168.1.100:2181,192.168.1.101:2181).
		//spark.deploy.zookeeper.dir	The directory in ZooKeeper to store recovery state (default: /spark).
		daemonOpts.put("spark.deploy.zookeeper.url", PlatformUtils.getZKQuorum(platformMgr.getPlatform()));
	}

    @Override
    public NodeRole getRole() {
        return null;
    }

    @Override
    public String getHome() {
        return EnvironmentSettings.getAppLayerHome() + File.separatorChar + "roles" + File.separatorChar + "spark";
    }


    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {
        NodeRoleStatus ret = super.getStatus();
        return ret;
    }


}
