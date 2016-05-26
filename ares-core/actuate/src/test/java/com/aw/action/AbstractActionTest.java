package com.aw.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.TestPlatform;
import com.aw.common.inject.TestProvider;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.aw.unity.UnityInstance;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Provider;

public class AbstractActionTest implements SecurityAware {

	enum TestActions implements ActionType {

		TEST_TYPE;

		@Override
		public Class<? extends Action> getClassType() {
			return TestAction.class;
		}

	}



	@Test
	public void replaceVariables_bean() throws Exception {

		//for testing, system access
		SecurityUtil.setThreadSystemAccess();

		TestAction action = new TestAction("test string ${test.prop}");

		Provider<PlatformMgr> platformMgrProvider = mock(Provider.class);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		when(platformMgrProvider.get()).thenReturn(platformMgr);
		when(platformMgr.getPlatform()).thenReturn(new TestPlatform());

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), platformMgrProvider);
		ctx.registerVariable("test", new TestObject());

		final AtomicInteger recordCalls = new AtomicInteger();
		DefaultActionManager mgr = newActionManager(recordCalls);

		mgr.takeAction(action, ctx);

		assertEquals("test string testing 123", action.m_value);
		assertEquals(1, recordCalls.get());

	}

	@Test
	public void replaceVariables_annotations() throws Exception {

		//for testing, system access
		SecurityUtil.setThreadSystemAccess();

		TestAction action = new TestAction("test string ${test.prop_test}");

		Provider<PlatformMgr> platformMgrProvider = mock(Provider.class);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		when(platformMgrProvider.get()).thenReturn(platformMgr);
		when(platformMgr.getPlatform()).thenReturn(new TestPlatform());

		ActionContext ctx = new DefaultActionContext(mock(UnityInstance.class), platformMgrProvider);
		ctx.registerVariable("test", new TestObject());

		final AtomicInteger recordCalls = new AtomicInteger();
		DefaultActionManager mgr = newActionManager(recordCalls);

		mgr.takeAction(action, ctx);

		assertEquals("test string testing 234", action.m_value);
		assertEquals(1, recordCalls.get());

	}

	/**
	 * Return a testable DefaultActionManager
	 *
	 * @param recordCalls
	 * @return
	 */
	private DefaultActionManager newActionManager(final AtomicInteger recordCalls) {

		Platform platform = new TestPlatform();
		RestCluster mockRestCluster = mock(RestCluster.class);
		PlatformMgr platformMgr = mock(PlatformMgr.class);
		UnityInstance unity = mock(UnityInstance.class);

		DefaultActionManager ret = new DefaultActionManager(
				new TestProvider<Platform>(platform),
				new TestProvider<RestCluster>(mockRestCluster),
				new TestProvider<PlatformMgr>(platformMgr),
				new TestProvider<>(unity),
				new DefaultRootActionFactory()) {
					@Override
					protected void recordAction(Action action) throws Exception {
						recordCalls.incrementAndGet();
					}

		};

		return ret;

	}

	class TestAction extends AbstractAction {

		public TestAction(String value) {
			m_value = value;
		}

		@Override
		public boolean isPersistable() {
			return true;
		}

		@Override
		public void prepare(ActionContext ctx) throws ActionPreparationException {
			m_value = replaceVariables(m_value, ctx);
		}

		@Override
		public void execute(ActionContext ctx) {
		}

		@Override
		public ActionType getType() {
			return TestActions.TEST_TYPE;
		}

		private String m_value;

	}

	class TestObject {

		public String getProp() { return m_prop; }
		public void setProp(String prop) { m_prop = prop; }
	 	private String m_prop = "testing 123";

	 	@JsonProperty("prop_test")
		public String getProp2() { return m_prop2; }
		public void setProp2(String prop) { m_prop2 = prop; }
	 	private String m_prop2 = "testing 234";

	}

}
