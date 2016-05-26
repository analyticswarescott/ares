package com.aw.action;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;

import com.aw.action.exceptions.ActionCopyException;
import com.aw.action.exceptions.ActionPreparationException;
import com.aw.common.AbstractTaggable;
import com.aw.common.auth.User;
import com.aw.util.Statics;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Common base class providing commonly needed implementations for all actions
 *
 *
 *
 */
public abstract class AbstractAction extends AbstractTaggable implements Action {

	public AbstractAction() {
		setGuid(UUID.randomUUID());
		setTimestamp(Instant.now());
		setComment("(not set)");
	}

	/**
	 * Replace variables in the string - exception if the value is null, which uses errorMsg as the exception message
	 *
	 * @param string
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	protected String replaceVariablesNotNull(String string, ActionContext ctx, String errorMsg) throws ActionPreparationException {

		//error if there is a null
		if (string == null) {
			throw new ActionPreparationException(errorMsg);
		}

		return replaceVariables(string, ctx);

	}

	/**
	 * Replace action variables in a string
	 *
	 * @param string The raw string with variables
	 * @param ctx The action context used to resolve variables
	 * @return The final string with variables resolved. Any variables that could not be resolved will be left in-place
	 * @throws Exception If anything goes wrong
	 */
	protected String replaceVariables(String string, ActionContext ctx) throws ActionPreparationException {

		//if null, return null here, there's nothing to do
		if (string == null) {
			return null;
		}

		Matcher matcher = Statics.VARIABLE_PATTERN.matcher(string);

		//the return string
		StringBuilder ret = null;
		int previousEnd = 0;

		//replace all
		while (matcher.find()) {

			//lazy creation - don't burn memory if we don't have to
			if (ret == null) {
				ret = new StringBuilder();
			}

			//add up to this point
			ret.append(string.substring(previousEnd, matcher.start()));

			replaceVariable(ret, string, matcher, ctx);

			//include from this point next time
			previousEnd = matcher.end();

		}

		//if no matches, return the original string
		if (ret == null) {
			return string;
		}

		else {

			//add the final part
			ret.append(string.substring(previousEnd));

			//return the result
			return ret.toString();

		}

	}

	private void replaceVariable(StringBuilder result, String source, Matcher match, ActionContext ctx) throws ActionPreparationException {

		String variable = match.group(1);

		try {

			String value = ctx.getVariableString(variable);
			if (value == null) {

				//if variable resolution fails, throw an exception
				throw new ActionPreparationException("could not resolve variable: " + variable);

			}

			else {

				//else we have a value, add it
				result.append(value);

			}

		} catch (Exception e) {
			throw new ActionPreparationException("error during variable replacement, variable: " + variable, e);
		}

	}

	@Override
	public void prepare(ActionContext ctx) throws ActionPreparationException {

		//just set the comment, the other values will be set by the framework and default constructor
		comment = replaceVariables(comment, ctx);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T copy() throws ActionCopyException {

		try {

			AbstractAction ret = getClass().newInstance();
			ret.comment = comment;

			return (T)ret;

		} catch (Exception e) {
			throw new ActionCopyException("could not copy action", e);
		}

	}

	@JsonProperty("dg_guid")
	public UUID getGuid() { return guid; }
	public void setGuid(UUID guid) { this.guid = guid; }
	private UUID guid;

	@JsonProperty("dg_user")
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	private User user;

	@JsonProperty("a_comment")
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	private String comment;

	@JsonProperty("dg_time")
	public Instant getTimestamp() { return timestamp; }
	public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
	private Instant timestamp;

}
