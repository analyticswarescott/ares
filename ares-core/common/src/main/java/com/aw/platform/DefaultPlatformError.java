package com.aw.platform;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.aw.common.AbstractTaggable;
import com.aw.common.Tag;
import com.aw.common.auth.User;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default error implementation
 *
 *
 *
 */
public class DefaultPlatformError extends AbstractTaggable implements PlatformError {

	public DefaultPlatformError() {
		super();
		this.guid = UUID.randomUUID();
	}

	/**
	 * Create a platform error without an exception, just a message
	 *
	 * @param tenant
	 * @param user
	 * @param originRole
	 * @param relatedRoles
	 * @param msg
	 * @param tags
	 */
	public DefaultPlatformError(Tenant tenant, User user, NodeRole originRole, List<NodeRole> relatedRoles, String msg, Tag... tags) {
		this(Instant.now(), tenant, user, originRole, relatedRoles, null, null, msg, tags);
	}

	/**
	 * Create a platform error that occurred right now
	 *
	 * @param tenant
	 * @param user
	 * @param originRole
	 * @param relatedRoles
	 * @param throwable
	 * @param tags
	 */
	public DefaultPlatformError(Tenant tenant, User user, NodeRole originRole, List<NodeRole> relatedRoles, 	Throwable throwable, Tag... tags) {
		this(Instant.now(), tenant, user, originRole, relatedRoles, throwable, tags);
	}

	/**
	 * Create a platform error given a throwable
	 *
	 * @param timestamp
	 * @param tenant
	 * @param user
	 * @param originRole
	 * @param throwable
	 * @param relatedRoles
	 */
	public DefaultPlatformError(Instant timestamp, Tenant tenant, User user, NodeRole originRole, List<NodeRole> relatedRoles, 	Throwable throwable, Tag... tags) {
		this(timestamp,
			 tenant,
			 user,
			 originRole,
			 relatedRoles,
			 ExceptionUtils.getFullStackTrace(throwable),
			 throwable.getClass().getSimpleName(),
			 throwable.getMessage(),
			 tags); //empty list if no related roles provided);
	}

	/**
	 * Create a platform error with all properties set
	 *
	 * @param timestamp
	 * @param tenant
	 * @param user
	 * @param originRole
	 * @param relatedRoles
	 * @param stackTrace
	 * @param type
	 */
	public DefaultPlatformError(Instant timestamp, Tenant tenant, User user, NodeRole originRole, List<NodeRole> relatedRoles, String stackTrace, String type, String msg, Tag... tags) {
		this();
		this.originRole = originRole;
		this.relatedRoles = relatedRoles;
		this.stackTrace = stackTrace;
		this.tenantId = tenant.getTenantID();
		this.timestamp = timestamp;
		this.type = type;
		this.user = user;
		this.message = msg;
		this.originHost = EnvironmentSettings.getHost();
		addAll(tags);
	}

	@JsonProperty("dg_guid")
	public UUID getGuid() { return guid; }
	public void setGuid(UUID guid) { this.guid = guid; }
	private UUID guid;

	@JsonProperty("err_role")
	public NodeRole getOriginRole() { return originRole; }
	public void setOriginRole(NodeRole originRole) { this.originRole = originRole; }
	private NodeRole originRole;

	@JsonProperty("err_related")
	public List<NodeRole> getRelatedRoles() { return relatedRoles; }
	public void setRelatedRoles(List<NodeRole> relatedRoles) { this.relatedRoles = relatedRoles; }
	private List<NodeRole> relatedRoles;

	@JsonProperty("err_stack")
	public String getStackTrace() { return stackTrace; }
	public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
	private String stackTrace;

	@JsonProperty("dg_tenant")
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	private String tenantId;

	@JsonProperty("dg_time")
	public Instant getTimestamp() { return timestamp; }
	public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
	private Instant timestamp;

	@JsonProperty("err_type")
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	private String type;

	@JsonProperty("dg_user")
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	private User user;

	@JsonProperty("err_msg")
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	private String message;

	@JsonProperty("err_host")
	public String getOriginHost() { return this.originHost;  }
	public void setOriginHost(String originHost) { this.originHost = originHost; }
	private String originHost;

}
