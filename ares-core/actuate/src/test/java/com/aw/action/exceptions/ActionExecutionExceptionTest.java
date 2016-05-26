package com.aw.action.exceptions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aw.action.AbstractAction;
import com.aw.action.Action;
import com.aw.action.ActionContext;
import com.aw.action.ActionType;

public class ActionExecutionExceptionTest {

	enum Actions implements ActionType {

		TEST_TYPE;

		@Override
		public Class<? extends Action> getClassType() {
			return TestAction.class;
		}

	}
	@Test
	public void test_create() {

		Exception cause = new Exception("cause");
		Action action = new TestAction();
		ActionExecutionException e = new ActionExecutionException(action, cause);

		assertTrue(e.getExceptionMap().containsKey(action));

	}

	@Test
	public void test_add() {

		Exception cause = new Exception("cause");
		Action action = new TestAction();
		ActionExecutionException e = new ActionExecutionException(action, cause);

		Action action2 = new TestAction();
		assertFalse(e.getExceptionMap().containsKey(action2));
		Exception cause2 = new Exception("cause2");
		e.addException(action2, cause2);
		assertTrue(e.getExceptionMap().containsKey(action));
		assertSame(cause2, e.getExceptionMap().get(action2));

	}

	private static class TestAction extends AbstractAction {

		@Override
		public boolean isPersistable() {
			return false;
		}

		@Override
		public void execute(ActionContext ctx) throws ActionExecutionException {
		}

		@Override
		public ActionType getType() {
			return Actions.TEST_TYPE;
		}

	}
}
