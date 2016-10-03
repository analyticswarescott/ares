package com.aw.document;

import com.aw.common.system.scope.ResourceScope;
import com.aw.common.system.scope.ScopedResource;
import com.aw.common.tenant.Tenant;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported document types. <br> <br>
 *     The addition of a defaultDocName via the constructor allows a user to spawn a version of this named document with their userID as the name (semantic ID) <br>
 *     A default document of this name must exist for the special 'aw' author for tenant 0 (default tenant)
 */
public enum DocumentType implements ScopedResource {

	PLATFORM(ResourceScope.SYSTEM),
    TENANT(ResourceScope.SYSTEM),

    //general application types
    USER_SETTINGS("default"),
	USER_ROLE,
    HUD,
    INVESTIGATION,
    QUERY,
    WORKSPACE,
    FILTER,
    DOCUMENT_GROUP,

    //detection framework types
    SIMPLE_RULE,

    //task definitions for the task cluster - scope is ALL since tenants can have their own tasks
    TASK_DEF(ResourceScope.ALL),

    //task types
    TASK_TYPE(ResourceScope.SYSTEM),

    //stream processing types
    STREAM_DRIVER(ResourceScope.SYSTEM),
    STREAM_TENANT,
    STREAM_GLOBAL(ResourceScope.SYSTEM),

    //elasticsearch types
    CONFIG_INDEX,

    //unity types
    UNITY_FIELD_REPO,
    UNITY_DATATYPE_REPO,
    UNITY_INSTANCE,
    UNITY_LOCALE,

    //other types
    TEST_TYPE,
	Z_NOT_USED,

	SMS_SEND_LIST,

	//Third party configuration file templates
	CONFIG_3P(ResourceScope.SYSTEM);



    DocumentType(ResourceScope scope) {
        m_scope = scope;
    }

	DocumentType(String defaultDocName) {
		_defaultDocName = defaultDocName;
	}

	DocumentType(ResourceScope scope, String defaultDocName) {
		m_scope = scope;
		_defaultDocName = defaultDocName;
	}

	DocumentType() {
		_defaultDocName = null;
	}

	public String getDefaultDocName() {
		return _defaultDocName;
	}

	private String _defaultDocName;


    @JsonCreator
    public static DocumentType forValue(String val) { return DocumentType.valueOf(val.toUpperCase()); }

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }


	public ResourceScope getScope() {
		return m_scope;
	}

	public boolean isInScope(Tenant tenant) {
		if (m_scope == ResourceScope.ALL) {
			return true;
		}
		if (m_scope == ResourceScope.SYSTEM && tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			return true;
		}
		if (m_scope == ResourceScope.TENANT && !tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			return true;
		}

		return false;
	}


	private ResourceScope m_scope = ResourceScope.ALL;

}
