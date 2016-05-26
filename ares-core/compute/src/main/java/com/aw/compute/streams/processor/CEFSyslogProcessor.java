package com.aw.compute.streams.processor;
/*
package com.aw.compute.streams.processor.impl;

import java.util.Iterator;

public class CEFSyslogProcessor extends SyslogProcessor {

	private static final long serialVersionUID = 1L;

	private static final String CEF_VERSION = "0";
	private static final String DEVICE_VENDOR = "Digital Guardian";
	private static final String DEVICE_PRODUCT = "Reporter";
	private static final String DEVICE_VERSION = "8.0";

	private static final String CEF_FIELD_DELIMITER = "|";

	public CEFSyslogProcessor (){

	}

//	@Override
//	protected byte[] buildPacket(org.codehaus.jettison.json.JSONObject json) throws Exception {
//
//		//simply build a syslog message of name/value pairs
//		StringBuilder syslog = new StringBuilder();
//
//		//add the header
//		appendHeader(syslog);
//
//		//flatten to a string
//		appendKeys(syslog, "", json);
//
//		//convert to utf-8 to send
//
//		return syslog.toString().getBytes(Charset.forName("UTF-8"));
//
//	}

	@Override
	*/
/**
	 * Build CEF header in the following format
	 *       Jan 18 11:07:53 host CEF:Version|Device Vendor|Device Product|Device Version|Signature ID|Name                      |Severity|[Extension]
	 *       Sep 19 08:26:10 host CEF:0      |Security     |threatmanager |1.0           |100         |worm successfully stopped|10       |src=10.0.0.1 dst=2.1.2.2 spt=1232
	 *
	 *		The CEF:Version portion of the message is a mandatory header. The remainder of the
	 *		message is formatted using fields delimited by a pipe ("|") character. All of these remaining
	 *		fields should be present and are defined under â€œHeader Field Definitionsâ€� on page 6".
	 *		The extension portion of the message is a placeholder for additional fields, but is not
	 *		mandatory. Any additional fields are logged as key-value pairs.
	 *
	 *
	 *//*

	protected void appendHeader(StringBuilder syslog) throws Exception {
		//build timestamp - use single digit day if applicable
		appendTimeAndHost(syslog);

		syslog.append(" CEF:" + CEF_VERSION + CEF_FIELD_DELIMITER);
		syslog.append(DEVICE_VENDOR + CEF_FIELD_DELIMITER);
		syslog.append(DEVICE_PRODUCT + CEF_FIELD_DELIMITER);
		syslog.append(DEVICE_VERSION + CEF_FIELD_DELIMITER);

		// Signature ID
		syslog.append("SignatureID" + CEF_FIELD_DELIMITER);
		syslog.append("EventName" + CEF_FIELD_DELIMITER);
		syslog.append("EventSeverity" + CEF_FIELD_DELIMITER);
	}


	@Override
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

}
*/
