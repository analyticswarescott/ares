package com.aw.compute.detection.correlation;

import java.time.Duration;
import java.util.Deque;

import com.aw.common.util.SlidingWindow.Element;
import com.aw.compute.detection.AbstractRealTimeRule;
import com.aw.compute.detection.SimpleRule;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;

/**
 * N SimpleRules occurred in M time units.
 *
 *
 *
 */
public class CollectionRule extends AbstractRealTimeRule implements CorrelationRule {

	public static final String TYPE = "collection";

	@Override
	public boolean correlate(Data data) {

		boolean ret = false;

		if (m_simpleRule.isMatch(data)) {

			//return that we matched
			ret = true;

			//add the data that matched to our sliding window
			getWindow().add(data, data.getTime());

		}

		return ret;

	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * Detects a set of matches within a time period
	 *
	 * @throws StreamProcessingException
	 */
	public MatchData detect(long start) throws StreamProcessingException {

		//get the collection of matched data
		Deque<Element<Data>> data = getWindow().find(m_matchCount, m_duration, start);

		//return the match data
		return new MatchData(this, data);

	}

	public CorrelationMethod getCorrelationMethod() { return m_correlationMethod; }
	public void setCorrelationMethod(CorrelationMethod correlationMethod) { m_correlationMethod = correlationMethod; }
	private CorrelationMethod m_correlationMethod;

	/**
	 * @return The number of times this rule must match within the duration
	 */
	public int getMatchCount() { return m_matchCount; }
	public void setMatchCount(int matchCount) { m_matchCount = matchCount; }
	private int m_matchCount;

	/**
	 * @return The simple rule that must occur N times
	 */
	public SimpleRule getSimpleRule() { return m_simpleRule; }
	public void setSimpleRule(SimpleRule simpleRule) { m_simpleRule = simpleRule; }
	private SimpleRule m_simpleRule;

	/**
	 * @return The maximum duration over which we are looking for N matches
	 */
	public Duration getDuration() { return m_duration; }
	public void setDuration(Duration duration) { m_duration = duration; }
	private Duration m_duration;

}
