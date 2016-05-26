package com.aw.compute.streams.drivers;

/**
 * A driver runs a spark streaming job.
 *
 * TODO: determine how we actually call these methods
 *
 *
 */
public interface Driver {

	/**
	 * Intiailize the driver
	 */
	public void initialize(String... arguments) throws Exception;

	/**
	 * Start the driver
	 */
	public void start() throws Exception;

}
