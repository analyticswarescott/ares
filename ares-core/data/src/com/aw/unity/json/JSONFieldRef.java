package com.aw.unity.json;

/**
 * A field reference in a JSON data type.
 *
 *
 *
 */
public class JSONFieldRef implements Comparable<JSONFieldRef> {

	public JSONFieldRef() {
	}

	public JSONFieldRef(String name, String[] path) {
		m_name = name;
		m_path = path;
	}

	/**
	 * Ordered base on name
	 */
	@Override
	public int compareTo(JSONFieldRef o) {

		//if same
		if (m_name == o.m_name) {
			return 0;
		}

		//nulls after
		else if (m_name == null) {
			return 1;
		}

		//we have names, do the real compare
		return m_name.compareTo(o.m_name);

	}

	/**
	 * hashcode based on name
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		return result;
	}

	/**
	 * equals based on name
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JSONFieldRef other = (JSONFieldRef) obj;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return m_name;
	}

	/**
	 * @return The name of the fieldS
	 */
	public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	/**
	 * @return The json key path to the value
	 */
	public String[] getPath() { return m_path; }
	public void setPath(String[] path) { m_path = path; }
	private String[] m_path;

	/**
	 * If this field represents a subtype, it will in turn contain fields
	 *
	 * @return The fields of this subtype
	 */
	public JSONFieldRef[] getFieldRefs() { return m_fieldRefs; }
	public void setFieldRefs(JSONFieldRef[] fieldRefs) { m_fieldRefs = fieldRefs; }
	private JSONFieldRef[] m_fieldRefs;

}
