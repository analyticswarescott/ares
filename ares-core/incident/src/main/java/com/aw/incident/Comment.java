package com.aw.incident;

import java.util.Date;

import com.aw.common.auth.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A comment on an incident
 *
 *
 *
 */
@JsonDeserialize(as=DefaultComment.class) //deserialize as default
public interface Comment {

	/**
	 * @return The user associated with a comment
	 */
	public User getUser();

	/**
	 * @return The timestamp of the comment
	 */
	public Date getTimestamp();

	/**
	 * @return The comment string itself
	 */
	public String getComment();

}
