package com.aw.unity.defaults;

import java.util.Iterator;
import java.util.List;

import com.aw.unity.Field;
import com.aw.unity.UnityResults;

/**
 * Default structure for unity formatted results
 *
 *
 */
public class DefaultUnityResults implements UnityResults, Iterable<List<Object>> {

	public DefaultUnityResults() {
	}

	public DefaultUnityResults(List<Field> fields, List<List<Object>> data) {
		m_fields = fields;
		m_data = data;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return m_data.iterator();
	}

	/**
	 * @return Field names
	 */
	public List<Field> getFields() { return m_fields; }
	public void setFields(List<Field> fields) { m_fields = fields; }
	private List<Field> m_fields;

	/**
	 * @return Raw data
	 */
	public List<List<Object>> getData() { return m_data; }
	public void setData(List<List<Object>> data) { m_data = data; }
	private List<List<Object>> m_data;

}
