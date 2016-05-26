package com.aw.platform.monitoring.os;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.util.os.CommandResult;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.Node;

/**
 * Collect raw data from sysstat
 */
public class SysStatReader implements SysStatInterface {

	public static final Logger LOGGER = LoggerFactory.getLogger(SysStatReader.class);

	@Override
	public String getRawData(Platform p, Instant t) throws Exception {

		RoleOSCommand tmp = buildRawDataCommand(p, t);
		CommandResult cr = tmp.execute();

		if (cr.result != 0) {
			LOGGER.error(" ERROR getting SYSTEM stats command " + cr.stdErr.toString());
		}
		String s = cr.stdOut.toString();
		return s;

	}

	RoleOSCommand buildRawDataCommand(Platform p, Instant t) {

		//commands used to get most recent report:
		//  sadf -dh  -s 05:59:00 -e 06:01:00  -- -bBqrRSuvwW -I SUM -I XALL  -u ALL

		List<String> cmd = new ArrayList<String>();
		//cmd.add("sadf");
		cmd.add("-dh");
		addTimeOptions(cmd, p, t);
		cmd.add("--");
		cmd.add("-bBqrRSuvwW");
		cmd.add("-I");
		cmd.add("SUM");
		cmd.add("-I");
		cmd.add("XALL");
		cmd.add("-u");
		cmd.add("ALL"); //to get VM stats for CPU info

		return new RoleOSCommand("sadf" , cmd);

	}

	@Override
	public String getRawNetworkData(Platform p, Instant t) throws Exception {

		RoleOSCommand cmd = buildRawNetworkDataCommand(p, t);
		CommandResult cr = cmd.execute();

		if (cr.result != 0) {
			LOGGER.error(" ERROR getting system stats " + cr.stdErr.toString());
		}


		String s = cr.stdOut.toString();
		return s;

	}

	RoleOSCommand buildRawNetworkDataCommand(Platform p, Instant t) {

		//command to get network data in XML format to allow summarization
		// sadf -xh  -s 05:59:00 -e 06:01:00  -- -n DEV,EDEV
		List<String> cmd = new ArrayList<String>();
		//cmd.add("sadf");
		cmd.add("-xh");
		addTimeOptions(cmd, p, t);
		cmd.add("--");
		cmd.add("-n");
		cmd.add("DEV,EDEV");

		return new RoleOSCommand("sadf" , cmd);

	}

	private void addTimeOptions(List<String> currentCommand, Platform p, Instant t) {

		//convert to DateTime for formatting ease


		Instant t0 = t.minusSeconds(p.getSettings(NodeRole.NODE).getSettingInt(Node.STAT_COLLECT_INTERVAL) * 60);

		ZonedDateTime z1 = t0.atZone(ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String startTime = formatter.format(z1.toLocalDateTime());


		String endtime = formatter.format(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());

		currentCommand.add("-s");
		currentCommand.add(startTime);
		currentCommand.add("-e");
		currentCommand.add(endtime);


	}


}
