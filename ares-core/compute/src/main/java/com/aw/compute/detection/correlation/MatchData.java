package com.aw.compute.detection.correlation;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.aw.common.util.SlidingWindow.Element;
import com.aw.compute.detection.DetectionRule;
import com.aw.unity.Data;

/**
 * Contains a set of matched data that can be serialized between rules
 *
 *
 *
 */
public class MatchData {

	public MatchData() {
	}

	public MatchData(DetectionRule rule, Data... matches) {
		this(rule, toElementDeque(matches));
	}

	public MatchData(DetectionRule rule, Deque<Element<Data>> matches) {
		this(rule, matches.getFirst().getTime(),  matches.getLast().getTime(), toIds(matches));
	}

	private MatchData(DetectionRule rule, long start, long end, Collection<String> ids) {
		m_ruleId = rule.getId();
		m_startTime = start;
		m_endTime = end;
		m_ids = ids;
	}

	private static Deque<Element<Data>> toElementDeque(Data... matches) {
		Deque<Element<Data>> ret = new LinkedList<Element<Data>>();
		for (Data match : matches) {
			ret.addLast(new Element<Data>(match, match.getTime().toEpochMilli()));
		}
		return ret;
	}

	/**
	 * Build a collecton of ids from the given sliding window matches
	 * @param matches The sliding window matches
	 * @return The collection of ids for the matches passed in
	 */
	private static Collection<String> toIds(Collection<Element<Data>> matches) {
		Set<String> ret = new HashSet<String>();
		for (Element<Data> match : matches) {
			ret.add(match.getData().getGuid());
		}
		return ret;
	}

	/**
	 * Add the child rule's data to this parent match data
	 *
	 * @param childData
	 */
	public void add(MatchData childData) {

		if (m_childData == null) {
			m_childData = new HashSet<>();
		}

		m_childData.add(childData);

	}

	public String getRuleId() { return m_ruleId; }
	public void setRuleId(String ruleId) { m_ruleId = ruleId; }
	private String m_ruleId;

	/**
	 * @return The start time of the match data
	 */
	public long getStartTime() { return m_startTime; }
	public void setStartTime(long startTime) { m_startTime = startTime; }
	private long m_startTime;

	/**
	 * @return The end time of the match data
	 */
	public long getEndTime() { return m_endTime; }
	public void setEndTime(long endTime) { m_endTime = endTime; }
	private long m_endTime;

	/**
	 * @return The ids involved in the match data
	 */	public Collection<String> getIds() { return m_ids; }
	public void setIds(Collection<String> ids) { m_ids = ids; }
	private Collection<String> m_ids = null;

	public Collection<MatchData> getChildData() { return m_childData; }
	public void setChildData(Collection<MatchData> childData) { m_childData = childData; }
	private Collection<MatchData> m_childData = null;
}
