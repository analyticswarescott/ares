package com.aw.platform.nodes.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.util.RestResponse;
import org.apache.http.HttpResponse;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.RestClient;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.SparkWorker;

/**
 * Configure and Manage a SPARK_WORKER node role.
 */
public class SparkWorkerRoleManager extends SparkBaseRoleManager {

	@Inject @com.google.inject.Inject
	public SparkWorkerRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

	@Override
	protected void configureForRole() throws Exception {
		//currently no settings files that are used by a worker -- spark-env settings are managed in the base class
	}

	@Override
    public NodeRole getRole() {
    	return NodeRole.SPARK_WORKER;
    }



	@Override
    public NodeRoleStatus getStatus() {

        //TODO: since startup depends on this working in 2 tries, adding a safetly sleep in case of minor startup delay
        //TODO: improve and make configurable if needed

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        NodeRoleStatus ret = new NodeRoleStatus();
        ret.setState(State.UNKNOWN);


        RestClient r = new RestClient(NodeRole.SPARK_WORKER, SparkWorker.WEB_UI_PORT, platformMgr);
		r.setSpecificNode(m_node);//make sure we are checking THIS node for a spark worker

        RestResponse resp = null;
        try {
            resp = r.get("");
        } catch (Exception e) {
            ret.setState(State.STOPPED);
            ret.setStatusMessage(" unable to contact Spark worker ");
            return ret;
        }

        if (resp.getStatusCode() == 200) {
            ret.setState(State.RUNNING);
        }
        else {
            ret.setState(State.ERROR);
            ret.setStatusMessage(" Spark worker UI call returned status " + resp.getStatusCode());
        }

        return ret;

    }

    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception{

        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./start-slave.sh";
        String dir = getHome() + "/sbin";
        List<String> args = new ArrayList<String>();

        PlatformNode spark = platformMgr.getPlatform().getNode(NodeRole.SPARK_MASTER);

        args.add(PlatformUtils.getSparkMasterList(platformMgr.getPlatform())); //a list of spark masters and stand-bys

        args.add("--webui-port");
        args.add(m_node.getSetting(SparkWorker.WEB_UI_PORT));

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
        ret.add(0, command);
        return ret;
    }



    @Override
    public List<RoleOSCommand> getStopCommands() {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

        String script = "./stop-slave.sh";
        String dir = EnvironmentSettings.getAresSparkHome() + File.separatorChar + "sbin";
        List<String> args = new ArrayList<String>();

        RoleOSCommand command = new RoleOSCommand(dir, script, args);

        ret.add(0,command);
        return ret;
    }


}
