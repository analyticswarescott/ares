package com.aw.common;

import org.apache.commons.io.IOUtils;

import com.aw.platform.DefaultPlatform;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformBuilder;
import com.aw.platform.PlatformNode.RoleSetting;

/**
 * Build a platform using localhost and default ports
 *
 *
 *
 */
public class TestPlatform extends DefaultPlatform {

	/**
	 * loads default test platform.json in src/test/resources
	 */
	public TestPlatform() {
		try {
			String data = IOUtils.toString(getClass().getResourceAsStream("platform.json"));
			initialize(data, null);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}

	public static Platform build(RoleSetting setting, Object value) {

		return builder(setting.getRole()).withSetting(setting, value).build();

	}

	public static Platform build(NodeRole... roles) {

		return builder(roles).build();

	}

	public static Platform build() {

		return build(NodeRole.values());

	}

	private static PlatformBuilder builder(NodeRole... roles) {
		return new PlatformBuilder()
				.withNode("localhost", roles);
	}

}
