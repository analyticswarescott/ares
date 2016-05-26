package com.aw.platform.nodes.managers;

import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.nodes.RoleOSCommand;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Created by scott on 13/05/16.
 */
public class ConfigDBMasterRoleManager extends ConfigDbBaseRoleManager {


	public static final String WORKER_CONFIG_FILE = "pg_worker_list.conf";

	@Inject
	public ConfigDBMasterRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
		super(platform, docs);
	}

	@Override
	public NodeRole getRole() {
		return NodeRole.CONFIG_DB_MASTER;
	}

	@Override
	protected void configureExt() throws Exception {


		//remove existing worker config file
		//TODO: need a way to determine if citus is being used
		/*String confDir = platformMgr.get().getSettings(getRole()).get(CONFIG_DIR).toString();
		File workerConf = new File(confDir + File.separatorChar + WORKER_CONFIG_FILE);
		FileUtils.forceDelete(workerConf);

		//add worker nodes
		String s = "";
		for (PlatformNode node : platformMgr.get().getNodes(NodeRole.CONFIG_DB_WORKER)) {
			s+= node.getHost() + "\n";
		}

		FileUtils.write(workerConf, s);*/



		//assume replication is being used




	}


	@Override
	protected List<RoleOSCommand> extractOSCommandList(String stopStart) {


		List<RoleOSCommand> osCommands = super.extractOSCommandList(stopStart);

		//TODO: add our extra master-related commands here


		//1. create/verify citus extension
		//sudo -i -u postgres psql -c "CREATE EXTENSION citus;"


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
