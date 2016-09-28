package com.aw.common.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aw.common.task.TaskSchedule.Type;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.SetTimeSource;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;

public class TaskDefTest {

	private static DocumentHandler docs;

	@BeforeClass
	public static void beforeClass() throws Exception {
		docs = new TestDocumentHandler();
	}

	@Test
	public void test_initialize() throws Exception {

		TaskDef def = new TaskDef();

		def.setCpuResourceWeighting(.5);
		def.setMemoryResourceWeighting(.5);
		def.setName("test_name");
		def.setSchedule(new TaskSchedule(Type.RECURRING, "0 0 1 1/1 * ? *"));
		def.setTenant(Tenant.forId("1"));
		def.setType(docs.getDocument(DocumentType.TASK_TYPE, "platform_status").getBodyAsObject(TaskType.class));

		JSONObject cfg = new JSONObject();
		cfg.put("test_key", "test_value");
		def.setConfig(cfg);

		String json = def.toJSON();

		TaskDef def2 = new TaskDef();
		def2.initialize(json, new TestDocumentHandler());

		assertEquals(def.getCpuResourceWeighting(), def2.getCpuResourceWeighting(), 0.0);
		assertEquals(def.getMemoryResourceWeighting(), def2.getMemoryResourceWeighting(), 0.0);
		assertEquals(def.getName(), def2.getName());
		assertEquals(def.getSchedule().getType(), def2.getSchedule().getType());
		assertEquals("test_value", def.getConfigScalar("test_key", String.class));
		assertNotNull(def2.getSchedule());
		assertEquals(Type.RECURRING, def2.getSchedule().getType());
		assertNotNull(def2.getSchedule().getRecurrencePattern());
		assertEquals("0 0 1 1/1 * ? *", def2.getSchedule().getRecurrencePattern().toString());


		System.out.println(JSONUtils.objectToString(def));

	}

	@Test
	public void test_getConfigVector() throws Exception {

		TestData testData1 = new TestData("data1");
		TestData testData2 = new TestData("data2");

		TaskDef def = new TaskDef();
		def.setCpuResourceWeighting(.5);
		def.setMemoryResourceWeighting(.5);
		def.setName("test_name");
		def.setSchedule(TaskSchedule.PERPETUAL);
		def.setTenant(Tenant.forId("1"));
		def.setType(new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));

		JSONObject cfg = new JSONObject();
		JSONArray array = new JSONArray(JSONUtils.objectToString(Arrays.asList(testData1, testData2)));
		cfg.put("array", array);
		def.setConfig(cfg);

		String json = def.toJSON();

		TaskDef def2 = new TaskDef();
		def2.initialize(json, new TestDocumentHandler());

		assertEquals(def.getCpuResourceWeighting(), def2.getCpuResourceWeighting(), 0.0);
		assertEquals(def.getMemoryResourceWeighting(), def2.getMemoryResourceWeighting(), 0.0);
		assertEquals(def.getName(), def2.getName());
		assertEquals(def.getSchedule().getType(), def2.getSchedule().getType());
		List<TestData> testData = def.getConfigVector("array", TestData.class);
		assertEquals(2, testData.size());
		assertEquals("data1", testData.get(0).getData());
		assertEquals("data2", testData.get(1).getData());





	}

	@Test(expected=IllegalArgumentException.class)
	public void test_nextExecutionTime_recurring() throws Exception {
		TimeSource setTime = new SetTimeSource(java.time.Instant.now());
		TaskDef def = new TaskDef();
		def.setSchedule(new TaskSchedule(Type.RECURRING));
		def.getNextExecutionTime(setTime);
	}

	@Test
	public void test_nextExecutionTime_perpetual() throws Exception {
		TimeSource setTime = new SetTimeSource(java.time.Instant.now());
		TaskDef def = new TaskDef();
		def.setSchedule(TaskSchedule.PERPETUAL);
		Instant next = def.getNextExecutionTime(setTime);
		assertEquals(Instant.MIN, next);
	}

	@Test
	public void test_newTask() throws Exception {
		TimeSource setTime = new SetTimeSource(java.time.Instant.now());
		TaskDef def = new TaskDef();
		def.setSchedule(TaskSchedule.PERPETUAL);
		def.setType(new TaskType(PlatformStatusPoller.TYPE, PlatformStatusPoller.class));
		Task task = def.newTask();
		assertTrue(task instanceof PlatformStatusPoller);
	}

	@Test
	public void test_equals() throws Exception {

		//tasks should be identified by their tenant and task name

		TaskDef def1 = new TaskDef();
		def1.setTenant(Tenant.forId("1"));
		def1.setName("name1");

		TaskDef def2 = new TaskDef();
		def2.setTenant(Tenant.forId("1"));
		def2.setName("name1");

		TaskDef def3 = new TaskDef();
		def3.setTenant(Tenant.forId("1"));
		def3.setName("name2");

		TaskDef def4 = new TaskDef();
		def4.setTenant(Tenant.forId("2"));
		def4.setName("name1");

		assertTrue(def1.equals(def2));
		assertEquals(def1.hashCode(), def2.hashCode());

		assertFalse(def1.equals(def3));
		assertFalse(def1.equals(def4));
		assertFalse(def3.equals(def4));


	}

	static class TestData {

		public TestData() {
		}

		public TestData(String data) {
			this.data = data;
		}

		public String getData() { return this.data; }
		public void setData(String data) { this.data = data; }
		private String data;

	}

}
