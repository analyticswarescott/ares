package com.aw.platform.nodes.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpResponse;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.RestClient;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.RoleConfig;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.SparkMaster;

/**
 * Controller for SPARK_MASTER role
 */
public class SparkMasterRoleManager extends SparkBaseRoleManager {

    public static final String LOG_4J = "log4j.properties";
	public static final String LOG_4J_TEMPLATE = "spark_log4j";

	@Inject @com.google.inject.Inject
	public SparkMasterRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

    @Override
    public NodeRole getRole() {
    	return NodeRole.SPARK_MASTER;
    }

    @Override
    public void configureForRole() throws Exception {
        configureLog4J();
    }


	private void configureLog4J() throws Exception {
        String log4J = m_roleConfig.getConfigTemplateContent(LOG_4J_TEMPLATE);
        Map<String, String> configs = new HashMap<String, String>();
        Map<String, Object> overrides = m_node.getSettingMap(SparkMaster.LOG4J_OVERRIDES);

        for (String key : overrides.keySet()) {
            configs.put(key + "=",  key + "=" + overrides.get(key).toString());
        }
        String newConf = m_roleConfig.applyConfig(log4J, configs, RoleConfig.HASHTAG);
		m_roleConfig.saveConfig(EnvironmentSettings.getAresSparkHome()
			+ File.separatorChar + "conf" + File.separatorChar + LOG_4J, newConf);
    }



    @Override
    public List<RoleOSCommand> getStartCommands() {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String script = "./start-master.sh";
        String dir = EnvironmentSettings.getAresSparkHome() + File.separatorChar + "sbin";
        List<String> args = new ArrayList<String>();

        args.add("--host");
        args.add(m_node.getHost());

        args.add("--port");
        args.add(m_node.getSetting(SparkMaster.SPARK_MASTER_PORT));

        args.add("--webui-port");
        args.add(m_node.getSetting(SparkMaster.SPARK_MASTER_UI_PORT));

        RoleOSCommand command = new RoleOSCommand(dir, script, args);

        ret.add(0,command);
        return ret;

    }


    @Override
    public List<RoleOSCommand> getStopCommands() {

        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

        String script = "./stop-master.sh";
        String dir = EnvironmentSettings.getAresSparkHome() + File.separatorChar + "sbin";
        List<String> args = new ArrayList<String>();

        RoleOSCommand command = new RoleOSCommand(dir, script, args);

        ret.add(0,command);
        return ret;

    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {

        //TODO: since startup depends on this working in 2 tries, adding a safetly sleep in case of minor startup delay
        //TODO: improve and make configurable if needed

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        NodeRoleStatus ret = super.getStatus();

        RestClient r = new RestClient(NodeRole.SPARK_MASTER, SparkMaster.SPARK_MASTER_UI_PORT, platformMgr.getPlatform());
		r.setSpecificNode(m_node); //make sure we are checking THIS node for a master
        HttpResponse resp = null;
        try {
            resp = r.get("");
        } catch (Exception e) {
            ret.setState(State.STOPPED);
            ret.setStatusMessage(" unable to contact Spark worker ");
            return ret;
        }

        if (resp.getStatusLine().getStatusCode() == 200) {
            ret.setState(State.RUNNING);
        }
        else {
            ret.setState(State.ERROR);
            ret.setStatusMessage(" Spark Master UI call returned status " + resp.getStatusLine().getStatusCode());
        }

        return ret;

    }
}
