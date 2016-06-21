package com.aw.test.nodeservice;

import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.util.ListMap;

import javax.inject.Provider;
import java.util.List;

public class TestRoleManager extends AbstractRoleManager {

	public TestRoleManager(PlatformMgr platformMgr, NodeRole role, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
		this.role = role;
	}

	@Override
	public String getHome() {
		return null;
	}

	@Override
	public NodeRole getRole() {
		return role;
	}

	@Override
	public void configure() throws Exception {
		configure.add(role, 1);
	}

	@Override
	public List<RoleOSCommand> getStartCommands() throws Exception {
		return null;
	}

	@Override
	public List<RoleOSCommand> getStopCommands() throws Exception {
		return null;
	}

	@Override
	public NodeRoleStatus getStatus() {
		getStatus.add(role, 1);
		return new NodeRoleStatus();
	}

	@Override
	public void start() throws Exception {
		start.add(getRole(), 1);
		super.start();
	}


	@Override
	protected void doStart() throws Exception {
	}

	@Override
	protected void doStop() throws Exception {
		m_stop.add(role, 1);
	}

	public static ListMap<NodeRole, Object> configure = new ListMap<>();
	public static ListMap<NodeRole, Object> getStatus = new ListMap<>();
	public static ListMap<NodeRole, Object> start = new ListMap<>();
	public static ListMap<NodeRole, Object> m_stop = new ListMap<>();

	private NodeRole role;

}