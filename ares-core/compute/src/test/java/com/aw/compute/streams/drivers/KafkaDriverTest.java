package com.aw.compute.streams.drivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;

import com.aw.common.inject.TestProvider;
import com.aw.common.spark.StreamDef;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.PlatformMgr;

public class KafkaDriverTest {

	@Test
	public void testInitialize() throws Exception {

		DocumentHandler docs = spy(new TestDocumentHandler());

		//get some stream defs to test with
		List<Document> tenantStreams = docs.getDocumentsOfType(DocumentType.STREAM_TENANT);
		assertTrue(tenantStreams.size() > 0);

		//use the target driver name
		StreamDef stream = tenantStreams.get(0).getBodyAsObject();

		KafkaDriver driver = spy(new KafkaDriver(mock(PlatformMgr.class), new TestProvider<>(docs)));

		//set the driver name to the stream target driver
		driver.initialize(stream.getTargetDriver());

		verify(docs).getDocument(DocumentType.STREAM_DRIVER, "driver30sec");
		assertEquals("driver30sec", driver.getDriverRootName());

	}

	@Test
	public void testWaitForWork() throws Exception {

		DocumentHandler docs = spy(new TestDocumentHandler());
		PlatformMgr mgr = mock(PlatformMgr.class);

		//get some stream defs to test with
		List<Document> tenantStreams = docs.getDocumentsOfType(DocumentType.STREAM_TENANT);
		assertTrue(tenantStreams.size() > 0);

		//set up our objects
		Streams streams = new Streams();
		KafkaDriver driver = spy(new KafkaDriver(mgr, new TestProvider<>(docs)));
		driver.getStreamUpdates().add(streams); //test empty streams

		//mock dependencies and methods
		doNothing().when(driver).startStreamingContext();
		doNothing().when(driver).stopStreamingContext();
		doNothing().when(driver).applyUpdate(streams);

		//run once in test
		driver.setRunning(false);
		driver.waitForWork();

		verify(driver).startStreamingContext();
		verify(driver).applyUpdate(streams);
		verify(driver).stopStreamingContext();

	}

}
