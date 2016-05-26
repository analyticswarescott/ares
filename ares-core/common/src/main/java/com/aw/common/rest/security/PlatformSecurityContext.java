package com.aw.common.rest.security;

import java.util.Locale;

/**
 * Describes the security needs of the platform.
 */
public interface PlatformSecurityContext {

	String getTenantID();

	Locale getLocale();

	AuthenticatedUser getUser();

	boolean isUserInRole(String role);
}
