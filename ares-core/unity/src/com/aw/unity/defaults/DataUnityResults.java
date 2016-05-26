package com.aw.unity.defaults;

import java.util.Iterator;
import java.util.List;

import com.aw.unity.Data;
import com.aw.unity.UnityResults;

/**
 * Unity query results in Data object format
 *
 *
 *
 */
public class DataUnityResults implements UnityResults, Iterable<Data> {

	public DataUnityResults(List<Data> data) {
		m_data = data;
	}

	public DataUnityResults() {
	}

	@Override
	public Iterator<Data> iterator() {
		return m_data.iterator();
	}

	public List<Data> getData() { return m_data; }
	public void setData(List<Data> data) { m_data = data; }
	private List<Data> m_data;

}
