package com.aw.incident;

import java.util.Date;

import com.aw.common.auth.User;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default implementation for Comments
 *
 * Note the JsonProperty annotations bind this object to unity json - these property names
 * should match the names in the incident datatype repository json.
 *
 *
 *
 */
public class DefaultComment implements Comment {

	public DefaultComment() {
		setTimestamp(new Date());
	}

	public DefaultComment(User  user, String comment) {
		this();
		setUser(user);
		setComment(comment);
	}

	//generated, use all properties
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_comment == null) ? 0 : m_comment.hashCode());
		result = prime * result + ((m_timestamp == null) ? 0 : m_timestamp.hashCode());
		result = prime * result + ((m_user == null) ? 0 : m_user.hashCode());
		return result;
	}

	//generated, use all properties
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultComment other = (DefaultComment) obj;
		if (m_comment == null) {
			if (other.m_comment != null)
				return false;
		} else if (!m_comment.equals(other.m_comment))
			return false;
		if (m_timestamp == null) {
			if (other.m_timestamp != null)
				return false;
		} else if (!m_timestamp.equals(other.m_timestamp))
			return false;
		if (m_user == null) {
			if (other.m_user != null)
				return false;
		} else if (!m_user.equals(other.m_user))
			return false;
		return true;
	}

	@JsonProperty("dg_comment")
	public String getComment() { return m_comment; }
	public void setComment(String comment) { m_comment = comment; }
	private String m_comment;

	@JsonProperty("inc_com_time")
	public Date getTimestamp() { return m_timestamp; }
	public void setTimestamp(Date timestamp) { m_timestamp = timestamp; }
	private Date m_timestamp;

	@JsonProperty("inc_com_user")
	public User getUser() { return m_user; }
	public void setUser(User user) { m_user = user; }
	private User m_user;

}
