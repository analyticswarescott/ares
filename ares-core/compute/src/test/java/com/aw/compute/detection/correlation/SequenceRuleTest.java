package com.aw.compute.detection.correlation;

import org.junit.Test;

public class SequenceRuleTest {

	@Test
	public void test() {

		SequenceRule rule = new SequenceRule();

		rule.getChildRules().add(new TestCorrelationRule());

	}

}
