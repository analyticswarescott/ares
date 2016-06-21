package com.aw.unity;

import java.util.HashMap;
import java.util.Map;

public class TestFieldRepository implements FieldRepository {

	/**
	 * Wraps another field repository for testability
	 *
	 * @param repo The field repository to wrap
	 */
	public TestFieldRepository(FieldRepository repo) {
		m_repo = repo;
	}

	public TestFieldRepository() {
		m_fieldMap.put(TestDataType.FIELD_DOUBLE.getName(), TestDataType.FIELD_DOUBLE);
		m_fieldMap.put(TestDataType.FIELD_LONG.getName(), TestDataType.FIELD_LONG);
		m_fieldMap.put(TestDataType.FIELD_IP.getName(), TestDataType.FIELD_IP);
		m_fieldMap.put(TestDataType.FIELD_STRING.getName(), TestDataType.FIELD_STRING);
		m_fieldMap.put(TestDataType.FIELD_MAC.getName(), TestDataType.FIELD_MAC);
		m_fieldMap.put(TestDataType.FIELD_IP2.getName(), TestDataType.FIELD_IP2);
		m_fieldMap.put(TestDataType.FIELD_INT.getName(), TestDataType.FIELD_INT);
	}

	@Override
	public Field[] getFields() {
		if (m_repo != null) {
			return m_repo.getFields();
		}

		return new Field[] {
				TestDataType.FIELD_INT,
				TestDataType.FIELD_IP,
				TestDataType.FIELD_STRING,
				TestDataType.FIELD_LONG,
				TestDataType.FIELD_DOUBLE,
				TestDataType.FIELD_MAC,
				TestDataType.FIELD_IP2,
				TestDataType.FIELD_GUID
		};
	}

	@Override
	public Field getField(String name) {
		if (m_repo != null) {
			return m_repo.getField(name);
		}

		else {
			return m_fieldMap.get(name);
		}
	}

	@Override
	public boolean hasField(String name) {
		return m_fieldMap.containsKey(name);
	}

	public Map<String, Field> getFieldMap() { return m_fieldMap; }
	public void setFieldMap(Map<String, Field> fieldMap) { m_fieldMap = fieldMap; }
	private Map<String, Field> m_fieldMap = new HashMap<String, Field>();

	private FieldRepository m_repo;

}
