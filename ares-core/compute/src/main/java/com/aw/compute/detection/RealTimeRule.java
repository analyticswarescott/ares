package com.aw.compute.detection;

import com.aw.common.util.SlidingWindow;
import com.aw.unity.Data;

/**
 * A logical occurrance that could be detected in real time
 *
 *
 *
 */
public interface RealTimeRule extends DetectionRule {

	/**
	 * Real time rules have a window of time in which they will evaluate whether they have been satisfied
	 *
	 * @return The sliding time window for this real time rule
	 */
	public SlidingWindow<Data> getWindow();

}
