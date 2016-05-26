package com.aw.common.rest.security;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;



public class AuthenticatedUser implements Principal, TenantAware {

	private String id;
	private String name;
	private String tenant;
	private String locale;
	private List<String> roles = new ArrayList<String>();
	private List<String> preferences = new ArrayList<String>();
	private List<String> obfuscationRules = new ArrayList<String>();

	private boolean isImpersonated = false;

	@Override
	public String toString() {
		return this.tenant + ":" + this.id + ":" + this.name;
	}

	public AuthenticatedUser(String id, String name, String tenant, String locale, List<String> roles,
							 List<String> preferences, List<String> obfuscationRules) {
		super();
		this.id = id;
		this.name = name;
		this.tenant = tenant;
		this.roles = roles;
		this.locale = locale;
		this.preferences = preferences;
		this.obfuscationRules = obfuscationRules;
	}

	public AuthenticatedUser(){

	}

	public AuthenticatedUser(String tenant, String name, String id, String locale) {
		this.tenant = tenant;
		this.name = name;
		this.id = id;
		this.locale = locale;
	}

	public void setImpersonated(boolean value) throws Exception {

		if (!isImpersonated && !getTenantID().equals("0")) {
			throw new Exception("cannot set from non-system tenant");

		}

		isImpersonated = value;
	}

    public boolean isImpersonated() {
		return isImpersonated;
	}

	/**
	 * User name, unique, and used for login
	 * @return
	 */

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * Roles used for row level security, such as the departments this user can view.
	 * @return
	 */
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	/**
	 * User preferences based upon system interactions.
	 * @return
	 */
	public List<String> getPreferences() {
		return preferences;
	}
	public void setPreferences(List<String> preferences) {
		this.preferences = preferences;
	}


	public List<String> getObfuscationRules() {
		return obfuscationRules;
	}
	public void setObfuscationRules(List<String> obfuscationRules) {
		this.obfuscationRules = obfuscationRules;
	}

}
