package com.aw.action;

import static com.aw.common.util.JSONUtils.objectToString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.TestPlatform;
import com.aw.common.auth.User;
import com.aw.common.inject.TestProvider;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.unity.UnityInstance;

import javax.inject.Provider;

public class DefaultActionManagerTest {

	enum Actions implements ActionType {
		TEST_TYPE;

		@Override
		public Class<? extends Action> getClassType() {
			return TestAction.class;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	@Test
	public void variables() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		DefaultActionManager mgr = newActionManager();

		String testAction1 = objectToString(new TestAction());
		String testAction2 = objectToString(new TestAction());

		actionCalls = 0;
		mgr.takeActions(Arrays.asList(new Action[] { mgr.newAction(testAction1), mgr.newAction(testAction2) }));

		//test variables
		Provider<PlatformMgr> platformMgrProvider = mock(Provider.class);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		when(platformMgrProvider.get()).thenReturn(platformMgr);
		when(platformMgr.getPlatform()).thenReturn(new TestPlatform());

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), platformMgrProvider);
		ctx.registerVariable("test_variable", new TestObject());

		String value = ctx.getVariableString("test_variable.prop");

		assertEquals("testing 123", value);
		assertEquals(2, actionCalls);

	}

	@Test(expected=ActionExecutionException.class)
	public void exceptions() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		DefaultActionManager mgr = newActionManager();

		String testAction1 = objectToString(new TestAction(true));

		actionCalls = 0;
		mgr.takeActions(Arrays.asList(new Action[] { mgr.newAction(testAction1) }));

	}
	/**
	 * Action manager with stubbed dependencies
	 *
	 * @return
	 * @throws Exception
	 */
	DefaultActionManager newActionManager() throws Exception {

		DefaultActionManager ret = spy(new DefaultActionManager(
				new TestProvider<>(new TestPlatform()),
				new TestProvider<>(mock(RestCluster.class)),
				new TestProvider<>(mock(PlatformMgr.class)),
				new TestProvider<>(mock(UnityInstance.class)),
				new DefaultRootActionFactory(new DefaultActionFactory(Action.class, Actions.values()))));

		return ret;

	}

	private static int actionCalls = 0;
	static class TestAction extends AbstractAction {

		public TestAction() {
			this(false);
		}

		public TestAction(boolean throwException) {
			this.throwException = throwException;
		}

		@Override
		public boolean isPersistable() {
			return false;
		}

		@Override
		public User getUser() {
			return null;
		}

		@Override
		public <T> T copy() throws ActionCopyException {
			return (T)this;
		}

		public void prepare(ActionContext ctx) throws ActionPreparationException {
		}

		@Override
		public void execute(ActionContext ctx) throws ActionExecutionException {
			actionCalls++;

			if (throwException) {
				throw new ActionExecutionException("testing 123", this);
			}
		}

		@Override
		public UUID getGuid() {
			return null;
		}

		@Override
		public Instant getTimestamp() {
			return Instant.now();
		}

		@Override
		public void setUser(User user) {
		}

		@Override
		public ActionType getType() {
			return Actions.TEST_TYPE;
		}

		public boolean getThrowException() { return this.throwException;  }
		public void setThrowException(boolean throwException) { this.throwException = throwException; }
		private boolean throwException;

	}

	class TestObject {

		public String getProp() { return m_prop; }
		public void setProp(String prop) { m_prop = prop; }
		private String m_prop = "testing 123";

	}

	class TestActionFactory implements ActionFactory {

		@Override
		public Class<? extends Action> getBaseType() {
			return Action.class;
		}

		@Override
		public List<ActionType> getAllTypes() {
			return Arrays.asList(Actions.values());
		}

		@Override
		public boolean hasType(String strType) {
			return strType.equals("test_type");
		}

		@Override
		public ActionType toType(String strType) {
			return Actions.valueOf(strType.toUpperCase());
		}

	}
}
