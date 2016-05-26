package com.aw.compute.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.action.AbstractAction;
import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.alarm.action.AlarmAction;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.compute.TestUnityInstance;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.TestComputeModule;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.incident.action.CreateIncidentAction;
import com.aw.incident.action.IncidentActionType;
import com.aw.unity.Data;
import com.aw.unity.json.JSONData;
import com.aw.util.Statics;

public class SimpleRuleTest {


	@Test
	public void test() throws Exception {

		//use compute test dependencies
		ComputeInjector.init(new TestComputeModule());

		DocumentHandler docs = ComputeInjector.get().getInstance(DocumentHandler.class);

		SecurityUtil.setThreadSystemAccess();
		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");
		TestUnityInstance instance = new TestUnityInstance();
		SimpleRule rule = docs.getDocument(DocumentType.SIMPLE_RULE, "sample_rule").getBodyAsObject();

		//verify some basics
		assertEquals("rule action count wrong", 5, rule.getActions().size());
		assertEquals("rule action type wrong", CreateIncidentAction.class, rule.getActions().get(0).getClass());
		assertEquals("rule id wrong", "sample_rule", rule.getId());
		assertEquals("rule severity wrong", 75, rule.getSeverity());

		//test match
		Data dataMatch = new JSONData(instance.getDataType("user_file_copy"), new JSONObject("{\n" +
				"	\"pi_pn\" : \"test_nvidia_test\"\n" +
				"}"));
		boolean match = rule.isMatch(dataMatch);
		assertTrue(match);

		Data dataNoMatch = new JSONData(instance.getDataType("user_file_copy"), new JSONObject("{\n" +
				"	\"pi_pn\" : \"test_nvidia_excel_test\"\n" +
				"}"));
		match = rule.isMatch(dataNoMatch);
		assertFalse(match);

		//test process
		assertTrue(rule.getActions().get(3) instanceof AlarmAction);
		rule.getActions().clear();
		rule.getActions().add(new TestAction());
		rule.process(dataMatch);
		assertEquals(1, TestAction.actionsTaken.get());

	}

	public static class TestAction extends AbstractAction {

		private static AtomicInteger actionsTaken = new AtomicInteger(0);

		public TestAction() {
		}

		@Override
		public boolean isPersistable() {
			return false;
		}

		@Override
		public void execute(ActionContext ctx) throws ActionExecutionException {
			actionsTaken.incrementAndGet();
		}

		@Override
		public ActionType getType() {
			return IncidentActionType.ERROR_ASSOCIATION; //just return something
		}

	}

}
