package com.aw.unity.query.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringValueParserTest {

	@Test
	public void test() throws Exception {
		String testString = "blah";

		//just make sure it's a passthrough for now
		assertTrue(new StringValueParser().parse(null, null, testString) == testString);
	}
}
