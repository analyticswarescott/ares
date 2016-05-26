package com.aw.unity.query.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;

import com.aw.unity.exceptions.InvalidFilterException;

public class IntValueParserTest {

	@Test
	public void test() throws Exception {

		try {
			assertEquals(new IntValueParser().parse(null, null, "12"), new Integer(12));
		} catch (InvalidFilterException e) {
		}

	}
}
