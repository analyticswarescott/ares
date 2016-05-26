package com.aw.compute.streams.processor.framework;

import java.io.Serializable;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.util.Statics;

import scala.Tuple2;

/**
 * Processes data across all tenants. This means each piece of data coming in is scoped to a tenant via its key. Splitting
 * the function from the processor like this decouples us from spark.
 *
 * TODO: move this to a spark package
 *
 *
 *
 */
public class ScalarProcessorFunction extends TupleProcessorFunction implements Serializable, Dependent  {

	public static final Logger logger = LoggerFactory.getLogger(ScalarProcessorFunction.class);
	private static final long serialVersionUID = 1L;

	public ScalarProcessorFunction() {
		this(null);
	}

	public ScalarProcessorFunction(Processor processor) {
		super(processor);
		m_forEach = new ForEachFunction();
	}

	@Override
	public Void call(JavaRDD<Tuple2<String, String>> tuple2JavaRDD) throws Exception {

		try {

			if (tuple2JavaRDD.isEmpty()) {
				logger.trace("ignoring empty RDD in processor " + getCheckpointId());
				return null;
			}

			storeOffsets(tuple2JavaRDD);

			SecurityUtil.setThreadSystemAccess();

			JavaPairRDD<String, String> pairs = tuple2JavaRDD.mapToPair(new Pair());

			//TODO: dynamically determine parallelism based on Spark cluster characteristics
			JavaPairRDD<String, String> parted =  pairs.repartition(8);

			//the processor wasn't getting set on the inner instance being sent around the cluster for whatever reason
			m_forEach.m_processor = (StringTupleProcessor)getProcessor();
			parted.foreach(m_forEach);

			writeOffsetsOnSuccess(getZk(), getDependency(Platform.class));


		} catch (Exception e) {
			getDependency(PlatformMgr.class).handleException(e, true, NodeRole.SPARK_WORKER);
		}

		//TODO: make sure this return is right
		return null;
	}

	private ForEachFunction m_forEach;

	private class Pair implements PairFunction<Tuple2<String, String>, String, String> {

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<String, String> call(Tuple2<String, String> stringStringTuple2) throws Exception {
			return stringStringTuple2;
		}
	}

	private static class ForEachFunction implements VoidFunction<Tuple2<String, String>>, Dependent {

		private static final long serialVersionUID = 1L;

		private StringTupleProcessor m_processor;

		@Override
		public void call(Tuple2<String, String> t) throws Exception {
			try {

				//TODO: deal with tenantID in the spawned workers if needed
				//make sure we are the right tenant
				SecurityUtil.setThreadSystemAccess();
				Impersonation.impersonateTenant(t._1());

				//TODO: do this somewhere else - this is needed to be sure unity
				//doesn't load any missing elasticsearch libraries
				System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

				//process the spark data
				m_processor.process(t._2);

			} catch (StreamProcessingException e) {

				//handle the exception in the DG platform and rethrow to spark
				getDependency(PlatformMgr.class).handleException(e, true, NodeRole.SPARK_WORKER);

			}
		}
	}
}
