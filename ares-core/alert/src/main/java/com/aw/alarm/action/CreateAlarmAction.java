package com.aw.alarm.action;

import org.apache.log4j.Logger;

import com.aw.action.ActionContext;
import com.aw.action.ActionType;
import com.aw.action.exceptions.ActionExecutionException;
import com.aw.alarm.Alarm;
import com.aw.common.messaging.Topic;
import com.aw.unity.Data;
import com.google.common.base.Preconditions;

/**
 * create an alarm in the system
 *
 *
 *
 */
public class CreateAlarmAction extends AbstractAlarmAction {

	static final Logger LOGGER = Logger.getLogger(CreateAlarmAction.class);

	@Override
	public void execute(ActionContext ctx) throws ActionExecutionException {

		try {

			//get the registered alarm for this action
			Alarm alarm = ctx.getVariableObject(VAR_ALARM);

			//make sure we have an alarm
			Preconditions.checkNotNull(alarm, "no " + VAR_ALARM + " variable was registered when CreateAlarmAction was executed");

			//send the alarm's data to the alarm topic, this will create the alarm entries in elasticsearch and process them in the detection framework as necessary
			for (Data data : alarm.asData()) {

				//clean=false here so we retain the aw-specificawype property allowing us to convert back to a Data object when inserted to elasticsearch
				ctx.getPlatformMgr().sendMessage(Topic.ALARM, data.toJsonString(false, true, false));

			}

		} catch (Exception e) {
			throw new ActionExecutionException("error sending message to topic " + Topic.ALARM, this, e);
		}

	}

	@Override
	public ActionType getType() {
		return AlarmActionType.ALARM_CREATE;
	}

}
