package com.aw.compute.streams.processor.edr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse windows event logs into edr json documents
 *
 *
 *
 */
public class WindowsLogParser extends DefaultHandler implements Iterable<JSONObject>, Iterator<JSONObject> {

	private static final String TAG_DATA = "Data";
	private static final String TAG_PROVIDER = "Provider";

	private static final String ATTRIB_NAME = "Name";
	private static final String ATTRIB_GUID = "Guid";

	//unity properties
	private static final String EDR_WIN_DATA = "edr_win_entry";
	private static final String EDR_PREFIX = "edr_win_";
	private static final String EDR_DATA_KEY = "edr_win_key";
	private static final String EDR_DATA_VAL = "edr_win_val";

	public WindowsLogParser(ArchiveEntry file, InputStream in) throws Exception {

		m_file = file;
		m_in = in;

		m_reader = new BufferedReader(new InputStreamReader(in));
		m_done = false;

		//prime the parser
		nextNonEmptyLine();

	}

	private void nextNonEmptyLine() {

		//when we're done with the stream this becomes a no-op
		if (m_done) {
			return;
		}

		try {

			//get the next non-empty line
			String next = m_reader.readLine();
			while (next != null) {

				//trim it and check it
				next = next.trim();

				//if it has some data, we're done
				if (next.length() > 0) {
					break;
				}

				//else if empty, continue
				else {
					next = m_reader.readLine();
				}

			}

			//update our line and done status
			m_line = next;
			m_done = m_line == null;

		} catch (Exception e) {
			throw new RuntimeException("error processing xml from event log " + m_file.getName());
		}

	}

	@Override
	public Iterator<JSONObject> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return !m_done;
	}

	@Override
	public JSONObject next() {

		//get the current object
		JSONObject ret = processLine();

		//move to the next line or finish
		nextNonEmptyLine();

		//return the one we have
		return ret;

	}

	private JSONObject processLine() {

		try {

			//create the new json object
			m_json = new JSONObject();

			//add the data subobject
			m_jsonData = new JSONArray();
			m_json.put(EDR_WIN_DATA, m_jsonData);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new StringReader(m_line)), this);

			return m_json;

		} catch (Exception e) {
			throw new RuntimeException(m_file.getName() + ": error processing event log", e);
		}

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		try {

			//start of a tag always resets key/value pair
			m_curKey = null;
			m_curValue.setLength(0);
			m_data = false;

			//save the data name for the key/value pair
			if (qName.equals(TAG_DATA)) {
				m_data = true;
				m_curKey = attributes.getValue(ATTRIB_NAME);
			}

			//we can store the two key/value pairs for provider right now
			else if (qName.equals(TAG_PROVIDER)) {

				String provider = attributes.getValue(ATTRIB_NAME);
				m_json.put(EDR_PREFIX + ATTRIB_NAME.toLowerCase(), provider);
				String guid = attributes.getValue(ATTRIB_GUID);
				m_json.put(EDR_PREFIX + ATTRIB_GUID.toLowerCase(), guid);

			}

			//else save the tag name as the key, the value will be the content of the element
			else {
				m_curKey = qName;
			}

		} catch (Exception e) {
			throw new  SAXException(m_file.getName() + ": error processing tag " + qName, e);
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		m_curValue.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		try {

			String value = m_curValue.toString().trim();

			//if we have some content for this tag, add a property
			if (value.length() > 0) {

				//if it's a data field, add it to the data array
				if (m_data) {
					JSONObject data = new JSONObject();
					data.put(EDR_DATA_KEY, m_curKey);
					data.put(EDR_DATA_VAL, value);
					m_jsonData.put(data);
				}

				//otherwise it's a top level unity field for event log data
				else {
					m_json.put(EDR_PREFIX + m_curKey.toLowerCase(), value);
				}

			}

		} catch (Exception e) {
			throw new SAXException(m_file.getName() + ": error processing " + m_curKey + "=" + m_curValue);
		}

	}

	/**
	 * The input stream from which we will read the log data
	 */
	private InputStream m_in;

	/**
	 * The archive file from the edr zip holding this log data
	 */
	private ArchiveEntry m_file;

	/**
	 * The reader we created to read each line of text
	 */
	private BufferedReader m_reader;

	/**
	 * The current non-empty line if we are not done
	 */
	private String m_line;

	/**
	 * If true, we finished the log
	 */
	private boolean m_done;

	/**
	 * The current json object being built
	 */
	private JSONObject m_json;

	/**
	 * The data name/value pairs for the current json object
	 */
	private JSONArray m_jsonData;

	/**
	 * The current key while processing a log line
	 */
	private String m_curKey;

	/**
	 * The current value while processing a log lien
	 */
	private StringBuilder m_curValue = new StringBuilder();

	/**
	 * Is the current key/value pair a data key/value pair? if so it will be added as a sub object in the data array
	 */
	private boolean m_data;

}
