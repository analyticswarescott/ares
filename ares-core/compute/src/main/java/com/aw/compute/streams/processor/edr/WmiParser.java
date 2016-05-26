package com.aw.compute.streams.processor.edr;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.util.Statics;

/**
 * Parse wmi data from an edr scan
 *
 *
 *
 */
public class WmiParser implements Iterable<JSONObject>, Iterator<JSONObject> {

	static final Logger logger = Logger.getLogger(WmiParser.class);

	public static final String WMI_TYPE = "edr_wmi_type";
	public static final String WMI_ENTRY = "edr_wmi_entry";
	public static final String WMI_KEY = "edr_wmi_key";
	public static final String WMI_VAL = "edr_wmi_val";
	public static final String WMI_UNKNOWN = "unknown"; //unknown type

	//after this many errors, give up on the file
	public static final int MAX_ERRORS = 100;

	/**
	 * Parse a particular fiel from an edr scan as wmi data
	 *
	 * @param file The file to parse
	 * @param in The input stream to read
	 * @throws Exception If anything goes wrong
	 */
	public WmiParser(ArchiveEntry file, InputStream in) throws Exception {

		m_file = file;
		m_in = in;

		//don't close this or else the input stream is closed and we can't process any more files from the zip
		m_parser = new CSVParser(new InputStreamReader(m_in, Statics.UTF_16), CSVFormat.DEFAULT);
		m_iterator = m_parser.iterator();

		//store the keys which should be the first row
		if (m_iterator.hasNext()) {
			m_keys = m_iterator.next();
		}

	}

	@Override
	public Iterator<JSONObject> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {

		int errors = 0;
		do {

			try {

				return m_iterator.hasNext();

			} catch (Exception e) {

				//TODO: log this back to REST server via error reporting api
				logger.warn(m_file.getName() + ": error processing line", e);

				errors++;

			}

		} while (errors > 0 && errors < MAX_ERRORS);

		//we maxed out on errors, we're done
		return false;

	}

	/**
	 * May return null if an error occurred
	 */
	@Override
	public JSONObject next() {
		return processWmiLine();
	}

	private static final Pattern WMI_FILE_TYPE_PATTERN = Pattern.compile("_wmic_(.*)\\.csv");
	private String getWmiType() {
		Matcher matcher = WMI_FILE_TYPE_PATTERN.matcher(m_file.getName());
		if (matcher.find()) {
			return matcher.group(1).toLowerCase();
		} else {
			return WMI_UNKNOWN;
		}
	}


	private JSONObject processWmiLine() {

		try {

			Iterator<String> valueIter = m_iterator.next().iterator();

			//one wmi data object per record
			JSONObject wmiData = new JSONObject();
			wmiData.put(WMI_TYPE, getWmiType());
			JSONArray entries = new JSONArray();
			wmiData.put(WMI_ENTRY, entries);

			for (String key : m_keys) {
				if (valueIter.hasNext()) {

					//create the key/value sub object
					JSONObject keyVal = new JSONObject();
					keyVal.put(WMI_KEY, key);
					keyVal.put(WMI_VAL, valueIter.next());

					entries.put(keyVal);

				}
			}

			return wmiData;

		} catch (Exception e) {

			//TODO: log this back to REST server via error reporting api
			logger.warn(m_file.getName() + ": error processing line", e);

			//throwing this up will cause spark to slow down on something that will always fail (parsing a bad line) - we need to log the bad line information and keep going
			//throw new RuntimeException("error processing wmi line", e);
			return null;

		}

	}

	private ArchiveEntry m_file;
	private InputStream m_in;
	private CSVParser m_parser;
	private Iterator<CSVRecord> m_iterator;
	private CSVRecord m_keys;

}
