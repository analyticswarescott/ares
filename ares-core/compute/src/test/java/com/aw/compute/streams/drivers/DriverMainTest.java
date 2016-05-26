package com.aw.compute.streams.drivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DriverMainTest {

	@Test
	public void startTest() throws Exception {

		//make sure this stuff is set
		TestDriver.s_initialized = false;
		TestDriver.s_started = false;
		TestDriver.s_tenantID = null;

		//start the driver
		DriverMain.main(new String[] { TestDriver.class.getName(), "tenant123" });

		//make sure things were called properly
		synchronized (TestDriver.class) {

			while (!TestDriver.s_initialized) {
				TestDriver.class.wait(2000L);
			}

			while (!TestDriver.s_started) {
				TestDriver.class.wait(2000L);
			}

			assertTrue("driver not initialized", TestDriver.s_initialized);
			assertTrue("driver not started", TestDriver.s_started);
			assertEquals("driver tenant ID wrong", "tenant123", TestDriver.s_tenantID);

		}

	}

	public static class TestDriver implements Driver {
		static boolean s_initialized = false;
		static boolean s_started = false;
		static String s_tenantID = null;

		@Override
		public void initialize(String... arguments) {
			s_tenantID = arguments[0];
			synchronized (TestDriver.class) {
				s_initialized = true;
				TestDriver.class.notifyAll();
			}
		}

		@Override
		public void start() {
			synchronized (TestDriver.class) {
				s_started = true;
				TestDriver.class.notifyAll();
			}
		}

	}

}
