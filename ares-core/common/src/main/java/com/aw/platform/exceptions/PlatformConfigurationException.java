package com.aw.platform.exceptions;

public class PlatformConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public PlatformConfigurationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public PlatformConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PlatformConfigurationException(String arg0) {
		super(arg0);
	}


}
