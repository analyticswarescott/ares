package com.aw.unity;

import java.time.Instant;

public class TestData implements Data {

	/**
	 * @param fieldValues Defined in the order in which they appear in the type (i.e. field1, field2, etc)
	 */
	public TestData(Object... fieldValues) {

		if (fieldValues == null) {
			throw new RuntimeException("can't test data with no values");
		}

		if (fieldValues.length % 2 != 0) {
			throw new RuntimeException("fieldValues must contain fields and values in pairs");
		}

		for (int x=0; x<fieldValues.length; x+=2) {
			Field field = (Field)fieldValues[x];
			m_values[m_type.getOrdinal(field)] = field.fromString(String.valueOf(fieldValues[x+1]));
		}

	}

	@Override
	public UnityInstance getUnity() {
		return null;
	}

	@Override
	public Instant getTime() {
		return Instant.now();
	}

	@Override
	public String toJsonString(boolean pretty, boolean epochMillis, boolean strict) {
		return "{}";
	}

	@Override
	public String getGuid() {
		return "test_id";
	}

	@Override
	public Object getValue(Field field) {

		int index = m_type.getOrdinal(field);
		return m_values[index];

	}

	public DataType getType() { return m_type; }
	public void setType(DataType type) { m_type = type; }
	private DataType m_type = new TestDataType();

	public Object[] getValues() { return m_values; }
	public void setValues(Object[] values) { m_values = values; }
	private Object[] m_values = new Object[m_type.getFields().length];

}
