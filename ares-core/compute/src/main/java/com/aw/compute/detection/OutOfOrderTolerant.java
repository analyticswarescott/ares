package com.aw.compute.detection;

import java.time.Duration;

/**
 * Implemented by anything that is out of order tolerant
 *
 *
 *
 */
public interface OutOfOrderTolerant {

	/**
	 * @return Out-of-order time tolerance - this is added to the window to allow for events delivered out of time order
	 */
	public Duration getTolerance();

}
