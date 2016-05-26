package com.aw.compute.streams.processor.correlation;

import java.io.Serializable;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.StreamDef;
import com.aw.compute.detection.correlation.CorrelationMethod;
import com.aw.compute.detection.correlation.MatchData;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.processor.framework.AbstractStreamHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.google.common.base.Optional;

import scala.Tuple2;

/**
 * Look for a sequence of events within a time range. The topic has a stream of matches for
 *
 *
 */
public abstract class AbstractCorrelatedProcessor extends AbstractStreamHandler implements Serializable, Dependent {

	private static final long serialVersionUID = 1L;

	public static final String KEY_DELIMITER = "|";

	public AbstractCorrelatedProcessor() {
	}

	@Override
	public void init(StreamDef data) throws InitializationException {
	}

	/**
	 * Handle the original DStream from spark
	 */
	@Override
	public void handle(JavaDStream<Tuple2<String, String>> stream) {

		//create a pair stream String->String
		JavaPairDStream<Tuple2<String, String>, String> pairStream = stream.mapToPair(new Pair());

		//correlate
		JavaMapWithStateDStream<Tuple2<String, String>, String, CorrelationRuleState, MatchData> stateStream = pairStream.mapWithState(StateSpec.function(new StateFunction()));

		//then process matches
		stateStream.foreachRDD(new FiringFunctionRDD());

	}

	/**
	 * For each match generated, process it
	 */
	private class FiringFunctionRDD implements VoidFunction<JavaRDD<MatchData>> {

		private static final long serialVersionUID = 1L;

		@Override
		public void call(JavaRDD<MatchData> t) throws Exception {

			t.foreach(new FiringFunction());

		}

	}

	/**
	 *
	 *
	 *
	 */
	private class FiringFunction implements VoidFunction<MatchData> {

		private static final long serialVersionUID = 1L;

		@Override
		public void call(MatchData t) throws Exception {

			if (t != null) {

				processMatch(t);

			}

		}

	}

	//TODO: make MatchData == firing object of some kind
	private class StateFunction implements Function3<Tuple2<String, String>, Optional<String>, State<CorrelationRuleState>, MatchData>  {

		private static final long serialVersionUID = 1L;

		@Override
		public MatchData call(Tuple2<String, String> key, Optional<String> value, State<CorrelationRuleState> state) throws Exception {

			//short circuit if there is no data
			if (!value.isPresent()) {
				return null;
			}

			//build data
			Data data = getDependency(UnityInstance.class).newData(value.get());

			CorrelationRuleState curState = null;

			if (!state.exists()) {
				curState = new CorrelationRuleState();
				curState.setUser(key._2());
			}

			else {
				curState = state.get();
			}

			//update state with the data
			updateState(key, curState, data);

			//update the state
			state.update(curState);

			//TODO: don't need to return anything right?
			return null;
		}

	}

	protected void updateState(Tuple2<String, String> key, CorrelationRuleState state, Data data) {

		//process data in this rule's state
		state.process(data);

	}

	/**
	 * Process correlated data. The data being passed in will be grouped based on the correlation method of
	 * this stream processor.
	 *
	 * @param data
	 * @throws Exception
	 */
	protected abstract void processMatch(MatchData matchData) throws Exception;

	private Tuple2<Tuple2<String, String>, String> toGroupedTuple(Tuple2<String, String> rawData) throws Exception {

		try {

			//ensure system identity in this thread
			SecurityUtil.setThreadSystemAccess();

			//get unity data from raw data
			Data data = getDependency(UnityInstance.class).newData(rawData._2());

			//build the key for this data
			String fieldKey = getCorrelationMethod().toKey(data);

			//create the final key
			Tuple2<String, String> key = new Tuple2<String, String>(rawData._1(), fieldKey);

			Tuple2<Tuple2<String, String>, String> ret = new Tuple2<Tuple2<String,String>, String>(key, data.toJsonString(false, true, false));

			return ret;

		} catch (Exception e) {

			//handle and then bubble up through spark
			getDependency(PlatformMgr.class).handleException(e, true, NodeRole.SPARK_WORKER);

			//shouldn't happen as we told the platform to throw, but just in case..
			throw new Exception(e.getMessage());

		}

	}

	private class Pair implements PairFunction<Tuple2<String, String>, Tuple2<String, String>, String> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<Tuple2<String, String>, String> call(Tuple2<String, String> t) throws Exception {

			return toGroupedTuple(t);
		}

	}

	/**
	 * @return The method of correlation used by this correlation processor - this will dictate how the data is partitioned
	 */
	public CorrelationMethod getCorrelationMethod() { return m_correlationMethod; }
	public void setCorrelationMethod(CorrelationMethod correlationMethod) { m_correlationMethod = correlationMethod; }
	private CorrelationMethod m_correlationMethod;

}
