package com.aw.platform.nodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aw.common.util.os.CommandResult;
import com.aw.common.util.os.SysCommandExecutor;
import com.aw.platform.NodeRole;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;

/**
 * For use when ps is the only viable way to verify a java proc
 */
public class NodeUtil {

	static final Logger logger = Logger.getLogger(NodeUtil.class);

	/**
	 * Modifies the status based on the running process count.
	 *
	 * @param lookFor
	 * @param runningCount
	 * @return If process count == 0, return will be STOPPED, if process count == runningCount, return will be RUNNING, anything else will be ERROR
	 */
	public static void statusFromProcessCount(NodeRole role, String lookFor, int runningCount, NodeRoleStatus status) {

		int procs = 0;
		try {

			procs = countJavaProcs(lookFor);

			if (procs == runningCount) {
				status.setState(State.RUNNING);
			}

			//if we weren't told to stop or start
			else if (status.getState() == State.UNKNOWN) {

				//if there are no processes running, we're stopped
				if (procs == 0 && status.getState() == State.UNKNOWN) {
					status.setState(State.STOPPED);
				}

				//else if it's not what we expected, error
				else if (procs != runningCount) {
					status.setState(State.ERROR);
					status.setStatusMessage("wrong number of processes detected, expected=" + runningCount + " actual=" + procs);
				}
			}

		} catch (Exception e) {

			//set the error information if there was a problem detecting processes running
			status.setState(State.ERROR);
			status.setStatusMessage("couldn't get process count: " + e.getMessage());

		}

	}

    public static int countJavaProcs(String lookFor) throws Exception {
        List<String> cmd = new ArrayList<String>();
        cmd.add("ps");
        cmd.add("-ef");
        CommandResult cr = SysCommandExecutor.executeSimple(cmd);

        String s = cr.stdOut.toString();

        int cnt = 0;
        String[] lines = s.split(System.getProperty("line.separator"));
        for (String l : lines) {
            if (l.contains(lookFor) ) {
                //System.out.println(l);
                cnt++;
            }
        }

        return cnt;
    }
}
