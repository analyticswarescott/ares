package com.aw.compute.detection;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.aw.common.util.SlidingWindow;
import com.aw.unity.Data;

/**
 * Default abstract implementation for RealTimeRule
 *
 *
 *
 */
public abstract class AbstractRealTimeRule extends AbstractDetectionRule implements RealTimeRule {

	protected void initialize() {

		//determine out of order tolerance if applicable - i.e. if this instance is OutOfOrderTolerant
		Duration tolerance = null;
		if (this instanceof OutOfOrderTolerant) {
			tolerance = ((OutOfOrderTolerant)this).getTolerance();
		}

		//initialize the sliding window
		m_window = new SlidingWindow<>(getTimeUnit(), getTimeUnitCount(), tolerance);

	}

	/**
	 * Sliding window property for this real time rule
	 */
	public SlidingWindow<Data> getWindow() { return m_window; }
	public void setWindow(SlidingWindow<Data> window) { m_window = window; }
	private SlidingWindow<Data> m_window;

	/**
	 * @return The number of time units in which we will search for matches
	 */
	public int getTimeUnitCount() { return m_timeUnitCount; }
	public void setTimeUnitCount(int timeUnitCount) { m_timeUnitCount = timeUnitCount; }
	private int m_timeUnitCount;

	/**
	 * @return The atomic unit of time for this collection rule
	 */
	public ChronoUnit getTimeUnit() { return m_timeUnit; }
	public void setTimeUnit(ChronoUnit timeUnit) { m_timeUnit = timeUnit; }
	private ChronoUnit m_timeUnit = null;

}
