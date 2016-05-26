package com.aw.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void toUnix() {
		long unix = DateUtil.fromMSToUnixTime(130927812880000000L);
		assertEquals(1448307688000L, unix);
	}

	@Test
	public void toMS() {
		long unix = DateUtil.fromUnixToMSTime(1448307688000L);
		assertEquals(130927812880000000L, unix);
	}

}
