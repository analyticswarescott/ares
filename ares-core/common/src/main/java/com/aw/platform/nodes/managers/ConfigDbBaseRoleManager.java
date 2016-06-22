package com.aw.platform.nodes.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.os.CommandResult;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;

/**
 * on 4/18/16.
 */
public abstract class ConfigDbBaseRoleManager extends AbstractRoleManager {

	public static final String PGA_CFG = "pga.conf";

	public static final String HBA_CONF = "postgresql_hba_conf";
	public static final String HBA_FILENAME = "pg_hba.conf";

	public static final String POSTGRES_CONF = "postgresql_conf";
	public static final String POSTGRES_FILENAME = "postgresql.conf";

	public static final Logger logger = LoggerFactory.getLogger(ConfigDbBaseRoleManager.class);

	public static final String CONFIG_DIR = "/var/lib/pgsql/data";

	public ConfigDbBaseRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
		super(platform, docs);
	}

	@Override
	public String getHome() {
		return EnvironmentSettings.getAppLayerHome();
	}

	@Override
	public abstract  NodeRole getRole();

	protected abstract void configureExt() throws Exception;

	@Override
	public void configure() throws Exception {

		configureExt(); //configuration for subclass
	}

	@Override
	protected void doStart() throws Exception {

		//if we aren't running yet, start postgres
		if (!isRunning()) {
			super.doStart();
		} else {
			logger.info("postgresql already running, not starting it");
		}

	}

	private boolean isRunning() throws Exception {

		//check if we are running, if so don't bother - this command will just list the available databases
		RoleOSCommand command = new RoleOSCommand("/usr/bin", "psql", Arrays.asList("-c", "\\list"));
		CommandResult result = command.execute(false);

		//postgresql is running if we get the invalid user error or if the return code is 0 and there is no error at all
		return result.getStdErr().contains("does not exist") || result.getResult() == 0 || result.getStdErr().length() == 0;

	}

	@Override
	public NodeRoleStatus getStatus() throws NodeOperationException {
		NodeRoleStatus status = super.getStatus();

		try {
			status.setState(isRunning() ? State.RUNNING : State.STOPPED);
		} catch (Exception e) {
			throw new NodeOperationException("error getting status for config db", e);
		}

		logger.info("config db state: " + status.getState());

		return status;
	}

	@Override
	public List<RoleOSCommand> getStartCommands() throws Exception {
		return extractOSCommandList("start");
	}

	@Override
	public List<RoleOSCommand> getStopCommands() throws Exception {
		return extractOSCommandList("stop");
	}

	protected List<RoleOSCommand> extractOSCommandList(String stopStart) {
		List<RoleOSCommand> osCommands = new ArrayList<>();
		String command = "sudo";
		String dir = "/usr/bin";

		List<String> args = new ArrayList<>();
		args.add("systemctl");
		args.add(stopStart);
		args.add("postgresql");

		RoleOSCommand roleOSCommand = new RoleOSCommand(dir, command, args);
		osCommands.add(0, roleOSCommand);




		return osCommands;
	}

}
