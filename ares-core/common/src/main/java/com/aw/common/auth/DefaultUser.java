package com.aw.common.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Default user in the incident model. This should be replaced or merged with any other User class in the system.
 *
 *
 */
@JsonSerialize(using=ToStringSerializer.class, as=String.class)
public class DefaultUser implements User {

	public DefaultUser() {
	}

	public DefaultUser(String username) {
		m_username = username;
	}

	@Override
	public String toString() {
		return m_username;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_username == null) ? 0 : m_username.hashCode());
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
		DefaultUser other = (DefaultUser) obj;
		if (m_username == null) {
			if (other.m_username != null)
				return false;
		} else if (!m_username.equals(other.m_username))
			return false;
		return true;
	}

	@JsonCreator
	public static User fromString(String username) {
		return new DefaultUser(username);
	}

	public String getUsername() { return m_username; }
	public void setUsername(String username) { m_username = username; }
	private String m_username;

}
