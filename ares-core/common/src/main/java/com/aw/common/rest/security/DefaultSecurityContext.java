package com.aw.common.rest.security;

import java.util.Locale;

/**
 * Default implementation of the {@link PlatformSecurityContext} interface.
 */
public class DefaultSecurityContext implements PlatformSecurityContext {

	private final AuthenticatedUser authenticatedUser;
	private final Locale locale;

	public DefaultSecurityContext(AuthenticatedUser authenticatedUser) {
		if (authenticatedUser == null) {
			throw new IllegalArgumentException("Authenticated User must not be null!");
		}

		this.authenticatedUser = authenticatedUser;
		this.locale = Locale.forLanguageTag(authenticatedUser.getLocale());
	}

	@Override
	public String getTenantID() {
		return authenticatedUser.getTenant();
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public AuthenticatedUser getUser() {
		return authenticatedUser;
	}

	@Override
	public boolean isUserInRole(String role) {
		return authenticatedUser.getRoles().contains(role);
	}
}
