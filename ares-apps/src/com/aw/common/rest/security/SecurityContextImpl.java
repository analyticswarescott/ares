package com.aw.common.rest.security;

import javax.ws.rs.core.SecurityContext;

//for reference
//http://stackoverflow.com/questions/17486746/glassfish-rolesallowed-with-custom-securitycontext
//http://porterhead.blogspot.fr/2013/01/writing-rest-services-in-java-part-6.html

public class SecurityContextImpl implements SecurityContext {

    private final AuthenticatedUser _user;


    public String getTenantID() {
    	return _user.getTenant();
    }

    public String getLocale() {
    	return _user.getLocale();
    }

    public SecurityContextImpl(AuthenticatedUser user) {
        this._user = user;
    }

    public AuthenticatedUser getUserPrincipal() {
    	return _user;
    }

    public boolean isUserInRole(String role) {

    	return _user.getRoles().contains(role);
    }

    public boolean isSecure() {
        return false;
    }

    public String getAuthenticationScheme() {
        return BASIC_AUTH;
    }

}
