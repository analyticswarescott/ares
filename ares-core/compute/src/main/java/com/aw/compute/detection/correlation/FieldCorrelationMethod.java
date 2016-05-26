package com.aw.compute.detection.correlation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aw.unity.Data;
import com.aw.unity.Field;

/**
 * The standard method of tying two events together, by field values
 *
 *
 *
 */
public class FieldCorrelationMethod implements CorrelationMethod {

	private static final long serialVersionUID = 1L;

	public static String KEY_DELIMITER = "|";

	public FieldCorrelationMethod(Field... fields) {
		//build a real ArrayList to avoid any serialization issues, etc
		this(new ArrayList<>(Arrays.asList(fields)));
	}

	public FieldCorrelationMethod(List<Field> fields) {
		m_fields = fields;
	}

	/**
	 * Build key from a given unity Data instance
	 *
	 * @param data The data to build the key from
	 * @return The key
	 */
	public String toKey(Data data) {

		StringBuilder ret = new StringBuilder();
		for (Field field : m_fields) {

			//append the next field onto the key
			ret.append(KEY_DELIMITER);
			ret.append(String.valueOf(data.getValue(field)));

		}

		//return the key we've built
		return ret.toString();

	}

	public List<Field> getFields() { return m_fields; }
	public void setFields(List<Field> fields) { m_fields = fields; }
	private List<Field> m_fields;

}
