package com.aw.platform;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.aw.common.Taggable;
import com.aw.common.auth.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Any error in the functioning of the platform should result in the creation of a PlatformError object.
 *
 *
 *
 */
@JsonDeserialize(as=DefaultPlatformError.class)
public interface PlatformError extends Taggable {

	public static final String UNITY_TYPE = "platform_error";

	/**
	 * @return The unique identifier for this error
	 */
	public UUID getGuid();

	/**
	 * @return The time this error occurred
	 */
	public Instant getTimestamp();

	/**
	 * @return A string identifying the error type. This could be the simple name of an exception class for example. It should indicate the class of error that occurred.
	 */
	public String getType();

	/**
	 * @return If applicable, a stack trace related to this error. May be null if a stack trace does not apply.
	 */
	public String getStackTrace();

	/**
	 * @return The role in which this error originated. This should be the role in which the executable code ran into this error (i.e. threw an exception).
	 */
	public NodeRole getOriginRole();

	/**
	 * @return The roles related to this error, other than the origin role. e.g. If the error was detected in Spark and involved elasticsearch somehow, NodeRole.ELASTICSEARCH should be present in this property.
	 */
	public List<NodeRole> getRelatedRoles();

	/**
	 * @return The tenant related to this error. May be the system tenant if not related to a specific tenant.
	 */
	public String getTenantId();

	/**
	 * The user related to this error if any. May be system user if not related to an actual person.
	 */
	public User getUser();

	/**
	 * @return A basic error message string describing the error
	 */
	public String getMessage();

	/**
	 * @return The hostname on which the error originated
	 */
	public String getOriginHost();

	@JsonProperty("dg_utype")
	default public String getUnityType() { return PlatformError.UNITY_TYPE; }

}
