package com.aw.unity.query.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;
import com.aw.unity.exceptions.InvalidFilterException;
import static org.junit.Assert.fail;

public class MatchesConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		MatchesConstraint c = new MatchesConstraint(TestDataType.FIELD_STRING, "\\d+.*");

		//make sure equality works
		assertTrue("abc111 contains numbers", c.match(new TestData(TestDataType.FIELD_STRING, "abc111")));
		assertFalse("abcabc does not contain numbers", c.match(new TestData(TestDataType.FIELD_STRING, "abcabc")));
		assertTrue("123 contains numbers", c.match(new TestData(TestDataType.FIELD_STRING, "123")));

		//check null
		try {
			c = new MatchesConstraint(TestDataType.FIELD_STRING, null);
			fail("null contains constraint should have failed");
		} catch (InvalidFilterException e) {
			//this is expected
		}
	}

}
