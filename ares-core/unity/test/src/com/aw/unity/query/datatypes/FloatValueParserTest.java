package com.aw.unity.query.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

import com.aw.unity.exceptions.InvalidFilterException;

public class FloatValueParserTest {

	@Test
	public void test() throws Exception {

		try {
			assertEquals(new FloatValueParser().parse(null, null, "12.5"), new Float(12.5));

			new FloatValueParser().parse(null, null, "abc123");
			fail("should have failed parsing abc123");
		} catch (InvalidFilterException e) {
		}

	}
}
