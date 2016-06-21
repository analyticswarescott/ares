package com.aw.unity.json;

import org.codehaus.jettison.json.JSONObject;

import com.aw.unity.Data;
import com.aw.unity.DataFactory;
import com.aw.unity.DataType;
import com.aw.unity.UnityInstance;
import com.aw.unity.exceptions.InvalidDataException;

/**
 * Default JSON data factory
 *
 *
 */
public class JSONDataFactory implements DataFactory {

	@Override
	public Class<JSONObject> getRawType() {
		return JSONObject.class;
	}

	@Override
	public Data newData(Object rawData, DataType dataType, UnityInstance instance) {

		//we need a json object
		JSONObject object = null;

		if (rawData == null) {
			throw new InvalidDataException("Null data encountered");
		}

		//quicker than instanceof..
		else if (rawData.getClass() == JSONObject.class) {
			object = (JSONObject)rawData;
		}

		//worst case parse it ourselves
		else {

			try {
				object = new JSONObject(rawData.toString());
			} catch (Exception e) {
				throw new InvalidDataException("Error parsing data json", e);
			}

		}

		JSONData data = new JSONData(dataType, (JSONObject)object);
		data.setUnity(instance);
		return data;

	}

	@Override
	public Data newData(Object rawData, UnityInstance unity) {

		DataType dataType = unity.getMetadata().getDataTypeRepository().resolveDataType(rawData);

		Data ret = newData(rawData, dataType, unity);
		return ret;

	}

}
