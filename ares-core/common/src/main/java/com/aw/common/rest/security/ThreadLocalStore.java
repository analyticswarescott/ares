package com.aw.common.rest.security;


public class ThreadLocalStore {

	public static final ThreadLocal<PlatformSecurityContext> userThreadLocal = new ThreadLocal<>();

	public static void set(PlatformSecurityContext context) {
		userThreadLocal.set(context);
	}

	public static void unset() {
		userThreadLocal.remove();
	}

	public static PlatformSecurityContext get() {
		return userThreadLocal.get();
	}
}
