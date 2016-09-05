/*
package com.aw.tools.generators;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.platform.PlatformMgr;

*/
/**
 * Generates reasonable elasticsearch data for the last 30 days
 *
 *
 *
 *//*

public class ESGenerator extends AbstractEventGenerator {

	//{ "index" : { "_index" : "test", "_type" : "type1", "_id" : "1" } }
	private static final String INDEX = "{ \"index\" : { \"_type\" : \"${type}\" } }";

	private static final String LOGIN = "{ \"dg_utype\":\"user_action_logon\", \"ua_ot\" : 23, \"ua_esu\" : \"2015-09-01 08:00:00.000 -0500\", \"ua_un\" : \"${username}\" }";
	private static final String COPY = "{ \"dg_utype\":\"user_net_transfer_download\", \"ua_ot\" :  2, \"ua_esu\" : \"2015-09-01 09:35:00.000 -0500\", \"ua_un\" : \"${username}\", \"ua_in\" : \"${filename}\", \"ua_fs\" : 12000 }";
	private static final String LOGOUT = "{ \"dg_utype\":\"user_action_logoff\", \"ua_ot\" : 24, \"ua_esu\" : \"2015-09-01 09:40:00.000 -0500\", \"ua_un\" : \"${username}\" }";

	private static final int MAX_SESSION_SIZE = 100;

	private static final int DAYS = 30;

	//maximum session length in milliseconds - 60 minutes seems reasonable
	private static final long MAX_SESSION_LENGTH = DateUtils.MILLIS_PER_HOUR;

	*/
/**
	 * Build a generator for the cached platform
	 *//*

	public ESGenerator() {
		this(true);
	}

	*/
/**
	 * @param includeEsDirectives whether to include elasticsearch { "index" ... } directives between unity data lines
	 *//*

	public ESGenerator(boolean includeEsDirectives) {
		super(PlatformMgr.getCachedPlatform());
		this.includeEsDirectives = includeEsDirectives;
	}

	private File m_file = null;
	private PrintWriter m_output = null;
	public void generate(int eventCount, File file) throws Exception {

		//to store our output
		m_file = file;
		m_output = new PrintWriter(m_file);

		int generated = 0;

		initialize();

		while (generated < eventCount) {

			int curSessionSize = (int)Math.round(Math.random() * MAX_SESSION_SIZE);
			generateSession(curSessionSize);
			generated += curSessionSize;

		}

		close();

	}

	*/
/**
	 * Close our writers after we're done generating data
	 *
	 * @throws Exception
	 *//*

	private void close() throws Exception {

		if (m_output == null) {
			return;
		}

		m_output.close();

	}

	private void initialize() throws Exception {
		login = new JSONObject(LOGIN);
		actions = new JSONObject[] { new JSONObject(COPY) };
		logout = new JSONObject(LOGOUT);
	}

	private void generateSession(int eventCount) throws Exception {

		//get a user for this session
		String user = randomUser();

		Date randomStartTime = randomStartTime();
		long length = Math.round(Math.random() * MAX_SESSION_LENGTH);

		//login
		login.put("ua_un", user);
		login.put("ua_esu", JSONUtils.JSON_DATE_FORMAT.format(randomStartTime));
		output("user_action_logon", login);

		for (int x=0; x<eventCount-2; x++) {

			long offset = Math.round(length * ((double)x / (double)eventCount));

			String file = randomFile();
			JSONObject action = random(actions);
			String product = getProduct(file);
			String hash = getHash(file);

			action.put("ua_un", user);
			action.put("ua_in", file);
			action.put("ua_esu", new Date(randomStartTime.getTime() + offset).getTime());
			action.put("pi_pn", product);
			action.put("pi_MD5", hash);
			action.put("ua_md5", hash);

			output("user_file_copy", action);

		}

		//logout
		logout.put("ua_un", user);
		logout.put("ua_esu", new Date(randomStartTime.getTime() + length).getTime());
		output("user_action_logoff", logout);

	}

	private void output(String dataType, JSONObject line) throws Exception {

		//get the output writer, if not there initialize a new one for this type
		if (includeEsDirectives) {
			m_output.println(INDEX.replace("${type}", dataType));
		}
		m_output.println(line.toString());

	}

	private Date randomStartTime() {

		//latest possible time
		long start = System.currentTimeMillis() - MAX_SESSION_LENGTH;

		//now subtract a random amount
		start -= Math.random() * ((DateUtils.MILLIS_PER_DAY * DAYS) - DateUtils.MILLIS_PER_HOUR);

		return new Date(start);

	}

	private JSONObject login;
	private JSONObject[] actions;
	private JSONObject logout;

	private boolean includeEsDirectives; //i.e. no elasticsearch indexing lines

	public static void main(String[] args) {

		try {

			File file = null;
			System.out.println("args: " + Arrays.toString(args));
			if (args.length == 2) {
				file = new File(args[1]);
			} else {
				file = File.createTempFile("es_data_", ".json");
			}
			new ESGenerator().generate(Integer.parseInt(args[0]), file);
			System.out.println("generated output written to " + file);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
*/
