package com.aw.compute.streams.processor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.messaging.Topic;
import com.aw.common.system.FileInputMetadata;
import com.aw.compute.TestDocumentHandler;
import com.aw.compute.TestUnityInstance;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.TestComputeModule;
import com.aw.compute.streams.processor.EDRProcessor;
import com.aw.test.DGStreamUnitTest;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.util.Statics;

import kafka.producer.KeyedMessage;

/**
 * Test the processing of edr data
 *
 *
 *
 */
public class EDRProcessorTest extends DGStreamUnitTest {

	@Test
	public void test() throws Exception {

		//init spark injection for testing
		ComputeInjector.init(new TestComputeModule());

		//keep track of some stuff to do assertions later on
		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

		TestEDRProcessor processor = new TestEDRProcessor();

		//bogus metadata
		FileInputMetadata testData = new FileInputMetadata(HadoopPurpose.BUNDLE, "tenant", "machine", "path", "file", "test_scan_id", false);

		//process a test edr scan
		processor.processFile(testData, getFileAsInputStream("test_edr_scan.zip"));

		//do our assertions
		assertEquals("on disk executable count wrong", 126, processor.messageCount.get(Topic.ON_DISK_EXECUTABLE).get());
		assertEquals("wmi data count wrong", 2, processor.messageCount.get(Topic.WMI_DATA).get());
		assertEquals("running process count wrong", 38, processor.messageCount.get(Topic.RUNNING_PROCESS).get());
		assertEquals("windows registry count wrong", 153, processor.messageCount.get(Topic.WINDOWS_REGISTRY).get());
		assertEquals("windows log count wrong", 20, processor.messageCount.get(Topic.WINDOWS_LOG).get());

	}

	@SuppressWarnings("serial")
	private class TestEDRProcessor extends EDRProcessor {

		public TestEDRProcessor() throws Exception {
			m_docs = new TestDocumentHandler();
		}

		@Override
		protected void log(String log) {
		}

		//override for unit testing - no kafka
		@Override
		protected void sendMessage(KeyedMessage<String, String> message, EDRProcessingContext pc) throws Exception {

			//track message counts per type
			AtomicInteger counter = messageCount.get(pc.getTopic());
			if (counter == null) {
				messageCount.put(pc.getTopic(), counter = new AtomicInteger(0));
			}

			counter.incrementAndGet();

			//turn into data, make sure there are no exceptions turning into json
			try {

				Data data = unity.newData(message.message());
				data.toJsonString(false, true, true);

				//keep last 10 message for assertions
				messages.addLast(message);
				if (messages.size() > 10) {
					messages.removeFirst();
				}

			} catch (Exception e) {
				System.err.println("message:\n" + new JSONObject(message.message()).toString(4));
				e.printStackTrace(System.err);
				fail("error processing message, stack trace in stderr, message=" + message.message());
			}

		}

		Map<Topic, AtomicInteger> messageCount = new HashMap<Topic, AtomicInteger>();
		UnityInstance unity = new TestUnityInstance();
		LinkedList<KeyedMessage<String, String>> messages = new LinkedList<KeyedMessage<String, String>>();
		TestDocumentHandler m_docs;

	}
}
