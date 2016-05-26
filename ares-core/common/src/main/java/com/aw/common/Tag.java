package com.aw.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * A tag defined in the system.
 *
 *
 */
@JsonSerialize(using=ToStringSerializer.class, as=String.class)
public class Tag {

	private Tag() {
	}

	@JsonCreator
	public synchronized static Tag valueOf(String name) {
		Tag ret = s_tags.get(name);
		if (ret == null) {
			ret = new Tag();
			ret.m_name = name;
			s_tags.put(ret.m_name, ret);
		}
		return ret;
	}
	private static Map<String, Tag> s_tags = new HashMap<String, Tag>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
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
		Tag other = (Tag) obj;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		return true;
	}

	public String getName() { return m_name; }
	private String m_name;

	@Override
	public String toString() {
		return m_name;
	}

}
