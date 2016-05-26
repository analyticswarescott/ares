package com.aw.platform.nodes;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.aw.common.inject.TestProvider;
import com.aw.document.DocumentHandler;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.MockRoleManager;
import com.aw.platform.PlatformMgr;
import com.aw.platform.RoleManager;
import com.aw.platform.nodes.exceptions.NodeRoleException;
import com.google.common.collect.Lists;

/**
 * Created by aron on 1/13/16.
 */
public class RoleOSCommandTest {

	Provider<PlatformMgr> platformMgr;
	Provider<DocumentHandler> docs;

	@Before
	public void before() throws Exception {

		PlatformMgr mock = Mockito.mock(PlatformMgr.class);
		platformMgr = new TestProvider<>(mock);
		docs = new TestProvider<>(new TestDocumentHandler());

	}

    @Test
    public void toStringTest() throws Exception {
        {
            RoleOSCommand command = new RoleOSCommand("/tmp", "ls", Lists.newArrayList("-lrth"));
            String cmd = command.toString();
            assertEquals("/tmp/ls -lrth", cmd);
        }
        {
            RoleOSCommand command = new RoleOSCommand("ls", Lists.newArrayList("-lrth"));
            String cmd = command.toString();
            assertEquals("null/ls -lrth", cmd);
        }
    }

    @Test
    public void execute() throws Exception {
        // NOTE: this test assumes that we are using Bash shell in a *nix environment!
        final RoleOSCommand command = new RoleOSCommand("/bin", "ls", Lists.newArrayList("-lrth"));
        RoleManager roleManager = new MockRoleManager(platformMgr.get(), docs) {
            @Override
            public List<RoleOSCommand> getStartCommands() throws Exception {
                return Lists.newArrayList(command);
            }
        };
        command.setRoleManager(roleManager);
        command.execute();
    }

    @Test(expected=Exception.class)
    public void executeBadCommand() throws Exception {
        // NOTE: this test assumes that we are using Bash shell in a *nix environment!
        final RoleOSCommand command = new RoleOSCommand("/bin", "lsBADBAD", Lists.newArrayList("-lrth"));
        RoleManager roleManager = new MockRoleManager(platformMgr.get(), docs) {
            @Override
            public List<RoleOSCommand> getStartCommands() throws Exception {
                return Lists.newArrayList(command);
            }
        };
        command.setRoleManager(roleManager);
        command.execute();
    }

    @Test(expected=NodeRoleException.class)
    public void executeFailedCommand() throws Exception {
        // NOTE: this test assumes that we are using Bash shell in a *nix environment!
        final RoleOSCommand command = new RoleOSCommand("/bin", "ls", Lists.newArrayList("-lrth", "NONEXISTENTDIR"));
        RoleManager roleManager = new MockRoleManager(platformMgr.get(), docs) {
            @Override
            public List<RoleOSCommand> getStartCommands() throws Exception {
                return Lists.newArrayList(command);
            }
        };
        command.setRoleManager(roleManager);
        command.execute();
    }


}
