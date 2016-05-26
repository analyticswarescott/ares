package com.aw.compute.detection.correlation;

/**
 * Handle a firing from a correlation processor
 *
 *
 *
 */
public interface FiringHandler {

	/**
	 * When the given rule fires, do something
	 *
	 * @param rule The rule that fired
	 */
	public void onFiring(CorrelationRule rule, MatchData matched);

}
