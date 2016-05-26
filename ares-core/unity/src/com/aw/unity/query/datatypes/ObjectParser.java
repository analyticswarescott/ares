package com.aw.unity.query.datatypes;

import org.codehaus.jettison.json.JSONObject;

import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidDataException;
import com.aw.unity.json.JSONData;

/**
 * Parse sub objects from raw json. Will return a Data[] instance if it succeeds.
 *
 *
 */

public class ObjectParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {
		try {

			//get the data type for us
			DataTypeRepository repo = parentType.getDataTypeRepository();

			DataType type = repo.getDataType(field.getDataType());

			//parse the json as data
			Data data = new JSONData(type, (JSONObject)value);
			return data;

		} catch (Exception e) {
			throw new InvalidDataException("error parsing object " + value + ": " + value, e);
		}
	}

}
