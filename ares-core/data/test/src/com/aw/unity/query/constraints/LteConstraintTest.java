package com.aw.unity.query.constraints;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;

/**
 * Test lte
 *
 *
 */
public class LteConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		LteConstraint c = new LteConstraint(TestDataType.FIELD_DOUBLE, 1.1D);

		//make sure equality works
		assertTrue("1.09 < 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.09D)));
		assertTrue("1.1 == 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.1D)));
		assertFalse("1.11 > 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.11D)));

	}

}
