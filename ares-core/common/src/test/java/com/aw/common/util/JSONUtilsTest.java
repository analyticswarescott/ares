package com.aw.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.document.Document;

public class JSONUtilsTest {

	/**
{
	"key1" : "value1",
	"key2" : 1,
	"key3" : "value1",
	"key4" : [
		"value1",
		"value6"
	],
	"key5" : { "key6" : { "key7" : 1 } }
}
	 */
	private static final String MERGE_JSON_1 =
			"{\n" +
			"	\"key1\" : \"value1\",\n" +
			"	\"key2\" : 1,\n" +
			"	\"key3\" : \"value1\",\n" +
			"	\"key4\" : [\n" +
			"		\"value1\",\n" +
			"		\"value6\"\n" +
			"	],\n" +
			"	\"key5\" : { \"key6\" : { \"key7\" : 1 } }\n" +
			"}";

	/**
{
	"key2" : 2,
	"key4" : [
		"value2",
	],
	"key5" : { "key5" : { "key8" : 1 } }
}
	 */
	private static final String MERGE_JSON_2 =
			"{\n" +
			"	\"key2\" : 2,\n" +
			"	\"key4\" : [\n" +
			"		\"value2\",\n" +
			"	],\n" +
			"	\"key5\" : { \"key5\" : { \"key8\" : 1 } }\n" +
			"}";

	/**
{
	"key1" : "value3",
	"key2.1" : 1,
	"key3" : "value1",
	"key4" : [
		"value1",
		"value3"
	],
	"key5" : { "key6" : { "key7" : 3 } }
}
	 */
	private static final String MERGE_JSON_3 =
			"{\n" +
			"	\"key1\" : \"value3\",\n" +
			"	\"key2.1\" : 1,\n" +
			"	\"key3\" : \"value1\",\n" +
			"	\"key4\" : [\n" +
			"		\"value1\",\n" +
			"		\"value3\"\n" +
			"	],\n" +
			"	\"key5\" : { \"key6\" : { \"key7\" : 3 } }\n" +
			"}";

	/**
{
	"key1" : "value3",
	"key2" : 2,
	"key2.1" : 1,
	"key3" : "value1",
	"key4" : [
		"value1",
		"value6",
		"value2",
		"value3"
	],
	"key5" : { "key6" : { "key7" : 3, "key8" : 1 } }
}
	 */
	private static final String MERGE_RESULT =
			"{\n" +
			"	\"key1\" : \"value3\",\n" +
			"	\"key2\" : 2,\n" +
			"	\"key3\" : \"value1\",\n" +
			"	\"key4\" : [\n" +
			"		\"value3\",\n" +
			"		\"value2\",\n" +
			"		\"value1\",\n" +
			"		\"value6\"\n" +
			"	],\n" +
			"	\"key5\" : { \"key6\" : { \"key7\" : 3 },\n" +
			"                \"key5\" : { \"key8\" : 1 } },\n" +
			"	\"key2.1\" : 1\n" +
			"}";

	@Test
	public void merge() throws Exception {

		Document d1 = new Document() { public String getBodyAsString() { return MERGE_JSON_1; } };
		Document d2 = new Document() { public String getBodyAsString() { return MERGE_JSON_2; } };
		Document d3 = new Document() { public String getBodyAsString() { return MERGE_JSON_3; } };

		JSONObject result = JSONUtils.merge(Arrays.asList(new Document[] { d1, d2, d3 }));

		assertEquals("json merge failed", new JSONObject(MERGE_RESULT).toString(), result.toString());

	}

	@Test
	public void replaceSingleTokenInString() throws Exception {

        String replaced = JSONUtils.replaceSingleTokenInString("myString$TOKENmyString", "$TOKEN", "_value_");

        assertEquals("myString_value_myString", replaced);

	}

	/**
{
	"key1" : "value3",
	"key2" : 2,
	"key2.1" : 1,
	"key3" : "value1",
	"key4" : [
		"value1",
		"value6",
		"value2",
		"value3"
	],
	"key5" : { "key6" : { "key7" : 3, "key8" : 1 } }
}
{
	"key6" : "value6"
}
	 */
	@Test
	public void overlay() throws Exception {

//        JSONObject hudJSON = JSONUtils.getLeafValue(workspaceBody, "elements[0].body");
		JSONObject source = new JSONObject("{\n" +
				"	\"key1\" : \"value3\",\n" +
				"	\"key2\" : 2,\n" +
				"	\"key2.1\" : 1,\n" +
				"	\"key3\" : \"value1\",\n" +
				"	\"key4\" : [\n" +
				"		\"value1\",\n" +
				"		\"value6\",\n" +
				"		\"value2\",\n" +
				"		\"value3\"\n" +
				"	],\n" +
				"	\"key5\" : { \"key6\" : { \"key7\" : 3, \"key8\" : 1 } }\n" +
				"}");
		JSONObject target = new JSONObject("{\n" +
				"	\"key6\" : \"value6\"\n" +
				"}\n" +
				"");

		JSONUtils.overlay(source, target);

		assertTrue(target.has("key1"));
		assertTrue(target.has("key4"));
		assertTrue(target.has("key5"));
		assertEquals(4, target.getJSONArray("key4").length());

	}

	/**
{
	"key2" : "value3",
	"key1" : 2,
	"key3" : "value1",
}
	 */
	@Test
	public void toStringSorted() throws Exception {

		JSONObject obj = new JSONObject("{\n" +
				"	\"key2\" : \"value3\",\n" +
				"	\"key1\" : 2,\n" +
				"	\"key3\" : \"value1\",\n" +
				"}");
		String expected = "{\"key1\":2,\"key2\":\"value3\",\"key3\":\"value1\"}";

		String sorted = JSONUtils.toStringSorted(obj, false);
		assertEquals(expected, sorted);
	}

	@Test
	public void jsonToMap() throws Exception {

		JSONObject obj = new JSONObject("{\n" +
				"	\"key2\" : \"value3\",\n" +
				"	\"key1\" : 2,\n" +
				"	\"key3\" : \"value1\",\n" +
				"}");

		Map<String, Object> map = JSONUtils.jsonToMap(obj);

		assertEquals(3, map.size());
		assertEquals(2, map.get("key1"));
		assertEquals("value3", map.get("key2"));
		assertEquals("value1", map.get("key3"));

	}

	@Test
	public void jsonStringToMap() throws Exception {

		String json = "{\n" +
				"	\"key2\" : \"value3\",\n" +
				"	\"key1\" : 2,\n" +
				"	\"key3\" : \"value1\",\n" +
				"}";

		Map<String, Object> map = JSONUtils.jsonToMap(json);

		assertEquals(3, map.size());
		assertEquals(2, map.get("key1"));
		assertEquals("value3", map.get("key2"));
		assertEquals("value1", map.get("key3"));

	}

	/**
{
	"key1" : "value3",
	"key2" : 2,
	"key2.1" : 1,
	"key3" : "value1",
	"key4" : [
		"value1",
		"value6",
		"value2",
		"value3"
	],
	"key5" : { "key6" : { "key7" : 3, "key8" : 1 } }
}
	 */
	@Test
	public void getLeafValueNonNull() throws Exception {

		JSONObject obj = new JSONObject("{\n" +
				"	\"key1\" : \"value3\",\n" +
				"	\"key2\" : 2,\n" +
				"	\"key2.1\" : 1,\n" +
				"	\"key3\" : \"value1\",\n" +
				"	\"key4\" : [\n" +
				"		\"value1\",\n" +
				"		\"value6\",\n" +
				"		\"value2\",\n" +
				"		\"value3\"\n" +
				"	],\n" +
				"	\"key5\" : { \"key6\" : { \"key7\" : 3, \"key8\" : 1 } }\n" +
				"}");

		Number value = JSONUtils.getLeafValue(obj,  "key5.key6.key7");
		assertEquals(3, value);

	}

	/**
{
	"key1" : "value3",
	"key2" : 2,
	"key2.1" : 1,
	"key3" : "value1",
	"key4" : [
		{ "key1" : "value1" },
		{ "key1" : "value6" }
	],
	"key5" : { "key6" : { "key7" : 3, "key8" : 1 } }
}
	 */
	@Test
	public void getLeafValueArray() throws Exception {

		JSONObject obj = new JSONObject("{\n" +
				"	\"key1\" : \"value3\",\n" +
				"	\"key2\" : 2,\n" +
				"	\"key2.1\" : 1,\n" +
				"	\"key3\" : \"value1\",\n" +
				"	\"key4\" : [\n" +
				"		{ \"key1\" : \"value1\" },\n" +
				"		{ \"key1\" : \"value6\" }\n" +
				"	],\n" +
				"	\"key5\" : { \"key6\" : { \"key7\" : 3, \"key8\" : 1 } }\n" +
				"}");

		String value = JSONUtils.getLeafValue(obj,  "key4[1].key1");
		assertEquals("value6", value);

	}

	static class TestToString {

		public Date getDate() { return m_date; }
		public void setDate(Date date) { m_date = date; }
		private Date m_date = new Date(100);

		public int getInteger() { return m_integer; }
		public void setInteger(int integer) { m_integer = integer; }
		private int m_integer = 10;

		public String getString() { return m_string; }
		public void setString(String string) { m_string = string; }
		private String m_string = "string";

	}

	static class TestToString2 implements JSONable {

		public Date getDate() { return m_date; }
		public void setDate(Date date) { m_date = date; }
		private Date m_date = new Date(100);

		public int getInteger() { return m_integer; }
		public void setInteger(int integer) { m_integer = integer; }
		private int m_integer = 10;

		public String getString() { return m_string; }
		public void setString(String string) { m_string = string; }
		private String m_string = "string";

		@Override
		public String toJSON() {
			try {
			return new JSONObject("{ \"blah\" : \"value\" }").toString(4);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Test
	public void objectToString() throws Exception {

		String expected = "{\"date\":100,\"integer\":10,\"string\":\"string\"}";

		TestToString obj = new TestToString();

		String str = JSONUtils.objectToString(obj);
		str = JSONUtils.toStringSorted(new JSONObject(str), false);

		assertEquals(expected, str);

	}

	@Test
	public void objectToString_custom() throws Exception {

		String expected = "{\n" +
				"  \"blah\" : \"value\"\n" +
				"}";

		TestToString2 obj = new TestToString2();

		String str = JSONUtils.objectToString(obj);
		str = JSONUtils.toStringSorted(new JSONObject(str), false);

		assertEquals(new JSONObject(expected).toString(), new JSONObject(str).toString());

	}

}

