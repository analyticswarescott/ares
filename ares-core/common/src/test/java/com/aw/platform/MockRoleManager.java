package com.aw.platform;

import java.util.List;

import javax.inject.Provider;

import com.aw.document.DocumentHandler;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;

public abstract class MockRoleManager extends AbstractRoleManager {

    protected NodeRoleStatus status;
    public boolean poisonCommands;

    public MockRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
    	super(platformMgr, docs);
	}

    @Override
    public void configure() throws Exception {
    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {
        return status == null ? super.getStatus() : status;
    }

    @Override
    public String getHome() {
        return null;
    }

    @Override
    public NodeRole getRole() {
        return null;
    }

    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        return null;
    }

}
