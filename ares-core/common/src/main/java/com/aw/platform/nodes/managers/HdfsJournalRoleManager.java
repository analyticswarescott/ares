package com.aw.platform.nodes.managers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.xml.Property;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.NodeUtil;
import com.aw.platform.nodes.RoleOSCommand;

/**
 * Role manager for HDFS Node
 */
public class HdfsJournalRoleManager extends HDFSBaseRoleManager {

	@Inject
	public HdfsJournalRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}


    @Override
    public NodeRole getRole() {
    	return NodeRole.HDFS_JOURNAL;
    }


    @Override
    protected void configureHDFSSiteForRole(List<Property> propertyList) {

		for (Property p : propertyList) {
			if (p.getName().equals("dfs.journalnode.edits.dir")) {
				p.setValue(EnvironmentSettings.getDgHome() + "/data/hadoop/journal");
			}
		}

    }


    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

        //******* journalnode daemon start
        String script = "./hadoop-daemon.sh";
        List<String> args = new ArrayList<String>();
        String dir = getHome() + "/sbin";

        args.add("start");
        args.add("journalnode");

        RoleOSCommand command = new RoleOSCommand(dir, script,args);

        ret.add(0, command);

        return ret;
    }


    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

        //******* journalnode daemon start
        String script = "./hadoop-daemon.sh";
        List<String> args = new ArrayList<String>();
        String dir = getHome() + "/sbin";

        args.add("stop");
        args.add("journalnode");

        RoleOSCommand command = new RoleOSCommand(dir, script,args);

        ret.add(0, command);
        return ret;
    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {

        NodeRoleStatus ret = super.getStatus();

        int procs = 0;
        try {
            procs = NodeUtil.countJavaProcs("JournalNode");
        } catch (Exception e) {
            throw new NodeOperationException("error getting status for " + getRole(), e);
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
