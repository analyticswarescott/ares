package com.aw.platform.nodes.managers;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.RestClient;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.monitoring.NodeStatus.State;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.Rest;
import com.aw.util.Statics;

/**
 * Default role manager in case none is avaiable
 */
public class RestRoleManager extends AbstractRoleManager {

    static public final Logger logger = LoggerFactory.getLogger(RestRoleManager.class);

    static final String HOME = EnvironmentSettings.getDgHome() + File.separatorChar + "roles" + File.separatorChar + "rest";

    @Inject @com.google.inject.Inject
    public RestRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
    	super(platformMgr, docs);
	}

    @Override
    public void configure() throws Exception {
    }

    @Override
    public String getHome() {
    	return HOME;

    }

    @Override
    public NodeRole getRole() {
    	return NodeRole.REST;
    }

    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception {

        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./dg_rest.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        Platform p = platformMgr.getPlatform();

        String port = Integer.toString(p.getNode(NodeRole.REST).getSettingInt(Rest.PORT));
        String baseDir = EnvironmentSettings.getDgHome() + File.separatorChar + "roles" + File.separatorChar + NodeRole.REST;

        args.add("start");

        args.add(baseDir);
        args.add(port);

        RoleOSCommand command = new RoleOSCommand(dir, cmd,args);
		command.setCanBeWaitedFor(false);
        ret.add(0, command);
        logger.warn(" * REST start command generated: " + command.toString());
        return ret;
    }

    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String cmd = "./dg_rest.sh";
        String dir = getHome() + "/bin";
        List<String> args = new ArrayList<String>();

        args.add("stop");

        RoleOSCommand command = new RoleOSCommand(dir, cmd, args);

        ret.add(0, command);
        return ret;
    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {

        NodeRoleStatus ret = super.getStatus();

        RestClient r = new RestClient();
		r.setRole(NodeRole.REST);
		r.setPort(Rest.PORT);
		r.setSpecificNode(m_node);

        HttpResponse resp = null;
        try {
				resp = r.get(Statics.VERSIONED_REST_PREFIX + "/ping");

        } catch (Exception e) {
			//need to see the stack trace here
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();
			logger.debug(" rest status error :" + sw.toString());
            ret.setState(State.STOPPED);
            ret.setStatusMessage(" unable to contact REST ");
            return ret;
        }

        if (resp.getStatusLine().getStatusCode() == 200) {
            ret.setState(State.RUNNING);
        }
        else {
			logger.warn("rest status CODE error : " + resp.getStatusLine().getStatusCode());
            ret.setState(State.ERROR);
            ret.setStatusMessage(" REST ping call returned status " + resp.getStatusLine().getStatusCode());
        }

        return ret;


    }
}
