/*
package com.aw.common.system;

import static org.junit.Assert.assertEquals;

import com.aw.common.hadoop.structure.HadoopPurpose;
import org.junit.Test;

import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileInputMetadataTest {

	@Test
	public void test() throws Exception {

		FileInputMetadata metadata = new FileInputMetadata(HadoopPurpose.EDR, "tenant", "machine", "path", "bundle", "guid", false);

		ObjectMapper mapper = new ObjectMapper();

		String json = JSONUtils.objectToString(metadata);
		JsonNode fromString = mapper.readTree(json);
		*/
/**
{
    "purpose": "edr",
    "filename": "bundle",
    "machine_id": "machine",
    "tenant_id": "tenant",
    "path": "path",
    "errors": [],
    "guid" : "guid",
    "has_parts" : false
}
		 *//*

		String expected = "{\n" +
				"    \"purpose\": \"edr\",\n" +
				"    \"filename\": \"bundle\",\n" +
				"    \"machine_id\": \"machine\",\n" +
				"    \"tenant_id\": \"tenant\",\n" +
				"    \"path\": \"path\",\n" +
				"    \"errors\": [],\n" +
				"    \"guid\" : \"guid\",\n" +
				"    \"has_parts\" : false\n" +
				"}";

		assertEquals("json not correct", mapper.readTree(expected), fromString);

		//make sure it loads back
		metadata = JSONUtils.objectFromString(fromString.toString(), FileInputMetadata.class);

		json = JSONUtils.objectToString(metadata);
		fromString = mapper.readTree(json);

		assertEquals("bundle metadata not loaded back from json correctly", mapper.readTree(expected), fromString);

	}

}
*/
