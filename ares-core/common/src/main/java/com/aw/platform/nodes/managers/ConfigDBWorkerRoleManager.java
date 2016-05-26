package com.aw.platform.nodes.managers;

import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUtils;
import com.aw.platform.nodes.RoleOSCommand;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by scott on 13/05/16.
 */
public class ConfigDBWorkerRoleManager extends ConfigDbBaseRoleManager {

	@Inject
	public ConfigDBWorkerRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
		super(platform, docs);
	}

	@Override
	public NodeRole getRole() {
		return NodeRole.CONFIG_DB_WORKER;
	}

	@Override
	protected void configureExt() throws Exception {

	}

	@Override
	protected List<RoleOSCommand> extractOSCommandList(String stopStart) {


		if (PlatformUtils.isSingleNodeSingleDB(platformMgr.get())) {
			//do not attempt start
			List<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
			return ret;
		}
		List<RoleOSCommand> osCommands = super.extractOSCommandList(stopStart);

		//TODO: add any extra worker-related commands here
		/*String command = "sudo";
		String dir = "/usr/bin";

		List<String> args = new ArrayList<>();
		args.add("systemctl");
		args.add(stopStart);
		args.add("postgresql");

		RoleOSCommand roleOSCommand = new RoleOSCommand(dir, command, args);
		osCommands.add(0, roleOSCommand);
		*/

		return osCommands;


	}



}
