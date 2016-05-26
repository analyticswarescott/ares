package com.aw.document;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of Document attributes
 */
public enum DocumentAttrib {

    //TODO: should deprecate this and require all attrib interaction via DocumentEnvelope

    TENANT_ID,
    ID,
    TYPE,
    NAME,
    AUTHOR,
    VERSION,
    IS_CURRENT,
    VERSION_DATE,
    VERSION_AUTHOR,
    DISPLAY_NAME,
    BODY,
    DESCRIPTION,
    DELETED,
    GROUPING,
    DEPENDENCIES_NAME,
    DEPENDENCIES_GUID;


    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }

}
