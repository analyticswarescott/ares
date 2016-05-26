package com.aw.common.util;

/**
 * Basic name value pair implementation
 *
 *
 *
 */
public class KeyValuePair {

	public KeyValuePair() {
	}

	public KeyValuePair(String key, Object value) {
		m_key = key;
		m_value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyValuePair other = (KeyValuePair) obj;
		if (m_key == null) {
			if (other.m_key != null)
				return false;
		} else if (!m_key.equals(other.m_key))
			return false;
		if (m_value == null) {
			if (other.m_value != null)
				return false;
		} else if (!m_value.equals(other.m_value))
			return false;
		return true;
	}

	public String getKey() { return m_key; }
	public void setKey(String key) { m_key = key; }
	private String m_key;

	public Object getValue() { return m_value; }
	public void setValue(Object value) { m_value = value; }
	private Object m_value;

}
