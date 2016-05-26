package com.aw.streams.processor;
/*
package com.aw.stream.processors;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.compute.streams.processor.impl.SyslogProcessor;
import com.aw.compute.streams.processor.impl.SyslogProcessor.Facility;
import com.aw.compute.streams.processor.impl.SyslogProcessor.Protocol;
import com.aw.compute.streams.processor.impl.SyslogProcessor.Severity;
import com.aw.test.DGStreamUnitTest;

*/
/**
 * Test the syslog processor, assuming the framework hands me json based event bundles
 *
 *
 *
 *//*

public class SyslogProcessorTest extends DGStreamUnitTest {

	private static final String[] FIELDS = { "sha256", "dfd" };

	//contains host
	private static String GOOD_CONFIG =
			"{\n" +
			"	\"source_topic\": {},\n" +
			"	\"processor_id\": {},\n" +
			"	\"processor_data\": \n" +
			"	{\n" +
			"		\"port\": 512,\n" +
			"		\"host\": \"hosttest\",\n" +
			"		\"protocol\": \"UDP\",\n" +
			"		\"maxTries\": \"3\",\n" +
			"		\"facility\": \"local_use_3\",\n" +
			"		\"severity\": \"alert\",\n" +
			"		\"fields\": [\"sha256\",\"dfd\"]\n" +
			"	}\n" +
			"}";

	//missing host
	private static String BAD_CONFIG =
			"{\n" +
			"	\"source_topic\": {}\n" +
			"	\"processor_id\": {}\n" +
			"	\"processor_data\": \n" +
			"	{\n" +
			"		\"port\": 512,\n" +
			"		\"protocol\": \"UDP\",\n" +
			"		\"maxTries\": \"3\",\n" +
			"		\"facility\": \"local_use_3\",\n" +
			"		\"severity\": \"alert\",\n" +
			"	}\n" +
			"}";


	//TODO: re-enable
	//@Test
	public void OnInit_good() throws Exception {

		//make sure things get set up ok
		SyslogProcessor proc = getSyslogProcessor();
		proc.init(new org.codehaus.jettison.json.JSONObject(GOOD_CONFIG), "0");
				//make sure all properties are set correctly from the json config
		assertEquals("while checking host", "hosttest", proc.getHost());
		assertEquals("while checking port", 512, proc.getPort());
		assertEquals("while checking protocol", Protocol.UDP, proc.getProtocol());
		assertEquals("while checking maxTries", 3, proc.getMaxTries());
		assertEquals("while checking facility", Facility.LOCAL_USE_3, proc.getFacility());
		assertEquals("while checking severity", Severity.ALERT, proc.getSeverity());
		assertEquals("while checking fields", new HashSet<String>(Arrays.asList(new String[] {
				"sha256", "dfd"
			}
		)), proc.getFields());

	}

	//TODO: re-enable
	//@Test
	public void OnInit_bad() throws Exception {

		try {

			//make sure things get set up ok
			SyslogProcessor proc = getSyslogProcessor();
			proc.init(new JSONObject(BAD_CONFIG), "0");
			//make sure it says something about host
			fail("invalid config should have caused exception");

		} catch (JSONException e) {
		}


	}

	@Test
	public void call_udp() throws Exception {

		class UDPListener implements Runnable {

			public UDPListener() {
				new Thread(this).start();
			}

			@Override
			public void run() {
				DatagramSocket socket = null;
				try {

					socket = new DatagramSocket(getPort());
					DatagramPacket packet = new DatagramPacket(new byte[102400], 102400);

					//receive the message
					socket.receive(packet);

					m_message = new String(packet.getData(), 0, packet.getLength());

					//let any listener know we got something
					synchronized (this) {
						this.notifyAll();
					}

				} catch(Exception e) {
					m_exception = e;
				} finally {
					if (socket != null) {
						socket.close();
					} else {
						throw new RuntimeException("Socket not created for test");
					}
				}
			}

			public Exception getException() { return m_exception; }
			private Exception m_exception;

			public String getMessage() { return m_message; }
			private String m_message;

		}

		SyslogProcessor proc = getSyslogProcessor();

		//create the listener
		UDPListener listener = new UDPListener();

		Thread.sleep(100);

		//configure with loopback, UDP
		proc.setHost("127.0.0.1");
		proc.setProtocol(Protocol.UDP);
		proc.setPort(getPort());
		proc.setFields(new HashSet<String>(Arrays.asList(FIELDS)));
		proc.setSourceHostname("my-host");

		//read from the test json topic that should contain the data we need
		String json = readFile("topicData.json");

		//test processing the json
		proc.call(json);

		//get the syslog message - give it up to 1 second for the notify
		String msg = listener.getMessage();
		synchronized (listener) {
			if (msg == null) {
				listener.wait(1000);
			}
			msg = listener.getMessage();
		}

		//throw an exception if needed
		if (listener.getException() != null) {
			throw listener.getException();
		}

		validateSyslogData(msg, "my-host");

	}

	protected SyslogProcessor getSyslogProcessor() {
		return new SyslogProcessor();
	}

	@Test
	public void call_tcp() throws Exception {

		SyslogProcessor proc = getSyslogProcessor();

		//create the listener
		TCPListener listener = new TCPListener();

		Thread.sleep(100);

		//configure with loopback, TCP
		proc.setHost("127.0.0.1");
		proc.setProtocol(Protocol.TCP);
		proc.setPort(getPort());
		proc.setFields(new HashSet<String>(Arrays.asList(FIELDS)));

		//test processing the json
		proc.call(readFile("topicData.json"));

		//close the connection as we're using IOUtils which reads the tcp stream until closed
		proc.cleanup();

		//get the syslog message - give it up to 1 second for the notify
		String msg = listener.getMessage();
		synchronized (listener) {
			if (msg == null) {
				listener.wait(1000);
			}
			msg = listener.getMessage();
		}

		//throw an exception if needed
		if (listener.getException() != null) {
			throw listener.getException();
		}

		//make sure the message isn't null
		assertNotNull("while receiving syslog over tcp, msg is null", msg);

		validateSyslogData(msg);

	}

	protected void validateSyslogData(String msg, String hostname) throws Exception {

		//make sure the syslog message matched
		Pattern pattern = Pattern.compile("\\<134\\>(.*?) " + hostname + "\\|sha256=testSHA\\|dfd=c:\\\\test\\\\path");
		Matcher matcher = pattern.matcher(msg);
		assertTrue("syslog format didn't match expected (" + msg + ")", matcher.find());

	}

	//validate with local hostname
	protected void validateSyslogData(String msg) throws Exception {

		validateSyslogData(msg, InetAddress.getLocalHost().getHostName());

	}

//	//test with local spark cluster
// TODO: commented this out as there is no test data available in machine_events in the dev stack (dev-dg8-01) as of the time of this writing
//	@Test
//	public void spark() throws Exception {
//
//		int count = IOUtils.readLines(new FileReader(getTopicPath(TOPIC_MACHINE_EVENT_DLP))).size();
//
//		//we only want 1 connection for this test
//		JavaSparkContext ctx = newSparkContext(1);
//
//		//get the rdd for the test data
//		JavaRDD<String> rdd = ctx.textFile(getTopicPath(TOPIC_MACHINE_EVENT_DLP));
//
//		TCPListener listener = new TCPListener();
//
//		Thread.sleep(100L);
//
//		SyslogProcessor proc = getSyslogProcessor();
//
//		//set it up for a loopback stream
//		proc.setHost("127.0.0.1");
//		proc.setProtocol(Protocol.TCP);
//		proc.setPort(getPort());
//
//		//run the foreach
//		rdd.foreachAsync(proc);
//
//		synchronized (listener) {
//			if (count > listener.getMessageCount()) {
//				listener.wait(5000L); //give it 5 seconds, should be more than enough
//			}
//		}
//
//		assertEquals("Didn't process expected message count", count, listener.getMessageCount());
//
//		ctx.stop();
//
//	}

	class TCPListener implements Runnable {

		public TCPListener() {
			new Thread(this).start();
		}

		@Override
		public void run() {
			ServerSocket socket = null;
			Socket connection = null;
			try {

				socket = new ServerSocket(getPort());
				connection = socket.accept();

				//receive the message
				InputStream in = connection.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = br.readLine();
				while (line != null) {
					m_message = line;
					m_messageCount++;
					line = br.readLine();

				}

				//let any listener know we got something
				synchronized (this) {
					this.notifyAll();
				}

			} catch(Exception e) {
				m_exception = e;
			} finally {

				try {
					if (socket != null) {
						socket.close();
					} else {
						throw new Exception("Socket not created for test", m_exception);
					}
				} catch (Exception e) {
					if (m_exception != null) {
						m_exception = e;
					}
				}

				try {
					if (connection != null) {
						connection.close();
					} else {
						throw new Exception("Socket not created for test");
					}
				} catch (Exception e) {
					if (m_exception != null) {
						m_exception = e;
					}
				}

			}
		}

		public int getMessageCount() { return m_messageCount; }
		private int m_messageCount = 0;

		public Exception getException() { return m_exception; }
		private Exception m_exception;

		public String getMessage() { return m_message; }
		private String m_message;

	}

	protected int getPort() { return 1514; }

}
*/
