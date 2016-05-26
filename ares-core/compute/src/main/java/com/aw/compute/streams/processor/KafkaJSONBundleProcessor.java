package com.aw.compute.streams.processor;

import org.codehaus.jettison.json.JSONObject;

import com.aw.compute.streams.exceptions.StreamProcessingException;

/**
 * Process bundles whose data is entirely stored in the kafka message
 *
 *
 *
 */
public class KafkaJSONBundleProcessor extends JSONBundleProcessor {

	private static final long serialVersionUID = 1L;

	@Override
	public void process(String string) throws StreamProcessingException {
		try {

			//the kafka message IS the bundle in this case
			processBundle(new JSONObject(string));

		} catch (Exception e) {
			throw new StreamProcessingException("error processing bundle json", e);
		}

	}

}
