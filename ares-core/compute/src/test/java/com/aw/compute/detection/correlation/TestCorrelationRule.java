package com.aw.compute.detection.correlation;

import com.aw.common.exceptions.ProcessingException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;

/**
 * A test correlation rule
 *
 *
 *
 */
public class TestCorrelationRule implements CorrelationRule {

	public TestCorrelationRule() {
	}

	@Override
	public boolean correlate(Data data) throws ProcessingException {
		m_event = data;
		return true;
	}

	@Override
	public String getType() {
		return "test";
	}

	@Override
	public MatchData detect(long time) throws StreamProcessingException {
		return new MatchData(this, m_event);
	}

	@Override
	public CorrelationMethod getCorrelationMethod() {
		return m_correlationMethod;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public String getName() {
		return m_name;
	}

	String m_id;
	String m_name;
	CorrelationMethod m_correlationMethod;
	Data m_event;

}
