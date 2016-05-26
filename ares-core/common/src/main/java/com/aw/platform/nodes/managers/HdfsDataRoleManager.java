package com.aw.platform.nodes.managers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.xml.Property;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.NodeUtil;
import com.aw.platform.nodes.RoleOSCommand;

/**
 * Role manager for HDFS Node
 */
public class HdfsDataRoleManager extends HDFSBaseRoleManager {

	@Inject
	public HdfsDataRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

	@Override
	public NodeRole getRole() {
		return NodeRole.HDFS_DATA;
	}


	@Override
	protected void configureHDFSSiteForRole(List<Property> propertyList) {

	}



	@Override
	public List<RoleOSCommand> getStartCommands() throws Exception {
		ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

		//******* datanode daemon start
		String script = "./hadoop-daemon.sh";
		List<String> args = new ArrayList<String>();
		String dir = getHome() + "/sbin";

		args.add("start");
		args.add("datanode");

		RoleOSCommand command = new RoleOSCommand(dir, script,args);

		ret.add(0, command);

		return ret;
	}


	@Override
	public List<RoleOSCommand> getStopCommands() throws Exception {
		ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

		//******* datanode daemon start
		String script = "./hadoop-daemon.sh";
		List<String> args = new ArrayList<String>();
		String dir = getHome() + "/sbin";

		args.add("stop");
		args.add("datanode");

		RoleOSCommand command = new RoleOSCommand(dir, script,args);

		ret.add(0, command);

		return ret;
	}

	@Override
	public NodeRoleStatus getStatus() throws NodeOperationException {

		NodeRoleStatus ret = super.getStatus();
		NodeUtil.statusFromProcessCount(getRole(), "DataNode", 1, ret);
		return ret;

	}
}