package com.aw.platform;

/**
 * Provides information when platform changes
 */
public interface PlatformListener {

	/**
	 * Platform changed in some way
	 *
	 * @param platform
	 */
	public void onPlatformChange(Platform platform) throws Exception;

}
