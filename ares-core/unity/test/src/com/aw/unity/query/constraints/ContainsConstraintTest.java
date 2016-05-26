package com.aw.unity.query.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;
import com.aw.unity.exceptions.InvalidFilterException;
import static org.junit.Assert.fail;

public class ContainsConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		ContainsConstraint c = new ContainsConstraint(TestDataType.FIELD_STRING, "abc");

		//make sure equality works
		assertTrue("123abc123 contains abc", c.match(new TestData(TestDataType.FIELD_STRING, "123abc123")));
		assertFalse("123123123 does not contain abc", c.match(new TestData(TestDataType.FIELD_STRING, "123123123")));

		//check null
		try {
			c = new ContainsConstraint(TestDataType.FIELD_STRING, null);
			fail("null contains constraint should have failed");
		} catch (InvalidFilterException e) {
			//this is expected
		}
	}

}
