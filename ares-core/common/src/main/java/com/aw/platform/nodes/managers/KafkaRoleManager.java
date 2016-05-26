package com.aw.platform.nodes.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.NodeUtil;
import com.aw.platform.nodes.RoleConfig;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.Kafka;
import com.aw.platform.roles.Zookeeper;

/**
 * Default role manager in case none is avaiable
 */
public class KafkaRoleManager extends AbstractRoleManager {

    public static final String SERVER_PROPERTIES = "server.properties";
    public static final String LOG4J_PROPERTIES = "log4j.properties";

	public static final String SERVER_PROPERTIES_TEMPLATE = "kafka_server";
	public static final String LOG4J_PROPERTIES_TEMPLATE = "kafka_log4j";

    static final String HOME = EnvironmentSettings.getDgHome() + File.separatorChar + "roles" + File.separatorChar + "kafka";

    @Inject @com.google.inject.Inject
    public KafkaRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
    	super(platformMgr, docs);
	}

	@Override
	public Map<String, String> getEnv() {
		//set log dir env variable for kafka commands
		Map ret = new HashMap<String, String>();
		ret.put("LOG_DIR", EnvironmentSettings.getDgHome() + "/log/kafka");
		return ret;
	}

    @Override
    public String getHome() {
    	return HOME;
    }

    @Override
    public NodeRole getRole() {
    	return NodeRole.KAFKA;
    }

    @Override
    public void configure() throws Exception {
        configureServerProperties();
        configureLog4j();
    }

    private void configureLog4j() throws  Exception{
        String log4J = m_roleConfig.getConfigTemplateContent(LOG4J_PROPERTIES_TEMPLATE);
        //TODO: currently no changes, so copy template, should add overrides somewhere, but possibly
        // don't belong in platform def as spark and zookeeper are now
        String newConf = log4J;

        m_roleConfig.saveConfig(getHome()
                + File.separatorChar + "config" + File.separatorChar + LOG4J_PROPERTIES
                , newConf);
    }

    private void configureServerProperties() throws Exception{
        String kafkaSP = m_roleConfig.getConfigTemplateContent(SERVER_PROPERTIES_TEMPLATE);
        Map<String, String> configs = new HashMap<String, String>();
        HashMap<String, Object> settings = new HashMap<String, Object>();

        //add individual settings
        settings.put("port", m_node.getSetting(Kafka.PORT));
		settings.put("broker.id", m_node.getSetting(Kafka.BROKER_ID));

        //add any settings in the config map
        Map<String, Object> configMap = m_node.getSettingMap(Kafka.CONFIGURATION);
        for (String conf : configMap.keySet()) {
            settings.put(conf, configMap.get(conf));
        }


        //add platform zookeeper nodes
        //TODO: this means that any re-start should be accompanied by a re-configure -- probably always
        List<PlatformNode> zooks =  platformMgr.getPlatform().getNodes(NodeRole.ZOOKEEPER);
        int i = 0;
        String zkConnect = "";
        for (PlatformNode zkNode : zooks) {
            if (i > 0) {zkConnect = zkConnect + ",";}
            i++;
            zkConnect = zkConnect + zkNode.getHost() + ":" + zkNode.getSetting(Zookeeper.PORT);
        }
        settings.put("zookeeper.connect=", zkConnect);

        //apply config to
        for (String key : settings.keySet()) {
            if (key.endsWith("=")) { //this is for handling contained key names i.e. ab=x and abc=y
                configs.put(key, key + settings.get(key).toString());
            }
            else {
                configs.put(key, key + "=" + settings.get(key).toString());
            }
        }
        String newConf = m_roleConfig.applyConfig(kafkaSP, configs, RoleConfig.HASHTAG);
        m_roleConfig.saveConfig(getHome()
                + File.separatorChar + "config" + File.separatorChar + SERVER_PROPERTIES
                , newConf);

    }

    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./kafka-server-start.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        args.add("-daemon");
        args.add(getHome() + "/config/server.properties");

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
        ret.add(0, command);
        return ret;
    }

    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./kafka-server-stop.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();


        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
        ret.add(0, command);
        return ret;
    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {
        NodeRoleStatus ret = super.getStatus();

        int procs = 0;
        try {
            procs = NodeUtil.countJavaProcs("kafka.Kafka");
        } catch (Exception e) {
            return ret;
        }


        if (procs == 0) {
            ret.setState(State.STOPPED);
        }
        if (procs == 1) {
            ret.setState(State.RUNNING);
        }
        if (procs >1) {
            ret.setState(State.ERROR);
            ret.setStatusMessage(" multiple zookeeper processes detected ");
        }


        return ret;

    }




}
