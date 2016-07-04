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
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.NodeUtil;
import com.aw.platform.nodes.RoleConfig;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.Zookeeper;

/**
 * Default role manager in case none is available
 */
public class ZookeeperRoleManager extends AbstractRoleManager {

    public static final String ZOO_CFG = "zoo.cfg";
    public static final String LOG_4J = "log4j.properties";


    static final String HOME = EnvironmentSettings.getAppLayerHome() + File.separatorChar + "roles" + File.separatorChar + "zookeeper";

    @Inject @com.google.inject.Inject
    public ZookeeperRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
    	super(platformMgr, docs);
	}

    @Override
    public void configure() throws Exception {
        configureZooCfg();
        configureLog4J();
    }


	@Override
	public Map<String, String> getEnv() {
		//set log dir env variable for kafka commands
		Map ret = new HashMap<String, String>();
		ret.put("ZOO_LOG_DIR", EnvironmentSettings.getAppLayerHome() + "/log/zookeeper");
		return ret;
	}

    @Override
    public String getHome() {
    	return HOME;
    }

    @Override
    public NodeRole getRole() {
    	return NodeRole.ZOOKEEPER;
    }

    private void configureZooCfg() throws Exception{

        String zooCFG = m_roleConfig.getConfigTemplateContent(ZOO_CFG);
        Map<String, String> configs = new HashMap<String, String>();
        HashMap<String, Object> settings = new HashMap<String, Object>();

        //add individual settings
        //settings.put("maxClientCnxns", m_node.getSetting(getRole(), Zookeeper.));

        //add any settings in the config map
        Map<String, Object> configMap = m_node.getSettingMap(Zookeeper.CONFIGURATION);
        for (String conf : configMap.keySet()) {
            settings.put(conf, configMap.get(conf));
        }

        //add platform zookeeper nodes
        //TODO: this means that any re-start should be accompanied by a re-configure -- probably always
        List<PlatformNode> zooks =  platformMgr.getPlatform().getNodes(NodeRole.ZOOKEEPER);
        Map<String, String> configAdds = new HashMap<String, String>();
        if (zooks.size() > 1) {
            for (PlatformNode zkNode : zooks) {
                int serverID = zkNode.getSettingInt(Zookeeper.SERVER_ID);
                String serverConnect = zkNode.getHost() + ":" + zkNode.getSettingInt(Zookeeper.PEER_PORT)
                        + ":" + zkNode.getSettingInt(Zookeeper.LEADER_ELECT_PORT);

                configAdds.put("server." + serverID, serverConnect);
            }

        }

        //apply config to
        for (String key : settings.keySet()) {
            configs.put(key,  key.toUpperCase() + "=" + settings.get(key).toString());
        }
        String newConf = m_roleConfig.applyConfig(zooCFG, configs, RoleConfig.HASHTAG);

        //add newly generated entries (for server.X in replicated mode)
        //if (configAdds.size() > 0) {
            newConf = m_roleConfig.appendZKConfig(newConf, configAdds, RoleConfig.HASHTAG);

            //add myid file with server ID to activate cluster mode
			m_roleConfig.saveConfig(EnvironmentSettings.getAppLayerHome()
                    + File.separatorChar + "data" + File.separatorChar + "zookeeper" + File.separatorChar + "myid"
                    , m_node.getSetting(Zookeeper.SERVER_ID));
        //}

		m_roleConfig.saveConfig(getHome()
			+ File.separatorChar + "conf" + File.separatorChar + ZOO_CFG
			, newConf);

    }

    private void configureLog4J() throws Exception {
        String log4J = m_roleConfig.getConfigTemplateContent( LOG_4J);

        Map<String, String> configs = new HashMap<String, String>();
        Map<String, Object> overrides = m_node.getSettingMap(Zookeeper.LOG4J_OVERRIDES);

        for (String key : overrides.keySet()) {
            configs.put(key + "=",  key + "=" + overrides.get(key).toString());
        }
        String newConf = m_roleConfig.applyConfig(log4J, configs, RoleConfig.HASHTAG);
		m_roleConfig.saveConfig(getHome()
                + File.separatorChar + "conf" + File.separatorChar + LOG_4J, newConf);
    }



    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./zkServer.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        args.add("start");

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
        ret.add(0, command);
        return ret;
    }


    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./zkServer.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        args.add("stop");

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
        ret.add(0, command);
        return ret;
    }

    @Override
    public NodeRoleStatus getStatus() {

        NodeRoleStatus ret = new NodeRoleStatus();

        int procs = 0;
        try {
            procs = NodeUtil.countJavaProcs("zoo.cfg");
        } catch (Exception e) {
            ret.setState(State.UNKNOWN);
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
