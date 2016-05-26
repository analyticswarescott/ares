package com.aw.compute.streams.processor.correlation;

import java.io.Serializable;

import com.aw.unity.Data;

public class CorrelationRuleState implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return getUser() + "=" + getCount();
	}

	public void process(Data data) {
		m_count++;
	}

	public String getUser() { return m_user; }
	public void setUser(String user) { m_user = user; }
	private String m_user;

	public int getCount() { return m_count; }
	private int m_count;

}
