package com.aw.platform.nodes.managers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;

/**
 * Default role manager in case none is avaiable
 */
public class DefaultRoleManager extends AbstractRoleManager {

	@Inject
	public DefaultRoleManager(PlatformMgr platform, Provider<DocumentHandler> docs) {
		super(platform, docs);
	}

	@com.google.inject.Inject
	public DefaultRoleManager(PlatformMgr platform, com.google.inject.Provider<DocumentHandler> docs) {
		super(platform, docs);
	}

	@Override
    public void configure() throws Exception {

    }

    @Override
    public void doStart() throws Exception {

    }

    @Override
    public NodeRole getRole() {
    	return NodeRole.NODE;
    }

    @Override
    public String getHome() {
    	return null;
    }


    @Override
    public List<RoleOSCommand> getStartCommands() {
        return null;
    }

    @Override
    public void doStop() throws Exception {

    }

    @Override
    public List<RoleOSCommand> getStopCommands() {
        return null;
    }

}
