package com.aw.common.spark;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import com.aw.common.messaging.Topic;
import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StreamDefTest {

	@Test
	public void test() throws Exception {

		StreamDef def = new StreamDef();
		def.setStreamHandlerClass("streamHandlerClass");
		def.getConfigData().put("a", "b");
		def.setProcessorId("processorId");
		ArrayList<Topic> st = new ArrayList<Topic>();
		st.add(Topic.ARCHIVED_FILES);
		def.setSourceTopic(st);
		def.setTargetDriver("targetDriver");
		def.setTenant(true);
		def.setSystem(true);

		ObjectMapper mapper = new ObjectMapper();
		String json = JSONUtils.objectToString(def);

		StreamDef def2 = JSONUtils.objectFromString(json, StreamDef.class);
		assertEquals(def.getDestTopic(), def2.getDestTopic());
		assertEquals(def.getSourceTopic().get(0), def2.getSourceTopic().get(0));

		/**
{
	"tenant": true,
	"source_topic": ["sourceTopic"],
	"target_driver": "targetDriver",
	"config_data": {
		"a": "b"
	},
	"processor_id": "processorId",
	"stream_handler_class": "streamHandlerClass"
}
		 */
		String expected = "{\n" +
				"	\"tenant\": true,\n" +
				"	\"system\": true,\n" +
				"	\"source_topic\": [\"archived_files\"],\n" +
				"	\"global\": false,\n" +
				"	\"target_driver\": \"targetDriver\",\n" +
				"	\"config_data\": {\n" +
				"		\"a\": \"b\"\n" +
				"	},\n" +
				"	\"processor_id\": \"processorId\",\n" +
				"	\"stream_handler_class\": \"streamHandlerClass\",\n" +
			"	\"optimal_events_per_task\": 0\n" +
				"}";

		JsonNode j1 = mapper.readTree(json);
		JsonNode j2 = mapper.readTree(expected);

		assertEquals("streamdef json not correct", j1, j2);

		//now make sure we load correctly from doc
		/**
{
    "target_driver" : "driver30sec",
    "processor_id": "load_machine_event_ES",
    "handler_class": "com.aw.compute.streams.processor.impl.GenericESProcessor",
    "source_topic": "machine_event",
    "offset_type": "smallest.always",
    "destination_topics": [],
    "processor_data": {
      "index_type": "machine_event",
      "index_name": "dlp",
      "id_attribute": "medid"
}
    		 */
		String exampleDef = "{\n" +
				"    \"target_driver\" : \"driver30sec\",\n" +
				"    \"processor_id\": \"load_machine_event_ES\",\n" +
				"    \"handler_class\": \"com.aw.compute.streams.processor.impl.GenericESProcessor\",\n" +
				"    \"source_topic\": [\"events\"],\n" +
				"    \"offset_type\": \"smallest.always\",\n" +
				"    \"destination_topics\": [],\n" +
				"    \"processor_data\": {\n" +
				"      \"index_type\": \"gameEvents\",\n" +
				"      \"index_name\": \"events\",\n" +
				"      \"id_attribute\": \"id\"\n" +
				"}} ";
		def2 = JSONUtils.objectFromString(exampleDef, StreamDef.class);

	}

}
