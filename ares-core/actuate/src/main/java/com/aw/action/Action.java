package com.aw.action;

import java.time.Instant;
import java.util.UUID;

import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.auth.User;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An action taken in the system
 *
 *
 *
 */
public interface Action extends Cloneable {

	/**
	 * field for foreign key reference to an action
	 */
	public static final String ACTION_GUID = "a_guid";

	//action type
	public static final String TYPE = "a_type";

	/**
	 * @return The type name, forcing the json form of actions to include type name
	 */
	@JsonProperty(TYPE)
	public ActionType getType();

	/**
	 * @return A guid for this action
	 */
	@JsonProperty("dg_guid")
	public UUID getGuid();

	/**
	 * @return The time this action occurred
	 */
	@JsonProperty("dg_time")
	public Instant getTimestamp();

	/**
	 * @return A copy of this action and its fields
	 */
	public <T> T copy() throws ActionCopyException;

	/**
	 * Prepare an instance of this action for execution, using the context for variable expansion.
	 *
	 * @param ctx The action context that is used for the execution of this action
	 */
	public void prepare(ActionContext ctx) throws ActionPreparationException;

	/**
	 * Execute an action
	 * @param ctx TODO
	 * @throws Exception If anything goes wrong while executing the action
	 */
	public void execute(ActionContext ctx) throws ActionExecutionException;

	/**
	 * @return The user taking the action, or null if not set
	 */
	@JsonProperty("dg_user")
	public User getUser();

	/**
	 * Set by the framework, the user taking the action
	 *
	 * @param user
	 */
	public void setUser(User user);

	/**
	 * whether the action itself should be persisted as unity data
	 *
	 * @return whether this action is persistable
	 */
	public boolean isPersistable();

}
