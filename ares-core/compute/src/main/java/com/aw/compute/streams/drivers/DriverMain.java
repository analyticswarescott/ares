package com.aw.compute.streams.drivers;

import java.util.Arrays;

import com.aw.compute.streams.exceptions.DriverException;

/**
 * The entry point for all drivers running within spark.
 *
 * Usage:DriverMain <className> [<tenantId>]
 *
 *
 *
 */
public class DriverMain {

	public static void main(String[] args) {

		try {

			Class<? extends Driver> driverClass = (Class<? extends Driver>)Class.forName(args[0]);

			//create, initialize, start the driver
			Driver driver = driverClass.newInstance();

			//remove the classname from the argument array
			if (args.length > 1) {
				args = Arrays.copyOfRange(args, 1, args.length);
			}

			//if there is nothing but the classname, then there are no arguments
			else {
				args = new String[0];
			}

			driver.initialize(args);
			driver.start();

		} catch (Exception e) {
			throw new DriverException("error starting driver", e);
		}

	}

	private static void setTestEnv() {

	}

}
