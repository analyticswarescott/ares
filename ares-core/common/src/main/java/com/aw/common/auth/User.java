package com.aw.common.auth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A user within the platform
 *
 *
 *
 */
@JsonDeserialize(as=DefaultUser.class) //deserialize as default
public interface User { //extends Principal { TODO: merge AuthenticatedUser concepts into auth package

	/**
	 * The system platform user
	 */
	public static final User SYSTEM = new DefaultUser("aw");

	/**
	 * @return A unique identifier for this user
	 */
	default public String getUserid() { return getUsername(); } //for now username is unique

	/**
	 * @return The username associated with this user
	 */
	public String getUsername();

}
