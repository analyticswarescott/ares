package com.aw.unity.query.constraints;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Test;

import com.aw.unity.FieldType;
import com.aw.unity.TestData;
import com.aw.unity.TestDataType;
import com.aw.unity.query.datatypes.CIDR;

/**
 * Test in constraint, both custom and non-custom
 *
 *
 *
 */
public class InConstraintTest extends ConstraintTest {

	//standard match test (not custom behavior)
	@Test
	public void match_standard() {

		InConstraint constraint = new InConstraint(TestDataType.FIELD_INT, Arrays.asList("10", "11"));

		assertTrue("int filter condition failed (10 in [10, 11])", constraint.match(new TestData(TestDataType.FIELD_INT, 10)));
		assertFalse("int filter condition failed (12 in [10, 11])", constraint.match(new TestData(TestDataType.FIELD_INT, 12)));

	}

	//custom match test (ip in check)
	@Test
	public void match_custom() throws Exception {

		InConstraint constraint = new InConstraint(TestDataType.FIELD_IP, new TreeSet<Object>() {
			{
				add(FieldType.IP_ADDRESS.parse("1.2.3.4"));
				add(FieldType.IP_ADDRESS.parse("2.3.3.5"));
				add(FieldType.IP_ADDRESS.parse("10.11.12.13/24"));
				add(FieldType.IP_ADDRESS.parse("4.5.6.7/8"));
			}
		});

		assertTrue(constraint.match(new TestData(TestDataType.FIELD_IP,  "1.2.3.4")));
		assertTrue(constraint.match(new TestData(TestDataType.FIELD_IP, "10.11.12.60")));

		//make sure the CIDRs are first
		Iterator<Object> values = constraint.getValues().iterator();
		assertTrue("cidrs need to come first in ip address filters", values.next() instanceof CIDR);
		assertTrue("cidrs need to come first in ip address filters", values.next() instanceof CIDR);

	}

}
