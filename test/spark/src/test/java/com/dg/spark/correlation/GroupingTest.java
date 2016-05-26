package com.aw.spark.correlation;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.junit.Test;

import com.aw.compute.detection.correlation.FieldCorrelationMethod;
import com.aw.compute.detection.correlation.MatchData;
import com.aw.compute.streams.processor.correlation.AbstractCorrelatedProcessor;
import com.aw.compute.streams.processor.correlation.CorrelationRuleState;
import com.aw.spark.AbstractSparkTest;
import com.aw.spark.TestDocumentHandler;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.UnityInstance;
import com.aw.unity.defaults.DefaultUnityFactory;
import com.aw.unity.dg.CommonField;
import com.aw.unity.json.DataBuilder;
import com.aw.unity.query.datatypes.TimestampInEqHandler;
import com.aw.unity.query.datatypes.TimestampInEqHandler.StartOf;
import com.aw.util.ListMap;
import com.aw.util.SetMap;
import com.aw.util.Statics;

import scala.Tuple2;

/**
 *
 * @author jlehmann
 *
 */
public class GroupingTest extends AbstractSparkTest {

	private static Logger logger = Logger.getLogger(GroupingTest.class);

	private static final List<String> TENANTS = Arrays.asList(new String[] {

		"tenant0",

	});

	private static final List<String> USERS = Arrays.asList(new String[] {

		"user0",
		"user1",
		"user2",
		"user3",
		"user4",
		"user5",
		"user6",
		"user7",
		"user8",
		"user9",

	});

	private static final String FIRING_TENANT = "tenant3";
	private static final String FIRING_USER = "user3";

	UnityInstance unity = null;

	public GroupingTest() throws Exception {
		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");
		this.unity = new DefaultUnityFactory().getInstance("1", new TestDocumentHandler());
	}

	@Test
	public void inOrderSequence() throws Exception {

		ListMap<String, Data> rddData = new ListMap<>();

		//generate data for each tenant/user combination
		for (String tenant : TENANTS) {

			for (int x=0; x<4; x++) {

				//generate events for each user
				for (String user : USERS) {
					genEvents(rddData, tenant, user);
				}

			}

		}

		//submit the streamdef for correlation

		//create rdds of max size 10 each
		Queue<JavaRDD<Tuple2<String, String>>> rdds = toRDDs(rddData, 10);

		//create the DStream
		JavaDStream<Tuple2<String, String>> dstream = this.ssContext.queueStream(rdds);

		//correlate the DStream
		TestCorrelatedProcessor proc = new TestCorrelatedProcessor();

		//let handlers notify this instance, don't rely on local references which may be serialized
		singleton = this;

		//correlated on username
		proc.setCorrelationMethod(new FieldCorrelationMethod(CommonField.DG_UN.asField()));

		//handle the stream
		proc.handle(dstream);

		this.ssContext.start();

//		for (String key : this.threadMap.keySet()) {
//			logger.info("for key: " + key + " : " + this.threadMap.get(key));
//		}

	}

	//generate events for the given tenant/user combination
	private void genEvents(ListMap<String, Data> rddData, String tenant, String user) throws Exception {

		//build a test unity instanceS
		DataType mailAttachType = unity.getDataType("user_mail_attach");
		DataType sendMailType = unity.getDataType("user_send_mail");

		//attach a file for this user
		long startOfDay = TimestampInEqHandler.getDate(StartOf.DAY).getTime();
		Data attachFile = new DataBuilder()
				    .withType(mailAttachType)
					.withField(CommonField.DG_UN.asField(), user)
					.withField(CommonField.DG_TIME.asField(), startOfDay + Duration.ofHours(1).toMillis() - 1000L)
 				    .withField(CommonField.DG_FILE_SIZE.asField(), 100000L)
					.build();

		//then send if it's the right user
		Data sendMail = new DataBuilder()
				   .withType(sendMailType)
				   .withField(CommonField.DG_UN.asField(), user)
				   .withField(CommonField.DG_TIME.asField(), startOfDay + Duration.ofHours(1).toMillis())
				   .build();

		//if this is the firing user/tenant combination, add both events
		if (tenant.equals(FIRING_TENANT) && user.equals(FIRING_USER)) {
			rddData.add(tenant, attachFile);
			rddData.add(tenant, sendMail);
		}

		//else only add one or the other for this user
		else {

			rddData.add(tenant, sendMail);

		}

	}

	private static class TestCorrelatedProcessor extends AbstractCorrelatedProcessor {

		private static final long serialVersionUID = 1L;

		@Override
		protected void updateState(Tuple2<String, String> key, CorrelationRuleState state, Data data) {
			logger.info("state for : " + key._1() + "/" + key._2() + " : " + state.getCount() + " thread=" + Thread.currentThread());
		}

		@Override
		protected void processMatch(MatchData matchData) throws Exception {
			synchronized (singleton) {
				logger.info("process match: " + matchData);
			}
		}

	}

	//map correlation fields to threads to processor instances to be sure each processor instance sees all data for each correlation value
	private SetMap<String, String> threadMap = new SetMap<>();

	private static GroupingTest singleton;

}