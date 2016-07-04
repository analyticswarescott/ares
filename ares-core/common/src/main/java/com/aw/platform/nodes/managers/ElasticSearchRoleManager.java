package com.aw.platform.nodes.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.aw.platform.roles.Elasticsearch;

/**
 * Default role manager in case none is avaiable
 */
public class ElasticSearchRoleManager extends AbstractRoleManager {

    public static final Logger logger = LoggerFactory.getLogger(ElasticSearchRoleManager.class);

    public static final String ES_YML = "elasticsearch.yml";
    public static final String LOGGING_YML = "logging.yml";



	@Inject
	public ElasticSearchRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

    @Override
	public Map<String, String> getEnv() {
		HashMap<String, String> ret = new HashMap<String, String>();

		//TODO: we could calculate this, but would then have to override for testing
		ret.put("ES_HEAP_SIZE", m_node.getSetting(Elasticsearch.ES_HEAP_SIZE, "1g"));

		return ret;
	}

    @Override
    public String getHome() {
    	return EnvironmentSettings.getAppLayerHome() + File.separatorChar + "roles" + File.separatorChar + "elastic";
    }

    @Override
    public void configure() throws Exception {
        configureESYML();
    }

    private void configureESYML()  throws Exception{
        String fyml = m_roleConfig.getConfigTemplateContent(ES_YML);
        Map<String, String> configs = new HashMap<String, String>();
        HashMap<String, Object> yml = new HashMap<String, Object>();

        yml.put("cluster.name", m_node.getSetting(Elasticsearch.CLUSTER_NAME));
        yml.put("node.name", m_node.getHost());
        yml.put("http.port", m_node.getSetting(Elasticsearch.PORT)); //REST port

        yml.put("network.host", m_node.getHost());
        yml.put("network.publish_host", m_node.getHost());

        for (String key : yml.keySet()) {
            configs.put(key,  key + ": \"" + yml.get(key).toString() + "\"");
        }


        //---    //set up unicast to all other nodes if there is  cluster in play

        //need to apply separately, as the value is not quoted
        //TODO: this would support only one cluster per platform
        List<PlatformNode> ess =  platformMgr.getPlatform().getNodes(NodeRole.ELASTICSEARCH);
        Map<String, String> configAdds = new HashMap<String, String>();

        int cnt =0;
        String uc_hosts = "";
        if (ess.size() > 1) {
            for (PlatformNode es : ess) {
                if (es.getHost().equals(m_node.getHost())) {
                    continue;
                }
                else {
                    int port = es.getSettingInt(Elasticsearch.ES_TRANSPORT_PORT);
                    String uch = es.getHost() + ":" + port;
                    if (cnt == 0) {
                        uc_hosts = "[\"" + uch + "\"";
                    } else {
                        uc_hosts = uc_hosts + ",\"" + uch + "\"";
                    }
                    cnt++;
                }
            }
            uc_hosts = uc_hosts + "]";
            configs.put("discovery.zen.ping.unicast.hosts:", "discovery.zen.ping.unicast.hosts: " + uc_hosts);

        }

        //apply the config

        String newConf = m_roleConfig.applyConfig(fyml, configs, RoleConfig.HASHTAG);
        m_roleConfig.saveConfig(getHome()
                + File.separatorChar + "config" + File.separatorChar + ES_YML, newConf);
    }

    @Override
    public NodeRole getRole() {
    	return NodeRole.ELASTICSEARCH;
    }


    @Override
    public List<RoleOSCommand> getStartCommands() {

        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./elasticsearch";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();
        args.add("-d");
        args.add("--pidfile");
        args.add(getPidFilePath());
        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);

        ret.add(0, command);

        return ret;

    }

    @Override
    public void doStop() throws Exception {

        executeCommands(getStopCommands());

    }

    @Override
    public List<RoleOSCommand> getStopCommands() {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

        //get pid

        File pidFile = new File(getHome() + "/bin/espid.pid");

        String pid = "";
        try {

            pid = FileUtils.readFileToString(pidFile);
            logger.info("ES pid detected as " + pid);
        } catch (IOException e) {
           //TODO: need to do nothing and let the kill -9 error out, or unit test does not work
            //TODO: get this working with cat pidfile
            logger.warn(" ES pid not found at " + pidFile);
        }


        String cmd = "kill";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        args.add("-9");
        if (pid.length() > 0) {
            args.add(pid);
        }

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);

        ret.add(0, command);

        return ret;
    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {
        NodeRoleStatus ret = super.getStatus();

        int procs = 0;
        try {
            procs = NodeUtil.countJavaProcs("org.elasticsearch.bootstrap.Elasticsearch");
        } catch (Exception e) {
            throw new NodeOperationException("error getting status for role " + getRole() + " : " + e.getMessage(), e);
        }

        if (procs == 0) {
            ret.setState(State.STOPPED);
        }

        if (procs == 1) {
            ret.setState(State.RUNNING);
        }

        if (procs >1) {
            ret.setState(State.ERROR);
            ret.setStatusMessage(" multiple ES processes detected ");
        }

        return ret;
    }

    private String getPidFilePath() {
        return "./espid.pid";
    }


}
