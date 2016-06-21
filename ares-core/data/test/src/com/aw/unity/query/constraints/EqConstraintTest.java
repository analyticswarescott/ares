package com.aw.unity.query.constraints;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.TestData;
import com.aw.unity.TestDataType;
import com.aw.unity.query.datatypes.IpAddress;

/**
 * Test equality.
 *
 *
 */
public class EqConstraintTest extends ConstraintTest {

	@Test
	public void match() throws Exception{

		//check basic equality
		EqConstraint eq = new EqConstraint(TestDataType.FIELD_IP, new IpAddress("1.2.3.4"));

		//make sure equality works
		assertTrue(eq.match(new TestData(TestDataType.FIELD_IP, "1.2.3.4")));

		//make sure does not equal works..
		assertFalse("ip addresses should not be equal", eq.match(new TestData(TestDataType.FIELD_IP, "1.2.3.5",
				 															  TestDataType.FIELD_STRING, "blah",
				 															  TestDataType.FIELD_LONG, "12345")));

	}

}
