package com.aw.unity.query.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;

/**
 * Test between
 *
 *
 */
public class BetweenConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		BetweenConstraint c = new BetweenConstraint(TestDataType.FIELD_INT, 1, 10);

		//make sure equality works
		assertFalse("0 < min", c.match(new TestData(TestDataType.FIELD_INT, 0)));
		assertTrue("1 == min", c.match(new TestData(TestDataType.FIELD_INT, 1)));
		assertTrue("9 < max", c.match(new TestData(TestDataType.FIELD_INT, 9)));
		assertFalse("10 == max", c.match(new TestData(TestDataType.FIELD_INT, 10)));

		//same thing setting values
		c = new BetweenConstraint();
		c.setField(TestDataType.FIELD_INT);
		c.setValues(Arrays.asList(new Object[] { 1, 10 }));

		//make sure equality works
		assertFalse("0 < min", c.match(new TestData(TestDataType.FIELD_INT, 0)));
		assertTrue("1 == min", c.match(new TestData(TestDataType.FIELD_INT, 1)));
		assertTrue("9 < max", c.match(new TestData(TestDataType.FIELD_INT, 9)));
		assertFalse("10 == max", c.match(new TestData(TestDataType.FIELD_INT, 10)));

	}

}
