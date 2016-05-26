package com.aw.compute.detection.correlation;

import com.aw.common.exceptions.ProcessingException;
import com.aw.compute.detection.DetectionRule;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;

/**
 *
 *
 *
 */
public interface CorrelationRule extends DetectionRule {

	/**
	 * Processes the Data for correlation purposes - returns whether the rule should have detect() called on it
	 */
	public boolean correlate(Data data) throws ProcessingException;

	/**
	 * @return The correlation method for this rule
	 */
	public CorrelationMethod getCorrelationMethod();

	/**
	 * Detect whether this correlation rule has been activated
	 *
	 * @param time
	 * @return
	 * @throws ProcessingException
	 */
	public MatchData detect(long time) throws StreamProcessingException;

}
