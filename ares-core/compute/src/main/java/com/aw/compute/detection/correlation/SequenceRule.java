package com.aw.compute.detection.correlation;

import java.util.ArrayList;
import java.util.List;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.util.JSONUtils;
import com.aw.compute.detection.AbstractDetectionRule;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;

/**
 * Multiple EventRules occurred in sequence. (A -> B -> C)
 *
 *
 *
 */
public class SequenceRule extends AbstractDetectionRule implements CorrelationRule {

	public static final String TYPE = "sequence";

	@Override
	public boolean correlate(Data data) throws ProcessingException {

		boolean ret = false; //did we fire
		boolean match = false; //did any child fire

		//have children process the data
		for (CorrelationRule rule : m_childRules) {

			//make sure all child rules see the event
			match = match || rule.correlate(data);

		}

		//if any components matched, detect
		if (match) {

			//see if we have new match data for a new firing
			MatchData matchData = detect(0L);

			//if we do, fire
			if (matchData != null) {

				fire(matchData);

				//update return value accordingly
				ret = true;

			}

		}

		//return whether we fired
		return ret;

	}

	@Override
	public String getType() {
		return TYPE;
	}

	private void fire(MatchData matchData) {
		System.out.println("firing:\n\n" + JSONUtils.objectToString(matchData) + "\n\n");
	}

	public MatchData detect(long startTime) throws StreamProcessingException {

		long start = 0L;
		int count = 0;
		MatchData ret = null;

		for (CorrelationRule rule : m_childRules) {

			//detect next child
			MatchData data = rule.detect(start);

			//if no match, continue
			if (data == null) {
				continue;
			}

			//lazy creation of top level MatchData
			else if (ret == null) {
				ret = new MatchData();
			}

			//add in the match data
			ret.add(data);

			//keep track of how many children have activated
			count++;

			//if we have what we need, stop
			if (count >= m_count) {
				break;
			}

			//if we have a match
			if (data != null) {

				//and if ordered, start time of next detection must be after last child
				if (m_ordered) {
					start = data.getEndTime();
				}

			}

		}

		//return what we have, if anything
		return ret;

	}

	public List<CorrelationRule> getChildRules() { return m_childRules; }
	public void setChildRules(List<CorrelationRule> childRules) { m_childRules = childRules; }
	private List<CorrelationRule> m_childRules = new ArrayList<>();

	/**
	 * @return Is this an ordered sequence
	 */
	public boolean getOrdered() { return m_ordered; }
	public void setOrdered(boolean ordered) { m_ordered = ordered; }
	private boolean m_ordered = false;

	/**
	 * @return The number of elements that must fire for this rule to fire
	 */
	public int getCount() { return m_count; }
	public void setCount(int count) { m_count = count; }
	private int m_count;

	public CorrelationMethod getCorrelationMethod() { return m_correlationMethod; }
	public void setCorrelationMethod(CorrelationMethod correlationMethod) { m_correlationMethod = correlationMethod; }
	private CorrelationMethod m_correlationMethod;

}
