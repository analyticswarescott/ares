package com.aw.common.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.aw.common.util.JSONUtils;

public class TenantTest {

	private static String TENANT = "{\n" +
			"  \"settings\" : {\n" +
			"    \"es_replication\" : 12\n" +
			"  },\n" +
			"  \"tid\" : \"2\",\n" +
			"  \"env_overrides\" : { },\n" +
			"  \"mwa\" : \"http://localhost:8080/rest/1.0/dgrest\"\n" +
			"}";


	@Test
	public void test() throws Exception {

		Tenant t = JSONUtils.objectFromString(TENANT, Tenant.class);
		t.getSettings().put(TenantSetting.ES_REPLICATION, 12);

		//System.out.println(t.toString());

		String actual = JSONUtils.objectToString(t,false);
		String expected = TENANT;

		//convert back and assert that it is correct
		t = JSONUtils.objectFromString(actual, Tenant.class);

		assertTrue(JSONUtils.jsonEquals(actual, expected));
		assertTrue(t.getSettings().containsKey(TenantSetting.ES_REPLICATION));
		assertEquals(12, t.getSettings().getSettingInt(TenantSetting.ES_REPLICATION));

	}

}
