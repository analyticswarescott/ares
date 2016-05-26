package com.aw.unity.query.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;
import com.aw.unity.exceptions.InvalidFilterException;
import static org.junit.Assert.fail;

public class StartsWithConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		StartsWithConstraint c = new StartsWithConstraint(TestDataType.FIELD_STRING, "abc");

		//make sure equality works
		assertTrue("abc111 starts with abc", c.match(new TestData(TestDataType.FIELD_STRING, "abc111")));
		assertFalse("123abc does not start with abc", c.match(new TestData(TestDataType.FIELD_STRING, "123abc")));
		assertTrue("abc starts with abc", c.match(new TestData(TestDataType.FIELD_STRING, "abc")));

		//check null
		try {
			c = new StartsWithConstraint(TestDataType.FIELD_STRING, null);
			fail("null starts_with constraint should have failed");
		} catch (InvalidFilterException e) {
			//this is expected
		}
	}

}
