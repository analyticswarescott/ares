package com.aw.common.util;

/**
 * A versioned object.
 *
 *
 *
 */
public class VersionedObject<T> {

	public VersionedObject() {
	}

	public VersionedObject(long version, T object) {
		m_version = version;
		m_object = object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_object == null) ? 0 : m_object.hashCode());
		result = prime * result + (int) (m_version ^ (m_version >>> 32));
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
		VersionedObject<?> other = (VersionedObject<?>) obj;
		if (m_object == null) {
			if (other.m_object != null)
				return false;
		} else if (!m_object.equals(other.m_object))
			return false;
		if (m_version != other.m_version)
			return false;
		return true;
	}

	public long getVersion() { return m_version; }
	public void setVersion(long version) { m_version = version; }
	private long m_version;

	public T get() { return m_object; }
	public void set(T object) { m_object = object; }
	private T m_object;

}
