
package com.aw.compute.streams.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.spark.StreamDef;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.TestComputeModule;
import com.aw.compute.referencedata.OperationTypeLookupData;
import com.aw.compute.referencedata.ReferenceDataManager;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.platform.Platform;
import com.aw.test.DGStreamUnitTest;
import com.aw.unity.DataType;
import com.aw.unity.UnityInstance;
import com.aw.unity.json.DefaultJSONDataTypeResolver;
import com.aw.util.Statics;

import kafka.producer.KeyedMessage;

public class XMLBundleProcessorTest extends DGStreamUnitTest {

/*	@Test
	public void processBundle() throws Exception {

		//init spark injection for testing
		ComputeInjector.init(new TestComputeModule());

		final List<JSONObject> events = new ArrayList<JSONObject>();
		final Map<String, AtomicInteger> countByType = new HashMap<String, AtomicInteger>();

		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

		XMLBundleProcessor processor = new XMLBundleProcessor() {

			private static final long serialVersionUID = 1L;

			@Override
			public void process(String string) throws StreamProcessingException {
				try {
					processBundle(new InputSource(new StringReader(string)));
				} catch (Exception e) {
					throw new StreamProcessingException("error processing xml bundle", e);
				}
			}

			@Override
			protected void output(KeyedMessage<String, String> km) throws Exception {

				events.add(new JSONObject(km.message()));

				JSONObject obj = new JSONObject(km.message());
				DataType type = getDependency(UnityInstance.class).getMetadata().getDataTypeRepository().resolveDataType(obj);

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

		};

		processor.init(mock(StreamDef.class));
		processor.process(getFileAsString("bundle-alerts.xml", Statics.UTF_16));

		//67 events in the bundle
		assertEquals("event size not right", 8, events.size());

		boolean found = false;
		for (JSONObject event : events) {
			if (event.has("uad_meid") && event.getString("uad_meid").equals("{179646C6-BCE7-1034-9DBF-806E67F5ABE5}")) {

				found = true;

				//check custom data
				JSONArray customData = event.getJSONArray("dg_custom_data");
				assertEquals(3, customData.length());
				assertEquals(10, customData.getJSONObject(0).getJSONArray("dg_values").length());

				//check alert data
				JSONArray alerts = event.getJSONArray("dg_alert");
				assertEquals(1, alerts.length());
				assertEquals("{47BFB785-976F-11E4-B9B8-806E6F6E6963}", alerts.getJSONObject(0).getString("alert_aid"));

				//check policy
				JSONArray policy = alerts.getJSONObject(0).getJSONArray("dg_policy");
				assertEquals(1, policy.length());
				assertEquals("{EBB4C52F-D5E9-4CD6-B910-6BA3037F5F92}", policy.getJSONObject(0).getString("p_pid"));
				assertEquals("{4E1B934B-9BDF-49F2-B1AB-05A5BE774390}", policy.getJSONObject(0).getString("p_rpid"));

			}
		}

		assertTrue("couldn't verify event with alerts, policy, and custom data", found);

		//take a look at the last event
		JSONObject lastEvent = events.get(events.size() - 1);
		assertNotNull("event missing timestamp", lastEvent.optString("esu"));

		//this will throw
		assertNotNull("missing ua_medid on event", lastEvent.optString("uad_medid", null));

		assertEquals("time wrong", 1420751765156L, lastEvent.getLong("ua_esu"));
		assertEquals("ua id wrong", "{179646C2-BCE7-1034-9DBF-806E67F5ABE5}", lastEvent.optString("uad_meid", null));
		assertEquals("decoded string wrong", "CF-WIN7x86-ATP", lastEvent.optString("ua_dn", null));

		assertEquals("bundle id wrong", "{91D06EA4-977B-11E4-B9B8-F81654CB2586}", lastEvent.getString("dg_bid"));
		assertEquals("machine id wrong", "{6872B44F-CD2D-C6A1-5A42-9C098B902DDB}", lastEvent.getString("dg_mid"));

		//test that the date format parses
		new Date(Long.parseLong(lastEvent.getString("ua_esu")));

	}*/

	public static class TestOTLookupData extends OperationTypeLookupData {

		private UnityInstance unity;

		@Inject @com.google.inject.Inject
		public TestOTLookupData(UnityInstance unity, Platform platform) throws Exception {
			super(platform);
			this.unity = unity;
		}

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

