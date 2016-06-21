package com.aw.unity.query.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.ConstraintOperator;

public class TimestampValueParserTest {

	@Test
	public void test() throws Exception {

		//make sure we parse standard formatted version
		TimestampValueParser tvp = new TimestampValueParser();
		List<Object> parsed = tvp.parse(ConstraintOperator.GTE, Collections.singletonList("2015-11-02 15:36:11.344 -0500"));

		assertEquals("formatted time wrong", 1446496571344L, ((Instant)parsed.get(0)).toEpochMilli());

		try {

			tvp.parse(ConstraintOperator.GTE, Collections.singletonList("abc123"));
			fail("parsing invalid date abc123 should have failed with an InvalidFilterException");

		} catch (InvalidFilterException e) {
			//we should get this here
		}

		//make sure numbers work fine
		Object value = tvp.parse(null, null, "1234567890");
		assertEquals("numeric timestamp not equal to expected", 1234567890L, ((Instant)value).toEpochMilli());

	}

}
