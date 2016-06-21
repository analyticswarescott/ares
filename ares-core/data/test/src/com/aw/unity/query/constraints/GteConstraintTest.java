package com.aw.unity.query.constraints;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;

/**
 * Test gte
 *
 *
 */
public class GteConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		GteConstraint c = new GteConstraint(TestDataType.FIELD_DOUBLE, 1.1D);

		//make sure equality works
		assertFalse("1.09 < 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.09D)));
		assertTrue("1.1 == 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.1D)));
		assertTrue("1.11 > 1.1", c.match(new TestData(TestDataType.FIELD_DOUBLE, 1.11D)));

	}

}
