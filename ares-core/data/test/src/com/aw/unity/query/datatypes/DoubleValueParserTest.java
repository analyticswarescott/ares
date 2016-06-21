package com.aw.unity.query.datatypes;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.unity.exceptions.InvalidFilterException;

public class DoubleValueParserTest {

	@Test
	public void test() throws Exception {

		try {
			Double value = (Double)new DoubleValueParser().parse(null, null, "12.5");
			assertEquals(new Double(12.5), value);

			new DoubleValueParser().parse(null, null, "abc123");
			fail("should have failed parsing abc123");
		} catch (InvalidFilterException e) {
		}

	}
}
