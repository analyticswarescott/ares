package com.aw.alarm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.aw.common.system.EnvironmentSettings;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

public class SMSManager {
	private static final Logger logger = LoggerFactory.getLogger(SMSManager.class);

	String _AccountSID = null;
	String _AuthToken = null;
	String _FromAddress=null;
	String _ruleFileFullPath=null;

	TwilioRestClient _client;


	public static void main(String[] args) throws  Exception {

		Properties properties = new Properties();

		SMSManager foober = new SMSManager("", properties);

		foober.startup();

		foober.sendMessage("+31681447532", " hg-ares test from The Netherlands ");

		foober.shutdown();
	}

	public SMSManager(String configDir, Properties props) throws  Exception{

		_AccountSID=props.getProperty("sms.sid");//props.getProperty("webapp.dir")+ File.separator + "video";
		_AuthToken=props.getProperty("sms.token");//props.getProperty("webapp.dir")+ File.separator + "video";
		_FromAddress=props.getProperty("sms.from");//props.getProperty("webapp.dir")+ File.separator + "video";
		_ruleFileFullPath=props.getProperty("sms.rulefile");

		if(_AccountSID==null||_AuthToken==null||_FromAddress==null){

			File f = new File(EnvironmentSettings.getConfDirectory() + File.separator + "sms.json");

			JSONObject sms = new JSONObject(FileUtils.readFileToString(f));

			_AccountSID=sms.getString("AccountSID");
			_AuthToken=sms.getString("AuthToken");
			_FromAddress=sms.getString("FromAddress");
		}

		logger.debug("DEBUG:_ruleFileFullPath"+_ruleFileFullPath);


	}

	public void startup() throws Exception {
		_client = new TwilioRestClient(_AccountSID, _AuthToken);
	}
	public void shutdown() {

	}

	public void sendMessage(String to, String msg)  {
		try {
			logger.debug("Attempting to send message: " + to + " - " + msg);
			//TwilioRestClient client = new TwilioRestClient(_AccountSID, _AuthToken);

			// Build the parameters
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("To", to));
			params.add(new BasicNameValuePair("From", _FromAddress));
			params.add(new BasicNameValuePair("Body", msg));

			MessageFactory messageFactory = _client.getAccount().getMessageFactory();
			Message message = messageFactory.create(params);

			logger.debug(message.getSid());
		} catch (Exception e) {
			//logger.debug("Error sending SMS Message via Twilio", e); error thrown for each unsub. user
			logger.debug("Error sending SMS Message via Twilio: " + e.getMessage());
		}
	}
	public void sendMessage(String from, String to, String msg)  {
		try {
			logger.debug("Attempting to send message: " + to + " - " + msg);
			//TwilioRestClient client = new TwilioRestClient(_AccountSID, _AuthToken);

			// Build the parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("To", to));
			params.add(new BasicNameValuePair("From", from));
			params.add(new BasicNameValuePair("Body", msg));

			MessageFactory messageFactory = _client.getAccount().getMessageFactory();
			Message message = messageFactory.create(params);

			logger.debug(message.getSid());
		} catch (Exception e) {
			//logger.debug("Error sending SMS Message via Twilio", e); error thrown for each unsub. user
			logger.debug("Error sending SMS Message via Twilio: " + e.getMessage());
		}
	}

	public void sendMessage(String msg,String from, List<String> toList)  {
		try {

			//TwilioRestClient client = new TwilioRestClient(_AccountSID, _AuthToken);
			MessageFactory messageFactory = _client.getAccount().getMessageFactory();

			for(String to : toList){

				logger.debug("Attempting to send message: " + to + " - " + msg);

				// Build the parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("To", to));
				params.add(new BasicNameValuePair("From", from));
				params.add(new BasicNameValuePair("Body", msg));
				Message message = messageFactory.create(params);


				logger.debug(message.getSid());

			}

		} catch (Exception e) {
			//logger.debug("Error sending SMS Message via Twilio", e); error thrown for each unsub. user
			logger.debug("Error sending SMS Message via Twilio: " + e.getMessage());
		}
	}





}

