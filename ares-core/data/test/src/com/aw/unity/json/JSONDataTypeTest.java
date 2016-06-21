package com.aw.unity.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.aw.common.exceptions.InvalidRequestException;
import com.aw.unity.Field;
import com.aw.unity.FieldRepository;
import com.aw.unity.FieldType;
import com.aw.unity.TestUnityInstance;
import com.aw.unity.exceptions.FieldNotFoundException;
import com.aw.unity.exceptions.InvalidDataException;
import com.google.common.collect.Maps;

public class JSONDataTypeTest {

	/**
	  {
	  "name" : "test_type",
	  "data_source" : "test",
	  "display_name" : "Test Type",
	  "description" : "Test Type Description",
	  "id_field" : "field_guid",
	  "field_refs" : [
	  {
	  "name" : "field_int",
	  "path" : [ "field1" ]
	  },
	  {
	  "name" : "field_string",
	  "path" : [ "field2" ]
	  },
	  {
	  "name" : "field_double",
	  "path" : [ "field3" ]
	  },
	  {
	  "name" : "field_timestamp",
	  "path" : [ "field4" ]
	  },
	  {
	  "name" : "field_float",
	  "path" : [ "field5" ]
	  },
	  {
	  "name" : "field_guid",
	  "path" : [ "field6" ]
	  },
	  {
	  "name" : "field_mac",
	  "path" : [ "field7" ]
	  }
	  ]
	  }
	 **/
	public static final String TYPE_JSON =
			"{\n" +
					"	\"name\" : \"test_type\",\n" +
					"    \"data_source\" : \"test\",\n" +
					"	\"display_name\" : \"Test Type\",\n" +
					"	\"description\" : \"Test Type Description\",\n" +
					"	\"id_field\" : \"field_guid\",\n" +
					"	\"field_refs\" : [\n" +
					"		{\n" +
					"			\"name\" : \"field_int\",\n" +
					"      		\"path\" : [ \"field1\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_string\",\n" +
					"      		\"path\" : [ \"field2\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_double\",\n" +
					"      		\"path\" : [ \"field3\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_timestamp\",\n" +
					"      		\"path\" : [ \"field4\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_float\",\n" +
					"      		\"path\" : [ \"field5\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_guid\",\n" +
					"      		\"path\" : [ \"field6\" ]\n" +
					"		},\n" +
					"		{\n" +
					"			\"name\" : \"field_mac\",\n" +
					"      		\"path\" : [ \"field7\" ]\n" +
					"		}\n" +
					"	]\n" +
					"}";

	//test that it loads up ok, fields are correct etc
	@Test
	public void test() throws Exception {
		FieldRepository repository = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);
		JSONDataType dataType = JSONDataType.fromString(null, TYPE_JSON, repository);

		//make sure things look ok
		assertEquals("field count wrong", 7, dataType.getFields().length);
		assertEquals("field name wrong", "field_double", dataType.getFields()[2].getName());
		assertEquals("field type wrong", FieldType.FLOAT, dataType.getFields()[4].getType());
		assertEquals("data source wrong", "test", dataType.getDataSource());

	}

	@Test
	public void testFromCollection() throws Exception {
		String name = UUID.randomUUID().toString();
		String description = UUID.randomUUID().toString();
		Field idField = makeTestField();

		Collection<JSONFieldRef> refs = new ArrayList<>();
		refs.add( new JSONFieldRef("fieldRefOne",
				new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()}));
		refs.add(new JSONFieldRef("fieldRefTwo", null));

		JSONDataType jsonDataType = JSONDataType.fromCollection(name, description, idField, refs);
		assertNotNull(jsonDataType);
		assertEquals(name, jsonDataType.getName());
		assertEquals(description, jsonDataType.getDescription());
		assertEquals(idField, jsonDataType.getIDField());

		JSONFieldRef[] fieldRefs = jsonDataType.getFieldRefs();
		assertEquals(refs.size(), fieldRefs.length);

		for (JSONFieldRef fieldRef : fieldRefs) {
			assertTrue(refs.contains(fieldRef));

			if ( "fieldRefOne".equals( fieldRef.getName() ) ) {
				assertNotEquals( fieldRef.getPath()[0], fieldRef.getName() );
			}
			if ( "fieldRefTwo".equals( fieldRef.getName() ) ) {
				assertEquals( fieldRef.getPath()[0], fieldRef.getName() );
			}
		}
	}

	@Test(expected=InvalidDataException.class)
	public void testFromCollectionErrors1() throws Exception {
		JSONDataType.fromCollection(UUID.randomUUID().toString(), UUID.randomUUID().toString(), makeTestField(), new ArrayList<>());
	}

	@Test(expected=InvalidDataException.class)
	public void testFromCollectionErrors2() throws Exception {
		JSONDataType.fromCollection(null, UUID.randomUUID().toString(), makeTestField(), makeRefs());
	}

	@Test(expected=InvalidDataException.class)
	public void testFromCollectionErrors3() throws Exception {
		JSONDataType.fromCollection(UUID.randomUUID().toString(), null, makeTestField(), makeRefs());
	}

	@Test(expected=InvalidDataException.class)
	public void testFromCollectionErrors4() throws Exception {
		JSONDataType.fromCollection(UUID.randomUUID().toString(), UUID.randomUUID().toString(), makeTestField(), new ArrayList<>());
	}

	@Test(expected=InvalidDataException.class)
	public void testFromCollectionErrors5() throws Exception {
		JSONDataType.fromCollection(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, makeRefs());
	}

	private Field makeTestField() {
		return new Field("mid", FieldType.STRING, "machine id", "machine id");
	}

	private Collection<JSONFieldRef> makeRefs() {
		Collection<JSONFieldRef> refs = new ArrayList<>();
		refs.add(new JSONFieldRef(UUID.randomUUID().toString(),
				new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()}));
		refs.add(new JSONFieldRef(UUID.randomUUID().toString(),
				new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()}));
		return refs;
	}

	@Test(expected=FieldNotFoundException.class)
	public void getJSONObjectFieldErrors1() throws Exception {
		FieldRepository repository = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);
		JSONDataType dataType = JSONDataType.fromString(null, TYPE_JSON, repository);
		dataType.getField("EXPECTED_TO_NOT_EXIST");
	}

	@Test(expected=InvalidRequestException.class)
	public void getJSONObjectFieldErrors2() throws Exception {
		FieldRepository repository = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);
		JSONDataType dataType = JSONDataType.fromString(null, TYPE_JSON, repository);
		dataType.getFieldFromPath(null);
	}

	@Test
	public void setFieldByAbsolutePath() throws Exception {
		FieldRepository repository = JSONFieldRepository.fromString(new TestUnityInstance(), JSONFieldRepositoryTest.FIELD_JSON);
		JSONDataType dataType = JSONDataType.fromString(null, TYPE_JSON, repository);
		Field idField = makeTestField();

		Map<String, Field> absolutePathMap = Maps.newHashMap();
		absolutePathMap.put("1.2.3.4", idField);
		dataType.setFieldByAbsolutePath( absolutePathMap );

		assertEquals(absolutePathMap, dataType.getFieldByAbsolutePath());

		Field fieldFromPath = dataType.getFieldFromPath("1", "2", "3", "4");
		assertNotNull( fieldFromPath );
	}

}