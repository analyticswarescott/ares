package com.aw.platform;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.Tag;
import com.aw.common.auth.DefaultUser;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.platform.exceptions.PlatformInitializationException;

public class DefaultPlatformErrorTest {

	@Test
	public void testConstruct() throws Exception {

		Exception e = new PlatformInitializationException("this is a test");

		Instant instant = Instant.now();
		DefaultPlatformError error = new DefaultPlatformError(instant, new Tenant("test_tenant"), new DefaultUser("test_user"), NodeRole.SPARK_WORKER, Arrays.asList(NodeRole.ELASTICSEARCH, NodeRole.KAFKA), e, Tag.valueOf("test_tag1"), Tag.valueOf("test_tag2"));

		//convert to json
		String jsonString = JSONUtils.objectToString(error);
		JSONObject result = new JSONObject(jsonString);

		//check properties
		assertTrue(result.has("err_stack"));
		assertTrue(result.getString("err_stack").contains("at "+DefaultPlatformErrorTest.class.getName()+".testConstruct(DefaultPlatformErrorTest.java:"));

		assertTrue(result.has("err_type"));
		assertEquals(PlatformInitializationException.class.getSimpleName(), result.getString("err_type"));

		assertTrue(result.has("err_role"));
		assertEquals(NodeRole.SPARK_WORKER.toString(), result.getString("err_role"));

		assertTrue(result.has("err_related"));
		assertEquals(2, result.getJSONArray("err_related").length());
		assertEquals(NodeRole.ELASTICSEARCH.toString(), result.getJSONArray("err_related").get(0));
		assertEquals(NodeRole.KAFKA.toString(), result.getJSONArray("err_related").get(1));

		assertTrue(result.has("dg_user"));
		assertEquals("test_user", result.getString("dg_user"));

		assertTrue(result.has("dg_time"));
		assertEquals(instant, error.getTimestamp());

		assertTrue(result.has("dg_guid"));
		assertNotNull(result.getString("dg_guid"));

		assertTrue(result.has("err_msg"));
		assertEquals("this is a test", result.getString("err_msg"));

		assertTrue(result.has("dg_tags"));
		assertEquals(2, result.getJSONArray("dg_tags").length());
		assertEquals("test_tag1", result.getJSONArray("dg_tags").get(0));
		assertEquals("test_tag2", result.getJSONArray("dg_tags").get(1));

		assertTrue(result.has("dg_utype"));
		assertTrue(result.getString("dg_utype").equals(PlatformError.UNITY_TYPE));

		PlatformError error2 = JSONUtils.objectFromString(jsonString, PlatformError.class);
		assertEquals(error.getTimestamp(), error2.getTimestamp());
		assertEquals(error.getGuid(), error2.getGuid());
		assertEquals(error.getTags(), error2.getTags());
		assertEquals(error.getRelatedRoles(), error2.getRelatedRoles());

	}

	@Test
	public void testSet() throws Exception {

		Exception e = new PlatformInitializationException("this is a test");

		Instant instant = Instant.now();
		DefaultPlatformError error = new DefaultPlatformError();

		UUID guid = UUID.randomUUID();

		error.addTag(Tag.valueOf("test_tag1"));
		error.addTag(Tag.valueOf("test_tag2"));
		error.setGuid(guid);
		error.setMessage(e.getMessage());
		error.setOriginRole(NodeRole.SPARK_WORKER);
		error.setRelatedRoles(Arrays.asList(NodeRole.ELASTICSEARCH, NodeRole.KAFKA));
		error.setStackTrace(ExceptionUtils.getFullStackTrace(e));
		error.setType(e.getClass().getSimpleName());
		error.setTenantId("test_tenant");
		error.setTimestamp(instant);
		error.setUser(new DefaultUser("test_user"));

		//convert to json
		String jsonString = JSONUtils.objectToString(error);
		JSONObject result = new JSONObject(jsonString);

		//check properties
		assertTrue(result.has("err_stack"));
		assertTrue(result.getString("err_stack").contains("at "+DefaultPlatformErrorTest.class.getName()+".testSet(DefaultPlatformErrorTest.java:"));

		assertTrue(result.has("err_type"));
		assertEquals(PlatformInitializationException.class.getSimpleName(), result.getString("err_type"));

		assertTrue(result.has("err_role"));
		assertEquals(NodeRole.SPARK_WORKER.toString(), result.getString("err_role"));

		assertTrue(result.has("err_related"));
		assertEquals(2, result.getJSONArray("err_related").length());
		assertEquals(NodeRole.ELASTICSEARCH.toString(), result.getJSONArray("err_related").get(0));
		assertEquals(NodeRole.KAFKA.toString(), result.getJSONArray("err_related").get(1));

		assertTrue(result.has("dg_user"));
		assertEquals("test_user", result.getString("dg_user"));

		assertTrue(result.has("dg_time"));
		assertEquals(instant, error.getTimestamp());

		assertTrue(result.has("err_msg"));
		assertEquals("this is a test", result.getString("err_msg"));

		assertTrue(result.has("dg_guid"));
		assertNotNull(result.getString("dg_guid"));

		assertTrue(result.has("dg_tags"));
		assertEquals(2, result.getJSONArray("dg_tags").length());
		assertEquals("test_tag1", result.getJSONArray("dg_tags").get(0));
		assertEquals("test_tag2", result.getJSONArray("dg_tags").get(1));

		assertTrue(result.has("dg_utype"));
		assertTrue(result.getString("dg_utype").equals(PlatformError.UNITY_TYPE));

		assertEquals("test_tenant", result.getString("dg_tenant"));

		PlatformError error2 = JSONUtils.objectFromString(jsonString, PlatformError.class);
		assertEquals(error.getTimestamp(), error2.getTimestamp());
		assertEquals(error.getGuid(), error2.getGuid());
		assertEquals(error.getTags(), error2.getTags());
		assertEquals(error.getRelatedRoles(), error2.getRelatedRoles());

	}


}
