package com.aw.platform.monitoring;

import java.util.Collections;

import com.aw.common.auth.User;
import com.aw.common.tenant.Tenant;
import com.aw.platform.DefaultPlatformError;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformError;
import com.aw.platform.monitoring.NodeStatus.State;

/**
 * Defines the State and last status activity of a NodeRole
 */
public class NodeRoleStatus {

	public NodeRoleStatus() {
	}

	public NodeRoleStatus(State state, String msg) {
		m_state = state;
		m_statusMessage = msg;
	}

	public NodeRoleStatus(NodeRole role, State state, String msg, Exception exception) {
		m_state = state;
		m_statusMessage = msg;
		this.error = new DefaultPlatformError(Tenant.SYSTEM, User.SYSTEM, role, Collections.emptyList(), exception);
	}

	/**
	 * @return The state of this role within this node
	 */
	public State getState() { return m_state; }
	public void setState(State state) { m_state = state; }
	private State m_state;

	/**
	 * @return Most recent status message
	 */
	public String getStatusMessage() { return m_statusMessage; }
	public void setStatusMessage(String statusMessage) { m_statusMessage = statusMessage; }
	private String m_statusMessage = "(none)";

	/**
	 * Any error while getting status
	 */
	public PlatformError getError() { return this.error;  }
	public void setError(PlatformError error) { this.error = error; }
	private PlatformError error;



}
