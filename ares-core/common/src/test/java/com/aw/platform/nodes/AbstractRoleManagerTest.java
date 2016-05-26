package com.aw.platform.nodes;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Rest;

public class AbstractRoleManagerTest {

	@Test
	public void testReplaceVariables() throws Exception {

		//test variable replacement
		AbstractRoleManager testManager = new TestManager();

		PlatformNode testNode = new DefaultPlatformNode("host", Rest.PORT, 12345);
		String result = testManager.replaceVariables(testNode, NodeRole.REST, "${port} this is ${port} test data ${port}");
		assertEquals("12345 this is 12345 test data 12345", result);

	}

	private class TestManager extends AbstractRoleManager {

		public TestManager() {
			super(null, null);
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
		public void configure() throws Exception {
		}

		@Override
		public List<RoleOSCommand> getStartCommands() throws Exception {
			return null;
		}

		@Override
		public List<RoleOSCommand> getStopCommands() throws Exception {
			return null;
		}


	}
}
