package com.aw.platform.nodes;

import com.aw.common.util.os.CommandResult;
import com.aw.common.util.os.SysCommandExecutor;
import com.aw.platform.RoleManager;
import com.aw.platform.exceptions.PlatformStateException;
import com.aw.platform.nodes.exceptions.NodeRoleException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage and execute an OS command
 */
public class RoleOSCommand {

    public static final Logger logger = LoggerFactory.getLogger(RoleOSCommand.class);

    protected File m_dir;
    protected String m_script;
    protected List<String> m_args;
    protected DateTime m_executedAt;
    protected DateTime m_completedAt;


	public boolean isCanBeWaitedFor() {
		return canBeWaitedFor;
	}
	public void setCanBeWaitedFor(boolean canBeWaitedFor) {
		this.canBeWaitedFor = canBeWaitedFor;
	}
	protected boolean canBeWaitedFor = true;


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(m_dir + "/" + m_script);
        for (String arg : m_args) {
            sb.append(" " + arg);
        }
        return sb.toString();
    }

    public RoleOSCommand(String script, List<String> args) {
        m_script = script;
        m_args = args;
    }

    public RoleOSCommand(String dir, String script, List<String> args) {
        m_dir = new File(dir);
        m_script = script;
        m_args = args;
    }

    public CommandResult execute() throws Exception{
    	return execute(true);
    }

    public CommandResult execute(boolean throwOnError) throws Exception{
        return execute(m_dir, throwOnError);
    }

	public void exec () throws Exception{
		 exec(m_dir);
	}

	public void exec(File dir) throws Exception{

		logger.warn(" executing command " + this.toString());
		for (String s : m_args) {
			logger.info(" arg: " + s);
		}

		// validate(); //TODO: develop way to validate system commands
		List<String> command = new ArrayList<String>();
		command.add(m_script);
		command.addAll(m_args);

		SysCommandExecutor sce = new SysCommandExecutor(command);

		//if (m_roleManager != null) {
		sce.setAddl_Environment(m_roleManager.getEnv());
		///}

		m_executedAt = DateTime.now();
		sce.execCommand(dir);

	}


	public void logCommand() {
		logger.warn(" executing command " + this.toString());
	}


    protected CommandResult execute(File dir) throws Exception {
    	return execute(dir, true);
    }

    protected CommandResult execute(File dir, boolean throwOnError) throws Exception {


       // validate(); //TODO: develop way to validate system commands
        List<String> command = new ArrayList<String>();
        command.add(m_script);
        command.addAll(m_args);

        SysCommandExecutor sce = new SysCommandExecutor(command);

		if (m_roleManager != null) {
			sce.setAddl_Environment(m_roleManager.getEnv());
		}

        m_executedAt = DateTime.now();
        CommandResult cr = sce.executeCommand(dir);

        m_completedAt = DateTime.now();


		//if(m_roleManager != null) {
			cr.stdOut = sce.getStandardOutputFromCommand();
			cr.stdErr = sce.getStandardErrorFromCommand();
		//}

		logger.info(" command result was: " + cr.toString());

        if (cr.result != 0 && throwOnError) {
        	throw new NodeRoleException(" Role OS command failed with result of  " + cr.result + " STDERR: " + cr.stdErr.toString());
        }

        return cr;



    }

    private void validate() throws Exception{
        File f = new File(m_script);
        if (!f.exists()) {
            throw new PlatformStateException(" command directory " + f.getAbsolutePath() + " does not exist ");
        }
        if (!f.canExecute()) {
            throw new PlatformStateException(" command directory " + f.getAbsolutePath() + " is not executable ");
        }



    }

/*    public boolean getErrorOnNonZero() { return m_errorOnNonZero; }
	public void setErrorOnNonZero(boolean errorOnNonZero) { m_errorOnNonZero = errorOnNonZero; }
	private boolean m_errorOnNonZero = true;*/

	public RoleManager getRoleManager() { return m_roleManager; }
	public void setRoleManager(RoleManager roleManager) { m_roleManager = roleManager; }
	private RoleManager m_roleManager;

	public List<String> getArgs() { return m_args; }

}
