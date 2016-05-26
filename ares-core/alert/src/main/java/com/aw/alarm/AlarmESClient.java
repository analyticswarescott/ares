package com.aw.alarm;

import org.apache.log4j.Logger;

import com.aw.common.util.es.ESClient;
import com.aw.platform.Platform;

public class AlarmESClient extends ESClient {

	private static final Logger LOGGER = Logger.getLogger(AlarmESClient.class);

	public AlarmESClient(Platform platform) {
		super(platform);
	}

	public void createAlarm(Alarm alarm) {

		LOGGER.warn("TODO: add alarm");

		//TODO: build a data object containing the additional fields for the alarm and insert it

	}

}
