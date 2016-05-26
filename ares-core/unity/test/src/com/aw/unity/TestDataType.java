package com.aw.unity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.common.AbstractTaggable;
import com.aw.unity.exceptions.InvalidDataException;

/**
 * Data type used in testing
 *
 * {
	    "field1": 10,
	    "field2": "1.2.3.4",
	    "field3": "test string",
	    "field4": 15,
	    "field5": 20,
	    "field6": "ab:ab:ab:ab:ab:ab",
	    "field7": [
	        "1.2.3.4",
	        "2.3.4.5"
	    ]
	}
 *
 *
 */
public class TestDataType extends AbstractTaggable implements DataType {

	public static final Field FIELD_INT = new Field("field_int", FieldType.INT);
	public static final Field FIELD_IP = new Field("field_ip", FieldType.IP_ADDRESS);
	public static final Field FIELD_STRING = new Field("field_string", FieldType.STRING);
	public static final Field FIELD_LONG = new Field("field_long", FieldType.LONG);
	public static final Field FIELD_DOUBLE = new Field("field_double", FieldType.DOUBLE);
	public static final Field FIELD_MAC = new Field("field_mac", FieldType.MAC_ADDRESS);
	public static final Field FIELD_IP2 = new Field("field_ip2", FieldType.IP_ADDRESS, true);
	public static final Field FIELD_GUID = new Field("field_guid", FieldType.GUID);
	public static final Field FIELD_TIMESTAMP = new Field("field_timestamp", FieldType.TIMESTAMP);

	public TestDataType() {
		int index = 0;
		for (Field field : m_fields) {
			m_ordinalMap.put(field, index++);
		}
	}

	@Override
	public String getDescription() {
		return "test data type";
	}

	@Override
	public String getName() {
		return "test_data_type";
	}

	@Override
	public Field getIDField() {
		return FIELD_GUID; //we'll call this one our ID field..
	}

	@Override
	public int getOrdinal(Field field) {
		if (!m_ordinalMap.containsKey(field)) {
			throw new InvalidDataException("field " + field.getName() + " does not exist on type " + getName());
		}

		else {
			return m_ordinalMap.get(field);
		}
	}

	@Override
	public boolean hasField(Field field) {
		return m_ordinalMap.containsKey(field);
	}

	@Override
	public Field getTimeField() {
		return null;
	}

	public Field[] getFields() { return m_fields; }
	public void setFields(Field[] fields) { m_fields = fields; }
	private Field[] m_fields = new Field[] {
		FIELD_INT,
		FIELD_IP,
		FIELD_STRING,
		FIELD_LONG,
		FIELD_DOUBLE,
		FIELD_MAC,
		FIELD_IP2,
		FIELD_GUID,
	};

	private Map<Field, Integer> m_ordinalMap = new HashMap<Field, Integer>();

	/**
	 * @return The test data type singleton
	 */
	public synchronized static TestDataType get() {
		if (s_type == null) {
			s_type = new TestDataType();
		}
		return s_type;
	}
	private static TestDataType s_type;

	@Override
	public String getDataSource() {
		return "test";
	}

	@Override
	public DataTypeRepository getDataTypeRepository() {
		return null;
	}

	@Override
	public List<DataType> getSubTypes() {
		return Collections.emptyList();
	}

	@Override
	public Field getField(String name) {
		return null;
	}

	@Override
	public boolean hasField(String name) {
		return false;
	}

	@Override
	public UnityInstance getUnity() {
		return null;
	}

}
