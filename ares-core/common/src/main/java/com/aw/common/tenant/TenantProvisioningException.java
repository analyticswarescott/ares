package com.aw.common.tenant;

/**
 * General processing exception
 *
 *
 *
 */
public class TenantProvisioningException extends Exception {

	private static final long serialVersionUID = 1L;

	public TenantProvisioningException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TenantProvisioningException(String message, Throwable cause) {
		super(message, cause);
	}

	public TenantProvisioningException(String message) {
		super(message);
	}

}
