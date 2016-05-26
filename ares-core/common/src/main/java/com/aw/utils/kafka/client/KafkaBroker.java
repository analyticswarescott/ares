package com.aw.utils.kafka.client;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper Class for Broker defintion (host + port)
 * @author bartonneil
 *
 */
public class KafkaBroker {

	private final String _host;
	private final int _port;


	public KafkaBroker(String host, int port) {
		_host = host;
		_port = port;
	}


	/**
	 * Broker Host
	 * @return
	 */
	public String getHost() { return _host; }


	/**
	 * Broker Port
	 * @return
	 */
	public int getPort() { return _port; }



	/**
	 * Parses list of brokers, where expected format is host:port,host:port
	 * @param brokerList
	 * @return
	 * @throws Exception
	 */
	public static List<KafkaBroker> parseBrokerList(String brokerList) throws Exception {
		List<KafkaBroker> brokers = new ArrayList<KafkaBroker>();

		String[] brokerports = brokerList.split(",");
		for (int i = 0; i < brokerports.length; i++) {
			// split each into host and port
			String[] s = brokerports[i].split(":");

			brokers.add(new KafkaBroker(s[0], Integer.parseInt(s[1])));
		}

		return brokers;
	}


}
