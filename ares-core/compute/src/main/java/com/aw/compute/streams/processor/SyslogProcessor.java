
package com.aw.compute.streams.processor;

//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.ByteChannel;
//import java.nio.channels.DatagramChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.Charset;
//import java.text.SimpleDateFormat;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.function.VoidFunction;
//import org.apache.spark.streaming.kafka.OffsetRange;
//
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectReader;
//import org.codehaus.jettison.json.JSONObject;

/**
 * Handles incoming json, turning it into syslog messages and sends those messages via
 * TCP or UDP. Configured with a syslog address/port, connection type.
 *
 * This processor defaults to standard target port 514, UDP - the only required parameter is the target host.
 *
 * <li>Example config:
 * <code><pre>
 * {
 	"source_topic": {...}
	"processor_id": {...}
	"processor_data":
	<b>
	{
		"port": 514,
		"host": "hosttest",
		"protocol": "UDP",
		"maxTries": "3",
		"facility": "local_use_3",
		"severity": "alert",
		"fields": ["sha256","dfd"],
		"delimiter" : "|",
	}
	</b>
}
</pre></code>

 *
 *
 *//*

public class SyslogProcessor extends BaseHandler implements VoidFunction<String> {


	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(SyslogProcessor.class);

	*/
/**
	 * Supported protocols for the syslog stream
	 *//*

	public enum Protocol {
		TCP,
		UDP,
	}

	//don't write exceptions to the log more frequently than this
	private static final long EXCEPTION_INTERVAL = 5000L;

	//defaults
	public static final int MAX_TRIES_DEFAULT = 1; //by default just drop the message if it doesn't send rather than potentially slowing things down
	public static final Facility FACILITY_DEFAULT = Facility.LOCAL_USE_0;
	public static final Severity SEVERITY_DEFAULT = Severity.INFORMATIONAL;
	public static final Protocol PROTOCOL_DEFAULT = Protocol.UDP;
	public static final int PORT_DEFAULT = 514;

	//syslog date (RFC3164)
	protected static final SimpleDateFormat DATE_FORMAT_ONE_DIGIT_DAY = new SimpleDateFormat("MMM  d HH:mm:ss ");
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd HH:mm:ss ");

	private static final String PRI_START = "<";
	private static final String PRI_END = ">";

	protected static final String FIELD_DELIMITER_DEFAULT = "|";

	protected static final int DATE_FIRST_DAY_INDEX = 4;

	//LF delimiter between syslog in a TCP stream by default
	private static final String TCP_DELIMITER_DEFAULT = "\n";

	public enum Facility {
		KERNEL_MESSAGES,
		USER_LEVEL_MESSAGES,
		MAIL_SYSTEM,
		SYSTEM_DAEMONS,
		SECURITY_AUTHEORIZATION_MESSAGES,
		INTERNAL_SYSLOG_MESSAGES,
		LINE_PRINTER_SUBSYSTEM,
		NETWORK_NEWS_SUBSYSTEM,
		UUCP_SUBSYSTEM,
		CLOCK_DAEMON,
		SECURITY_AUTH_MESSAGES,
		FTP_DAEMON,
		NTP_SUBSYSTEM,
		LOG_AUDIT,
		LOG_ALERT,
		CLOCK_DAEMON_2,
		LOCAL_USE_0,
		LOCAL_USE_1,
		LOCAL_USE_2,
		LOCAL_USE_3,
		LOCAL_USE_4,
		LOCAL_USE_5,
		LOCAL_USE_6,
		LOCAL_USE_7;

		@JsonCreator
		public static Facility forValue(String val) { return Facility.valueOf(val.toUpperCase()); }
	}

	public enum Severity {
		EMERGENCY,
		ALERT,
		CRITICAL,
		ERROR,
		MEDIUM,
		NOTICE,
		INFORMATIONAL,
		DEBUG;

		@JsonCreator
		public static Severity forValue(String val) { return Severity.valueOf(val.toUpperCase()); }
	}

	public SyslogProcessor() {
	}

	@Override
	protected void OnInit(JSONObject processor_data)  throws Exception{
		super.OnInit(processor_data);

		try {

			//set our properties
			ObjectMapper mapper = new ObjectMapper();
			ObjectReader reader = mapper.readerForUpdating(this);
			reader.readValue(processor_data.toString());

			//make sure we're set up
			if (getHost() == null) {
				throw new Exception("host property must be set for a syslog processor");
			}

		} catch (Exception e) {
			//can't handle this here TODO: shouldn't this method throw a checked exception?
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void OnHandleRDD(OffsetRange[] offsetRanges, JavaRDD<String> vals) {

		//handle the json
		vals.foreach(this);

	}

	protected void finalize() throws Throwable {

		cleanup();

	}

	public void cleanup() throws Exception {

		if (m_channel != null) {
			m_channel.close();
			m_channel = null;
		}

	}

	*/
/**
	 * Called for each string element in the RDD - these are the json documents
	 *//*

	@Override
	public void call(String t) throws Exception {

		//parse the bundle data
		org.codehaus.jettison.json.JSONObject json = new org.codehaus.jettison.json.JSONObject(t);

		//the entire json document in this case is the event
		byte[] msg = buildPacket(json);

		if (isLogging()) {
			logger.info(new String(msg, Charset.forName("UTF-8")));
		}

		//try to send multiple times if asked
		for (int curTry=0; curTry<getMaxTries(); curTry++) {

			//when we succeed, stop trying
			if (tryToSend(msg)) {
				break;
			}

		}

	}

	*/
/**
	 * Try to send the syslog message
	 *
	 * @param msg The message to send
	 * @return If the message was sent
	 * @throws Exception If anything goes wrong (other than the send itself)
	 *//*

	private boolean tryToSend(byte[] msg) throws Exception {

		boolean ret = false;

		try {

			//make sure we're connected (lazily)
			switch (getProtocol()) {
				case TCP:
					checkConnectionTCP(); break;
				case UDP:
					checkConnectionUDP(); break;
			}

			//send the message
			send(msg);

			//if no exceptions, it worked
			ret = true;

		} catch (IOException e) {

			if (getMaxTries() > 0) {

				//give it a little time to avoid spinning
				Thread.sleep(100);

			}

			//try reconnecting next time through
			cleanup();

			//throttle the exception logging even more
			if (System.currentTimeMillis() - getLastLoggedException() > EXCEPTION_INTERVAL) {
				logger.error("Error sending syslog", e);
				setLastLoggedException(System.currentTimeMillis());
			}

		}

		return ret;

	}

	protected byte[] buildPacket(org.codehaus.jettison.json.JSONObject json) throws Exception {

		//simply build a syslog message of name/value pairs
		StringBuilder syslog = new StringBuilder();

		//add the header
		appendHeader(syslog);

		//flatten to a string
		appendKeys(syslog, "", json);

		//append LF to delimit messages in a tcp stream - this is pretty standard
		if (getProtocol() == Protocol.TCP) {
			syslog.append(getTcpDelimiter());
		}

		//convert to utf-8 to send
		return syslog.toString().getBytes(Charset.forName("UTF-8"));

	}

	protected void appendHeader(StringBuilder syslog) throws Exception {

		syslog.append(PRI_START);
		syslog.append(getFacility().ordinal() * 8 + getSeverity().ordinal());
		syslog.append(PRI_END);

		appendTimeAndHost(syslog);

	}

	protected void appendTimeAndHost(StringBuilder syslog) throws Exception {

		//build timestamp - use single digit day if applicable
		String timestamp = DATE_FORMAT.format(System.currentTimeMillis());
		if (timestamp.charAt(DATE_FIRST_DAY_INDEX) == '0') {
			timestamp = DATE_FORMAT_ONE_DIGIT_DAY.format(System.currentTimeMillis());
		}

		syslog.append(timestamp);

		//now append the hostname
		String hostname = getSourceHostname();
		if (hostname == null) {
			hostname = InetAddress.getLocalHost().getHostName();
		}
		syslog.append(hostname);

	}

	protected void appendKeys(StringBuilder syslog, String prefix, org.codehaus.jettison.json.JSONObject json) throws Exception {

		Iterator<String> keys = json.keys();

		while (keys.hasNext()) {

			//key the field key
			String key = keys.next();

			//if this field should be included
			if (getFields().size() == 0 || getFields().contains(key)) {

				//append the <delimiter>field=value
				syslog.append(getFieldDelimiter());
				syslog.append(key);
				syslog.append("=");

				//TODO: escape values?
				syslog.append(json.get(key).toString());

			}

		}
	}

	private void checkConnectionTCP() throws IOException {

		//if we need a connection, make one here
		if (!isConnected()) {

			SocketChannel channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(m_host, m_port));
			m_channel = channel;

		}

	}

	private void checkConnectionUDP() throws IOException {

		if (!isConnected()) {
			DatagramChannel channel = DatagramChannel.open();
			channel.connect(new InetSocketAddress(m_host, m_port));
			m_channel = channel;

		}

	}

	*/
/**
	 * Write the syslog message to the channel
	 *
	 * @param msg The syslog message
	 * @throws IOException
	 *//*

	private void send(byte[] msg) throws IOException {

		m_channel.write(ByteBuffer.wrap(msg));

	}

	private boolean isConnected() throws IOException {
		return m_channel != null && m_channel.isOpen();
	}

	public int getMaxTries() { return m_maxTries; }
	public void setMaxTries(int maxTries) { m_maxTries = maxTries; }
	private int m_maxTries = MAX_TRIES_DEFAULT;

	private long getLastLoggedException() { return m_lastLoggedException; }
	private void setLastLoggedException(long lastLoggedException) { m_lastLoggedException = lastLoggedException; }
	private long m_lastLoggedException = 0L;

	//the outgoing data channel
	private transient ByteChannel m_channel;

	*/
/**
	 * @return The facility of outgoing syslog data, defaults to {@value #FACILITY_DEFAULT}
	 *//*

	public Facility getFacility() { return m_facility; }
	public void setFacility(Facility facility) { m_facility = facility; }
	private Facility m_facility = FACILITY_DEFAULT;

	*/
/**
	 * @return The severity value for the PRI of outgoing syslog, defaults to {@value #SEVERITY_DEFAULT}
	 *//*

	public Severity getSeverity() { return m_severity; }
	public void setSeverity(Severity severity) { m_severity = severity; }
	private Severity m_severity = SEVERITY_DEFAULT;

	*/
/**
	 * @return The hostname to set in the syslog header, defaults to system hostname
	 *//*

	public String getSourceHostname() { return m_sourceHostname; }
	public void setSourceHostname(String hostname) { m_sourceHostname = hostname; }
	private String m_sourceHostname;

	*/
/**
	 * @return Syslog server to send to, required
	 *//*

	public String getHost() { return m_host; }
	public void setHost(String host) { m_host = host; }
	private String m_host;

	*/
/**
	 * @return Syslog port to send to, defaults to 514
	 *//*

	public int getPort() { return m_port; }
	public void setPort(int port) { m_port = port; }
	private int m_port = PORT_DEFAULT;

	*/
/**
	 * @return Protocol to use when sending syslog
	 *//*

	public Protocol getProtocol() { return m_protocol; }
	public void setProtocol(Protocol protocol) { m_protocol = protocol; }
	private Protocol m_protocol = PROTOCOL_DEFAULT;

	*/
/**
	 * @return The fields to include in the syslog messages - if empty, all fields will be included
	 *//*

	public Set<String> getFields() { return m_fields; }
	public void setFields(Set<String> fields) { m_fields = fields; }
	private Set<String> m_fields = new HashSet<String>();

	*/
/**
	 * @return Separator for fields in the syslog, defaults to |
	 *//*

	public String getFieldDelimiter() { return m_fieldDelimiter; }
	public void setFieldDelimiter(String fieldDelimiter) { m_fieldDelimiter = fieldDelimiter; }
	private String m_fieldDelimiter = FIELD_DELIMITER_DEFAULT;

	*/
/**
	 * @return Whether we are logging syslog to system logs
	 *//*

	public boolean isLogging() { return m_logging; }
	public void setLogging(boolean logging) { m_logging = logging; }
	private boolean m_logging = false;

	*/
/**
	 * @return Delimiter between TCP messages
	 *//*

	public String getTcpDelimiter() { return m_tcpDelimiter; }
	public void setTcpDelimiter(String tcpDelimiter) { m_tcpDelimiter = tcpDelimiter; }
	private String m_tcpDelimiter = TCP_DELIMITER_DEFAULT;

}
	*/
