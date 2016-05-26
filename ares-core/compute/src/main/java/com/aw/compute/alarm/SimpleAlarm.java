package com.aw.compute.alarm;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;

import com.aw.alarm.Alarm;
import com.aw.common.util.JSONUtils;
import com.aw.compute.detection.SimpleRule;
import com.aw.document.DocumentType;
import com.aw.unity.Data;
import com.aw.unity.dg.CommonField;
import com.aw.unity.json.JSONData;

/**
 * simple alarm - represents a single source event to alarm mapping from a simple rule
 *
 *
 *
 */
public class SimpleAlarm implements Alarm {

	public static final String DATA_TYPE = "simple_alarm";

	public SimpleAlarm(Data sourceEvent, SimpleRule simpleRule) throws Exception {

		guid = UUID.randomUUID();

		//clone the original source event to build the alarm Data object
		JSONData data = new JSONData(sourceEvent.getType(), ((JSONData)sourceEvent).getJson());

		//inject the simple rule properties
		data.getJson().put(CommonField.DG_DET_NAME.toString(), simpleRule.getName());
		data.getJson().put(CommonField.DG_DET_TYPE.toString(), simpleRule.getType());
		data.getJson().put(CommonField.DG_DET_ID.toString(), simpleRule.getId());

		//generate a guid for the alarm firing
		data.getJson().put(CommonField.DG_ALARM_ID.toString(), guid.toString());

		//add tags array
		data.getJson().put(CommonField.DG_TAGS_STRING, new JSONArray(JSONUtils.objectToString(simpleRule.getTags())));

		this.data = data;

	}

	/**
	 * time of the alarm is the time of the source event
	 */
	@Override
	public Instant getTime() {
		return data.getTime();
	}

	/**
	 * @return the type of rule that generated this alarm
	 */
	public DocumentType getRuleType() { return DocumentType.SIMPLE_RULE; }

	@Override
	public Collection<Data> asData() {
		return Collections.singleton(data);
	}

	private Data data;

	@Override
	public UUID getGuid() { return guid; }
	private UUID guid;

}
