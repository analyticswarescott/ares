package com.aw.platform;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.InputStream;

import org.junit.Test;

import com.aw.common.util.JSONUtils;

public class PlatformUpgradeTest {

	@Test
	public void toFromJson() throws Exception {

		PlatformUpgrade upgrade = new PlatformUpgrade(mock(PlatformMgr.class), "1.0", mock(InputStream.class));

		String json = JSONUtils.objectToString(upgrade);

		PlatformUpgrade upgrade2 = JSONUtils.objectFromString(json, PlatformUpgrade.class);

		assertEquals(upgrade.getVersionId(), upgrade2.getVersionId());
		assertEquals(upgrade.getFilename(), upgrade2.getFilename());

	}

}
