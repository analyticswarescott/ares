package com.aw.compute.streams.processor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.spark.StreamDef;
import com.aw.compute.TestUnityInstance;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.TestComputeModule;
import com.aw.compute.referencedata.ReferenceDataManager;
import com.aw.compute.referencedata.ReferenceDataMap;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.KafkaJSONBundleProcessor;
import com.aw.test.DGStreamUnitTest;
import com.aw.unity.DataType;
import com.aw.unity.UnityInstance;
import com.aw.unity.json.DefaultJSONDataTypeResolver;
import com.aw.util.Statics;

import kafka.producer.KeyedMessage;

public class KafkaJSONBundleProcessorTest extends DGStreamUnitTest {

	@Test
	public void processBundle() throws Exception {

		//init spark injection for testing
		ComputeInjector.init(new TestComputeModule());

		final List<JSONObject> events = new ArrayList<JSONObject>();
		final Map<String, AtomicInteger> countByType = new HashMap<String, AtomicInteger>();

		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

		final UnityInstance unity = new TestUnityInstance();

		KafkaJSONBundleProcessor processor = new KafkaJSONBundleProcessor() {

			@Override
			protected void output(KeyedMessage<String, String> km) throws Exception {

				events.add(new JSONObject(km.message()));

				JSONObject obj = new JSONObject(km.message());
				DataType type = unity.getMetadata().getDataTypeRepository().resolveDataType(obj);

				//spot check some fields
				if (type.hasField("uad_medid")) {
					assertNotNull("medid field missing from event of type " + type.getName());
				}

				//add to the counts
				AtomicInteger integer = countByType.get(type.getName());
				if (integer == null) {
					integer = new AtomicInteger();
					countByType.put(type.getName(), integer);
				}
				integer.getAndIncrement();

			}

			@Override
			protected ReferenceDataMap<String, String> getOperationTypeMap() throws Exception {
				return new ReferenceDataMap<String, String>() {
					@Override
					public String get(String key) throws StreamProcessingException {
						DefaultJSONDataTypeResolver resolver = (DefaultJSONDataTypeResolver)unity.getMetadata().getDataTypeRepository().getResolvers()[0];
						DataType type = resolver.getMappings()[0].getValueMap().get(key);
						if (type == null) {
							resolver = (DefaultJSONDataTypeResolver)unity.getMetadata().getDataTypeRepository().getResolvers()[1];
							type = resolver.getMappings()[0].getValueMap().get(key);
						}
						return type == null ? null : type.getName();
					}
					@Override
					public ReferenceDataManager getManager() {
						return null;
					}
					@Override
					public void refreshNow() throws ProcessingException {
					}
					@Override
					public void setManager(ReferenceDataManager manager) {
					}
					@Override
					public int size() {
						return 1;
					}
				};
			}

		};

		processor.init(new StreamDef());
		processor.process(getFileAsString("bundle.json"));

		//67 events in the bundle
		assertEquals("event size not right", 66, events.size());

		//take a look at the last event
		JSONObject lastEvent = events.get(events.size() - 1);
		assertNotNull("event missing timestamp", lastEvent.optString("esu"));

		//this will throw
		assertNotNull("missing ua_meid on event", lastEvent.optString("uad_medid", null));
		assertNotNull("missing product name", lastEvent.optString("pi_pn", null));

		assertEquals("time wrong", 1448309341000L, lastEvent.getLong("ua_esu"));

		//test that the date format parses
		new Date(Long.parseLong(lastEvent.getString("ua_esu")));

	}

}
