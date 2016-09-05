/*
package com.aw.tools.generators;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.http.entity.StringEntity;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.DGCommandLine;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import com.aw.util.DateUtil;

@SuppressWarnings({"static-access", "deprecation"})
public class BundleGenerator extends AbstractEventGenerator {

	private static final long MAX_BUNDLE_TIME = 300000L;

	private static Option DATE_RANGE = OptionBuilder.hasArg()
			.withDescription("number of days the data should span, ending today")
			.withArgName("days")
			.isRequired(true)
			.create("range");

	private static Option EVENTS_PER_DAY = OptionBuilder.hasArg()
			.withDescription("Events to generate per day")
			.withArgName("count")
			.isRequired(true)
			.create("events_per_day");

	private static Option AVERAGE_BUNDLE_SIZE = OptionBuilder.hasArg()
			.withDescription("Number of UADs per bundle on average")
			.withArgName("size")
			.isRequired(true)
			.create("avg_bundle_size");

	private static Option MACHINE_COUNT = OptionBuilder.hasArg()
			.withDescription("Number of machines sending the bundles")
			.withArgName("count")
			.isRequired(true)
			.create("machines");

	private static Option TENANT_ID = OptionBuilder.hasArg()
			.withDescription("The tenant id for which bundles will be generated")
			.withArgName("tenant id")
			.isRequired(true)
			.create("tenant");

	private static Option FORMAT = OptionBuilder.hasArg()
			.withDescription("")
			.withArgName("xml|json")
			.isRequired(true)
			.create("format");

	private static Option SERVER = OptionBuilder.hasArg()
			.withDescription("")
			.withArgName("host:port")
			.isRequired(true)
			.create("server");

	*/
/**
	 * Supported bundle formats
	 *//*

	public enum Format {
		XML,
		JSON
	}

	*/
/**
	 * Command line options for this tool
	 *//*

	public static class Cli extends DGCommandLine {

		*/
/**
		 * serial version UID
		 *//*

		private static final long serialVersionUID = 1L;

		public Cli() {
			super(DATE_RANGE, EVENTS_PER_DAY, AVERAGE_BUNDLE_SIZE, MACHINE_COUNT, TENANT_ID, FORMAT, SERVER);
		}

	}

	public BundleGenerator() {
		super(PlatformMgr.getCachedPlatform());
	}


	public BundleGenerator(CommandLine cli) {
		super(PlatformMgr.getCachedPlatform());

		setDateRange(Integer.parseInt(cli.getOptionValue(DATE_RANGE.getOpt())));
		setEventsPerDay(Long.parseLong(cli.getOptionValue(EVENTS_PER_DAY.getOpt())));
		setAverageBundleSize(Integer.parseInt(cli.getOptionValue(AVERAGE_BUNDLE_SIZE.getOpt())));
		setMachineCount(Integer.parseInt(cli.getOptionValue(MACHINE_COUNT.getOpt())));
		setFormat(Format.valueOf(cli.getOptionValue(FORMAT.getOpt()).toUpperCase()));
		setTenantID(cli.getOptionValue(TENANT_ID.getOpt()));
		//setServer(cli.getOptionValue(SERVER.getOpt()));
	}

	public void execute() throws Exception {

		//set up reference bundle
		m_referenceBundle = new JSONObject(REFERENCE_BUNDLE_DATA);

		//start 30 days ago
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime localStart = now.minusDays(30).with(LocalTime.MIN);
		Instant start = localStart.toInstant(ZoneOffset.UTC);
		setStart(start);

		//randomize event count per day
		long[] eventCounts = generateDailyEventCounts();
		long bundleCount = 0;
		long nextUpdate = System.currentTimeMillis() + 1000L;
		LocalDateTime curStart = localStart;
		LocalDateTime curEnd = curStart.plusDays(1);

		//generate bundles per day
		for (long dayCount : eventCounts) {

			System.out.println("generating bundles for " + curStart);

			long left = dayCount;
			while (left > 0) {

				JSONObject bundle = newBundle(curStart, curEnd, randomMachineId(m_machineCount));

				String machineID = bundle.getString("mid");
				String bundleID = bundle.getString("bid");

				String strBundle = null;
				if (m_format == Format.XML) {
					strBundle = toXml(bundle);
				} else {
					strBundle = bundle.toString(4);
				}

				//send the bundle
				getClient().postPayload(getTenantID(), PlatformClient.PayloadType.BUNDLE, machineID, bundleID, new StringEntity(strBundle));
				//post(Type.BUNDLE, getServer(), machineID, bundleID, new StringEntity(strBundle));

				//keep track of how many we've sent
				bundleCount++;

				//only send up to the amount this day gets
				left -= bundle.getJSONArray("uad").length();

				if (nextUpdate < System.currentTimeMillis()) {
					System.out.println("events left for day: " + left + "/" + dayCount + " bundle count: " + bundleCount);
					nextUpdate = System.currentTimeMillis() + 1000L;
				}

			}

			//increment time range for bundles
			curStart = curEnd;
			curEnd = curStart.plusDays(1);

		}

		System.out.println("start date: " + start);
		System.out.println("event counts: " + eventCounts);
		System.out.println("bundle count: " + bundleCount);

	}

	private long[] generateDailyEventCounts() {

		long[] ret = new long[m_dateRange];
		for (int x=0; x<m_dateRange; x++) {

			//keep within n% of the average
			long count = randomLongWithinVariance(getEventsPerDay(), getVariance());
			ret[x] = count;

		}
		return ret;

	}

	private double getVariance() {
		return .5; //for now hard code 50% event volume variability per day
	}

	//generate a random bundle
	private JSONObject newBundle(LocalDateTime start, LocalDateTime end, String machineID) throws Exception {

		JSONObject ret = new JSONObject();

		//determine UAD count
		int uadCount = randomBundleSize();

		//get a random start time
		long startTime = randomLong(start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
				                      end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1L);

		long endTime = startTime + randomLong(MAX_BUNDLE_TIME/2, MAX_BUNDLE_TIME);
		long range = endTime - startTime;
		long timeUnit = range / uadCount;

		JSONArray uads = new JSONArray();
		JSONArray pis = new JSONArray();
		JSONArray uas = new JSONArray();

		ret.put("uad", uads);
		ret.put("pi", pis);
		ret.put("ua", uas);

		Map<String, JSONObject> piData = new HashMap<String, JSONObject>();
		Map<String, JSONObject> uaData = new HashMap<String, JSONObject>();
		for (int x=0; x<uadCount; x++) {

			Instant time = Instant.ofEpochMilli(startTime + timeUnit * x);

			JSONObject uad = newUad(time);
			uads.put(uad);

			//get the ua for this uad
			String meid = uad.getString("meid");
			JSONObject ua = uaData.get(meid);
			if (ua == null) {
				ua = newUa(machineID, meid, uad, time);
				uas.put(ua);
			}

			//get the pi for this uad
			String md5 = ua.getString("md5");
			JSONObject pi = piData.get(md5);
			if (pi == null) {
				pi = newPi(ua);
				pis.put(pi);
			}

		}

		//put the bundle level properties on it
		ret.put("bid", UUID.randomUUID().toString());
		ret.put("mid", machineID);
		ret.put("tid", getTenantID());

		return ret;

	}

	private JSONObject newUad(Instant time) throws Exception {

		JSONObject uad = new JSONObject(getFirst("uad"));

		LocalDateTime localTime = LocalDateTime.from(time.atZone(ZoneId.systemDefault()));

		//set it up
		uad.put("medid", UUID.randomUUID().toString());
		uad.put("meid", UUID.randomUUID().toString());
		uad.put("ot", randomInt(0, 50));
		uad.put("un", randomUser());
		uad.put("bw", randomLong(1024, 10240) * getByteMultiplier(uad.getString("un"), localTime));

		return uad;
	}

	private long getByteMultiplier(String username, LocalDateTime localTime) {

		//if it's time for an anomaly, generate more bytes
		if (username.equals(getAnomalousUser()) &&
			localTime.getDayOfMonth() == getAnomalousDayOfMonth() &&
			localTime.getHour() == getAnomalousHourOfDay()) {
			System.out.println("anomalous bytes generated for " + username + " day=" + localTime.getDayOfMonth() + " hour=" + localTime.getHour());
			return 100L;
		} else {
			return 1L;
		}
	}

	private JSONObject newPi(JSONObject ua) throws Exception {

		JSONObject ret = new JSONObject(getFirst("pi"));

		//link the md5 from the ua to this pi
		String md5 = ua.getString("md5");
		String filename = ua.getString("in_");

		//set it up
		ret.put("md5", md5);
		ret.put("in_", filename);
		ret.put("pn", getProduct(filename));
		ret.put("cn", getCompany(getProduct(filename)));

		return ret;

	}

	private JSONObject newUa(String machineID, String meid, JSONObject uad, Instant time) throws Exception {

		JSONObject ret = new JSONObject(getFirst("ua"));

		String file = randomFile();
		String username =  uad.getString("un");

		//set it up
		ret.put("meid", meid);
		ret.put("in_", file);
		ret.put("md5", getHash(file));
		ret.put("un", username);
		ret.put("dn", getMachineName(machineID));
		ret.put("esu", DateUtil.fromUnixToMSTime(time.toEpochMilli()));

		return ret;

	}

	//generate a LocalMachine# machine name for this machine id
	private String getMachineName(String id) {
		String ret = m_machineNames.get(id);
		if (ret == null) {
			ret = "LocalMachine" + m_machineNames.size() + 1;
			m_machineNames.put(id, ret);
		}
		return ret;
	}
	private Map<String, String> m_machineNames = new HashMap<String, String>();

	//get the first json object from the array in the bundle
	private String getFirst(String strArray) throws JSONException {

		JSONArray array = m_referenceBundle.getJSONArray(strArray);
		return array.getJSONObject(0).toString();

	}

	*/
/**
	 * convert the bundle to xml
	 *
	 * @param bundle
	 * @return
	 * @throws Exception
	 *//*

	private String toXml(JSONObject bundle) throws Exception {

		throw new UnsupportedOperationException("xml bundle format not supported yet");

	}

	private int randomBundleSize() {
		return randomIntWithinVariance(getAverageBundleSize(), getVariance());
	}

	public JSONObject getReferenceBundle() { return m_referenceBundle; }
	public void setReferenceBundle(JSONObject referenceBundle) { m_referenceBundle = referenceBundle; }
	private JSONObject m_referenceBundle;

	public int getDateRange() { return m_dateRange; }
	public void setDateRange(int dateRange) { m_dateRange = dateRange; }
	private int m_dateRange;

	public Instant getStart() { return m_start; }
	public void setStart(Instant start) { m_start = start; }
	private Instant m_start;

	public long getEventsPerDay() { return m_eventsPerDay; }
	public void setEventsPerDay(long eventsPerDay) { m_eventsPerDay = eventsPerDay; }
	private long m_eventsPerDay;

	public int getAverageBundleSize() { return m_averageBundleSize; }
	public void setAverageBundleSize(int averageBundleSize) { m_averageBundleSize = averageBundleSize; }
	private int m_averageBundleSize;

	public int getMachineCount() { return m_machineCount; }
	public void setMachineCount(int machineCount) { m_machineCount = machineCount; }
	private int m_machineCount;

	public Format getFormat() { return m_format; }
	public void setFormat(Format format) { m_format = format; }
	private Format m_format;


	public static void main(String[] args) {

		Cli cli = new Cli();

		try {

			CommandLine commandLine = cli.parse(args);
			new BundleGenerator(commandLine).execute();

		} catch (ParseException e) {

			//on parameter error, output usage
			System.err.println(e.getMessage());
			cli.printUsage();

		} catch (Exception e) {

			//other exception types, print the full stack trace
			e.printStackTrace();

		}

	}

	*/
/**
{
  "tid": "48db6e86-bdf0-43b3-82a3-02b8b518bf9a",
  "pi": [
    {
      "md5": "22b48008-35de-4a80-99ec-bf913c3fe6ad",
      "cn": "microsoft corporation",
      "pn": "excel.exe",
      "pv": "9.0.8924",
      "in_": "excel.exe",
      "nda": false,
      "fp": "C:\\windows\\system32\\",
      "tid": "48db6e86-bdf0-43b3-82a3-02b8b518bf9a",
      "ts": 130927830560000000,
      "seq": 1,
      "sid": "S-1-5-21-1697404398-administrator",
      "dn": "LocalMachine9",
      "un": "administrator",
      "wh": "201548",
      "cr": 130934617257096505,
      "bid": "fbd12b04-bc12-4a5a-85c4-aa863be6341b",
      "v": 771,
      "mid": "266c45bb-7879-4c85-9e5d-a493a27d70ff",
      "fifo": 0
    }
  ],
  "ua": [
    {
      "meid": "2153fd02-30bf-4b58-ba8f-66590bd3ea1d",
      "auid": "2ba27f6b-17c9-430e-b10f-e87e4921ee01",
      "md5": "22b48008-35de-4a80-99ec-bf913c3fe6ad",
      "ph": "2c551755-b073-4a1b-82e2-59a10b822839",
      "ot": 39,
      "esu": 130927812880000000,
      "eeu": 130927813610000000,
      "esl": 130927632880000000,
      "eel": 130927633610000000,
      "pt": 0,
      "rp": 0,
      "lp": 0,
      "edt": 20151123,
      "ob": true,
      "ar": false,
      "aa": false,
      "in_": "excel.exe",
      "tfs": 0,
      "dc": 1,
      "vs": false,
      "hc": true,
      "md": true,
      "wb": false,
      "drpt": 0,
      "cf": false,
      "sc": false,
      "psu": 130927812880000000,
      "tid": "48db6e86-bdf0-43b3-82a3-02b8b518bf9a",
      "ts": 130927830560000000,
      "seq": 14,
      "sid": "S-1-5-21-1697404398-administrator",
      "dn": "LocalMachine9",
      "un": "administrator",
      "wh": "201548",
      "cr": 130934617257236556,
      "bid": "fbd12b04-bc12-4a5a-85c4-aa863be6341b",
      "v": 771,
      "mid": "266c45bb-7879-4c85-9e5d-a493a27d70ff",
      "fifo": 0
    }
  ],
  "uad": [
    {
      "medid": "f09d5876-2a03-4c72-a5b2-2fe707fb79cf",
      "meid": "2153fd02-30bf-4b58-ba8f-66590bd3ea1d",
      "ot": 0,
      "sfn": "",
      "sfe": "",
      "sfd": "",
      "svn": 0,
      "sdt": 0,
      "sir": false,
      "dfn": "ExpenseReport2005_12_30.xls",
      "dfe": ".xls",
      "dfd": "",
      "dvn": 0,
      "br": 0,
      "ddt": 4,
      "dir": false,
      "bw": 0,
      "fs": 0,
      "edt": 20151123,
      "sea": 0,
      "dea": 0,
      "sfc": false,
      "dfc": true,
      "dfi": "b528777e-01d4-4c96-bdc1-83c031ae43aa",
      "wb": false,
      "bc": 0,
      "ss": 0,
      "scf": false,
      "dcf": false,
      "sc": false,
      "tid": "48db6e86-bdf0-43b3-82a3-02b8b518bf9a",
      "ts": 130927830560000000,
      "seq": 15,
      "wh": "201548",
      "cr": 130934617257246528,
      "bid": "fbd12b04-bc12-4a5a-85c4-aa863be6341b",
      "v": 771,
      "mid": "266c45bb-7879-4c85-9e5d-a493a27d70ff",
      "fifo": 0
    }
  ],
  "cad": [
    {
      "cedid": "f09d5876-2a03-4c72-a5b2-2fe707fb79cf",
      "medid": "f09d5876-2a03-4c72-a5b2-2fe707fb79cf",
      "meid": "2153fd02-30bf-4b58-ba8f-66590bd3ea1d",
      "ct": 0,
      "ci": "a097b9a8-a7a6-4715-92b6-89858a3b0900",
      "fid": "b528777e-01d4-4c96-bdc1-83c031ae43aa",
      "tc": 0,
      "bid": "fbd12b04-bc12-4a5a-85c4-aa863be6341b",
      "v": 771,
      "mid": "266c45bb-7879-4c85-9e5d-a493a27d70ff",
      "fifo": 0
    }
  ],
  "bid": "fbd12b04-bc12-4a5a-85c4-aa863be6341b",
  "mid": "266c45bb-7879-4c85-9e5d-a493a27d70ff"
}
	 *
	 *//*

	protected static final String REFERENCE_BUNDLE_DATA =
			"{\n" +
			"  \"tid\": \"48db6e86-bdf0-43b3-82a3-02b8b518bf9a\",\n" +
			"  \"pi\": [\n" +
			"    {\n" +
			"      \"md5\": \"22b48008-35de-4a80-99ec-bf913c3fe6ad\",\n" +
			"      \"cn\": \"microsoft corporation\",\n" +
			"      \"pn\": \"excel.exe\",\n" +
			"      \"pv\": \"9.0.8924\",\n" +
			"      \"in_\": \"excel.exe\",\n" +
			"      \"nda\": false,\n" +
			"      \"fp\": \"C:\\\\windows\\\\system32\\\\\",\n" +
			"      \"tid\": \"48db6e86-bdf0-43b3-82a3-02b8b518bf9a\",\n" +
			"      \"ts\": 130927830560000000,\n" +
			"      \"seq\": 1,\n" +
			"      \"sid\": \"S-1-5-21-1697404398-administrator\",\n" +
			"      \"dn\": \"LocalMachine9\",\n" +
			"      \"un\": \"administrator\",\n" +
			"      \"wh\": \"201548\",\n" +
			"      \"cr\": 130934617257096505,\n" +
			"      \"bid\": \"fbd12b04-bc12-4a5a-85c4-aa863be6341b\",\n" +
			"      \"v\": 771,\n" +
			"      \"mid\": \"266c45bb-7879-4c85-9e5d-a493a27d70ff\",\n" +
			"      \"fifo\": 0\n" +
			"    }\n" +
			"  ],\n" +
			"  \"ua\": [\n" +
			"    {\n" +
			"      \"meid\": \"2153fd02-30bf-4b58-ba8f-66590bd3ea1d\",\n" +
			"      \"auid\": \"2ba27f6b-17c9-430e-b10f-e87e4921ee01\",\n" +
			"      \"md5\": \"22b48008-35de-4a80-99ec-bf913c3fe6ad\",\n" +
			"      \"ph\": \"2c551755-b073-4a1b-82e2-59a10b822839\",\n" +
			"      \"ot\": 39,\n" +
			"      \"esu\": 130927812880000000,\n" +
			"      \"eeu\": 130927813610000000,\n" +
			"      \"esl\": 130927632880000000,\n" +
			"      \"eel\": 130927633610000000,\n" +
			"      \"pt\": 0,\n" +
			"      \"rp\": 0,\n" +
			"      \"lp\": 0,\n" +
			"      \"edt\": 20151123,\n" +
			"      \"ob\": true,\n" +
			"      \"ar\": false,\n" +
			"      \"aa\": false,\n" +
			"      \"in_\": \"excel.exe\",\n" +
			"      \"tfs\": 0,\n" +
			"      \"dc\": 1,\n" +
			"      \"vs\": false,\n" +
			"      \"hc\": true,\n" +
			"      \"md\": true,\n" +
			"      \"wb\": false,\n" +
			"      \"drpt\": 0,\n" +
			"      \"cf\": false,\n" +
			"      \"sc\": false,\n" +
			"      \"psu\": 130927812880000000,\n" +
			"      \"tid\": \"48db6e86-bdf0-43b3-82a3-02b8b518bf9a\",\n" +
			"      \"ts\": 130927830560000000,\n" +
			"      \"seq\": 14,\n" +
			"      \"sid\": \"S-1-5-21-1697404398-administrator\",\n" +
			"      \"dn\": \"LocalMachine9\",\n" +
			"      \"un\": \"administrator\",\n" +
			"      \"wh\": \"201548\",\n" +
			"      \"cr\": 130934617257236556,\n" +
			"      \"bid\": \"fbd12b04-bc12-4a5a-85c4-aa863be6341b\",\n" +
			"      \"v\": 771,\n" +
			"      \"mid\": \"266c45bb-7879-4c85-9e5d-a493a27d70ff\",\n" +
			"      \"fifo\": 0\n" +
			"    }\n" +
			"  ],\n" +
			"  \"uad\": [\n" +
			"    {\n" +
			"      \"medid\": \"f09d5876-2a03-4c72-a5b2-2fe707fb79cf\",\n" +
			"      \"meid\": \"2153fd02-30bf-4b58-ba8f-66590bd3ea1d\",\n" +
			"      \"ot\": 0,\n" +
			"      \"sfn\": \"\",\n" +
			"      \"sfe\": \"\",\n" +
			"      \"sfd\": \"\",\n" +
			"      \"svn\": 0,\n" +
			"      \"sdt\": 0,\n" +
			"      \"sir\": false,\n" +
			"      \"dfn\": \"ExpenseReport2005_12_30.xls\",\n" +
			"      \"dfe\": \".xls\",\n" +
			"      \"dfd\": \"\",\n" +
			"      \"dvn\": 0,\n" +
			"      \"br\": 0,\n" +
			"      \"ddt\": 4,\n" +
			"      \"dir\": false,\n" +
			"      \"bw\": 0,\n" +
			"      \"fs\": 0,\n" +
			"      \"edt\": 20151123,\n" +
			"      \"sea\": 0,\n" +
			"      \"dea\": 0,\n" +
			"      \"sfc\": false,\n" +
			"      \"dfc\": true,\n" +
			"      \"dfi\": \"b528777e-01d4-4c96-bdc1-83c031ae43aa\",\n" +
			"      \"wb\": false,\n" +
			"      \"bc\": 0,\n" +
			"      \"ss\": 0,\n" +
			"      \"scf\": false,\n" +
			"      \"dcf\": false,\n" +
			"      \"sc\": false,\n" +
			"      \"tid\": \"48db6e86-bdf0-43b3-82a3-02b8b518bf9a\",\n" +
			"      \"ts\": 130927830560000000,\n" +
			"      \"seq\": 15,\n" +
			"      \"wh\": \"201548\",\n" +
			"      \"cr\": 130934617257246528,\n" +
			"      \"bid\": \"fbd12b04-bc12-4a5a-85c4-aa863be6341b\",\n" +
			"      \"v\": 771,\n" +
			"      \"mid\": \"266c45bb-7879-4c85-9e5d-a493a27d70ff\",\n" +
			"      \"fifo\": 0\n" +
			"    }\n" +
			"  ],\n" +
			"  \"cad\": [\n" +
			"    {\n" +
			"      \"cedid\": \"f09d5876-2a03-4c72-a5b2-2fe707fb79cf\",\n" +
			"      \"medid\": \"f09d5876-2a03-4c72-a5b2-2fe707fb79cf\",\n" +
			"      \"meid\": \"2153fd02-30bf-4b58-ba8f-66590bd3ea1d\",\n" +
			"      \"ct\": 0,\n" +
			"      \"ci\": \"a097b9a8-a7a6-4715-92b6-89858a3b0900\",\n" +
			"      \"fid\": \"b528777e-01d4-4c96-bdc1-83c031ae43aa\",\n" +
			"      \"tc\": 0,\n" +
			"      \"bid\": \"fbd12b04-bc12-4a5a-85c4-aa863be6341b\",\n" +
			"      \"v\": 771,\n" +
			"      \"mid\": \"266c45bb-7879-4c85-9e5d-a493a27d70ff\",\n" +
			"      \"fifo\": 0\n" +
			"    }\n" +
			"  ],\n" +
			"  \"bid\": \"fbd12b04-bc12-4a5a-85c4-aa863be6341b\",\n" +
			"  \"mid\": \"266c45bb-7879-4c85-9e5d-a493a27d70ff\"\n" +
			"}";
}
*/
