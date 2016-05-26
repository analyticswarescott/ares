package com.aw.unity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.aw.platform.AbstractPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.NodeRoleSettings;
import com.aw.platform.Platform;
import com.aw.unity.exceptions.InvalidDataException;

public class TestPlatformNode extends AbstractPlatformNode {

	private static final long serialVersionUID = 1L;

	public TestPlatformNode(String host, NodeRole role, Object... settings) {

		m_host = host;
		m_role = role;

		if (settings == null) {
			return;
		}

		if (settings.length % 2 != 0) {
			throw new InvalidDataException("Settings must be a multiple of 2, setting, value, setting, value, etc");
		}

		for (int x=0; x<settings.length; x+=2) {
			m_settings.put((RoleSetting)settings[x], settings[x+1]);
		}

		//only one role for test (for now)
		m_roleSettings = new HashMap<NodeRole, NodeRoleSettings>();
		m_roleSettings.put(m_role, m_settings);

	}

	@Override
	public Collection<NodeRole> getRoles() {
		return m_roleSettings.keySet();
	}

	@Override
	public String getHost() { return m_host; }
	private String m_host;

	@Override
	protected Map<NodeRole, NodeRoleSettings> getSettings() {
		return m_roleSettings;
	}

	@Override
	public void setPlatform(Platform platform) {
		m_platform = platform;
	}

	private Platform m_platform;
	private NodeRole m_role;
	private NodeRoleSettings m_settings = new NodeRoleSettings();
	private Map<NodeRole, NodeRoleSettings> m_roleSettings = new HashMap<>();

}
