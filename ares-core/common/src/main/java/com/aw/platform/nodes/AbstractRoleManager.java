package com.aw.platform.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.os.CommandResult;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.RoleManager;
import com.aw.platform.Setting;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.util.Statics;

/**
 * Class to control state of NodeRoles
 */
public abstract class AbstractRoleManager implements RoleManager {

    public static final Logger logger = LoggerFactory.getLogger(AbstractRoleManager.class);

	protected RoleConfig m_roleConfig;
    protected PlatformNode m_node;
	protected Provider<DocumentHandler> docs;
   // protected Platform m_platform;

    protected PlatformMgr platformMgr;

    public Map<String, Process> getProcesses() {
        return m_processes;
    }

    protected Map<String, Process> m_processes = new HashMap<String, Process>();

    public List<RoleOSCommand> getCommand_history() {
        return m_command_history;
    }

    public void addCommand(RoleOSCommand command) {
        m_command_history.add(command);
    }

    protected List<RoleOSCommand> m_command_history = new ArrayList<RoleOSCommand>();



    public void addProcess(String key, Process p) {
        m_processes.put(key, p);
    }


    public Map<String, String> getEnv() {
        return new HashMap<String, String>();
    }

    public AbstractRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
    	this.platformMgr = platform;
    	m_roleConfig = new RoleConfig(this.getRole());
		this.docs = docs;
    }

    public void init( PlatformNode node) {
        m_node = node;
      //  m_platform = getPlatform(); //TODO: this would be used, for example in configuring multiple ZOOKEEPER nodes, which all need peer information
    }

    public void install(boolean start) throws Exception {
        //TODO: block for now to simplify exception handling
        configure();
        if (start) {
           start();
        }

    }

    public void start() throws Exception {

        doStart();
        NodeRoleStatus status = getStatus();
        report(status);
    }
    public void stop() throws Exception {
        doStop();
    }


    public void report(NodeRoleStatus status) {
        //TODO: report to REST and/or Zook
    }


    protected void doStart() throws Exception {
    	try {
            executeCommands(getStartCommands());
    	} finally {
    		m_state = State.STARTING;
    	}
    }

    protected void doStop() throws Exception {
    	try {
            executeCommands(getStopCommands());
    	} finally {
        	m_state = State.STOPPING;
    	}
    }

    public void executeCommands(List<RoleOSCommand> commands) throws Exception {
        for (RoleOSCommand command : commands)  {
            command.setRoleManager(this);
            addCommand(command); //add to history

			if (command.isCanBeWaitedFor()) {
				command.logCommand();
				CommandResult cr = command.execute();
			}
			else { //command does not return
				logger.warn("executing command that will not be waited for");
				command.exec();
			}
        }
    }

    public abstract void configure() throws Exception;

    public abstract List<RoleOSCommand> getStartCommands() throws Exception;


    public abstract List<RoleOSCommand> getStopCommands() throws Exception;

    /**
     * replace platform settings that appear as variables in the given string, in the form ${setting}, for the given
     * node role's settings
     *
     * @param settings
     * @return
     */
    protected <T extends Setting> String replaceVariables(PlatformNode node, NodeRole nodeRole, String data) throws Exception {

    	Matcher matcher = Statics.VARIABLE_PATTERN.matcher(data);

    	StringBuilder ret = new StringBuilder();
    	int last = 0;
    	while (matcher.find()) {

    		//add up to the variable
    		ret.append(data.substring(last, matcher.start()));

    		//get the setting for the variable
    		String variable = matcher.group(1);

    		//check environment
    		String value = null;
    		try {

    			//try from the environment first
    			value = EnvironmentSettings.fetch(EnvironmentSettings.Setting.valueOf(variable.toUpperCase()));

    		} catch (Exception e) {

        		//get the value for the setting
        		value = node.getSetting(nodeRole.settingValueOf(variable));

    		}

    		ret.append(value);

    		last = matcher.end();

    	}

    	//add the rest
    	ret.append(data.substring(last));

    	return ret.toString();

    }

    /**
     * Default implementation sets STARTING if start has been called, or STOPPING of stop has been called
     */
    public NodeRoleStatus getStatus() throws NodeOperationException {

    	NodeRoleStatus ret = new NodeRoleStatus();
		ret.setState(m_state);
    	return ret;

    }
    State m_state = State.UNKNOWN;

}
