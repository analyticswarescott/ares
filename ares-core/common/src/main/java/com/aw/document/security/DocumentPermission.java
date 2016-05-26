package com.aw.document.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by scott on 20/10/15.
 */
public enum DocumentPermission {

    AUTHOR,
    ALL;

	//in the future we could allow a custom set of users or groups a particular document permission
    //CUSTOM;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static DocumentPermission forValue(String val) { return DocumentPermission.valueOf(val.toUpperCase()); }

}
