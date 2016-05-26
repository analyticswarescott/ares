package com.aw.incident.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.incident.AbstractIncidentTest;
import com.aw.incident.CreationType;
import com.aw.incident.TestUnityInstance;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.dg.CommonField;
import com.aw.util.Statics;

public class AbstractIncidentActionTest extends AbstractIncidentTest {

	/**
	 * Test that the action's json representation contains all unity fields
	 *
	 * @param action
	 * @throws Exception
	 */
	protected void testUnityFields(IncidentAction action) throws Exception {

		String strAction = JSONUtils.objectToString(action);

		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");
		TestUnityInstance instance = new TestUnityInstance();

		Data actionData = instance.newData(strAction);

		assertNotNull("data not created from action " + action.getClass().getSimpleName(), actionData);

		JSONObject jsonAction = new JSONObject(actionData.toJsonString(false, true, true));

		DataType type = instance.getDataType(action.getUnityType());

		//check that all properties in the type are in the json
		Iterator<String> keys = jsonAction.keys();
		while (keys.hasNext()) {

			String key = keys.next();

			//if it's not the unity type, make sure it's in the data type
			if (CommonField.tryFromString(key) == null) {
				assertTrue("field " + key + " missing from type " + type.getName(), type.hasField(key));
			}

		}

		//check that all unity fields are in the data
		for (Field field : type.getFields()) {
			assertNotNull("field " + field + " missing from incident action json", actionData.getValue(field));
		}

		assertEquals("user should be aw", Statics.DG_USER, action.getUser().getUsername());
		assertEquals("creation type should be automatic", CreationType.AUTOMATIC, action.getCreationType());

	}

}
