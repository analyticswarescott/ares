package com.aw.compute.streams.processor.framework;

import java.io.Serializable;

import com.aw.platform.PlatformMgr;
import com.aw.utils.kafka.Offset;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.drivers.KafkaDriver;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;
import com.aw.util.Statics;
import scala.Tuple2;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Processes data across all tenants. This means each piece of data coming in is scoped to a tenant via its key. Splitting
 * the function from the processor like this decouples us from spark.
 *
 *
 *
 */
public class GroupedProcessorFunction extends TupleProcessorFunction implements Serializable, Dependent  {

	public static final Logger logger = LoggerFactory.getLogger(GroupedProcessorFunction.class);
	private static final long serialVersionUID = 1L;

	public GroupedProcessorFunction() {
		this(null);
	}

	public GroupedProcessorFunction(Processor processor) {
		super(processor);
		m_forEach = new ForEachFunction();
	}

	@Override
	public Void call(JavaRDD<Tuple2<String, String>> tuple2JavaRDD) throws Exception {

		try {

			if (tuple2JavaRDD.isEmpty()) {
				logger.trace("ignoring empty RDD in function " + getCheckpointId());
				return null;
			}
			storeOffsets(tuple2JavaRDD);

			SecurityUtil.setThreadSystemAccess();

			long count = 0;
			for (Offset o : getOffsetRanges()) {
				count = count + o.getCount();
			}
			logger.debug("record count for checkpoint " + getCheckpointId() + ": " + count);


			//compute number of additional keys to prepend to default key (tenant id)

			long parallelism = 1;
			PairFunction pf = null;
			if (!isGlobal()) {
				GroupedPairFunction gpf = new GroupedPairFunction();
				//TODO: improve if needed for greater control over parallelism
				parallelism = computeParallelism(count);
				gpf.targetParallelism =  parallelism;
				pf = gpf;
			}
			else {
				//in the Multi-tenant case, use
				parallelism = KafkaDriver.default_parallelism;
				pf = new GroupedPairFunctionNoOp();
			}

			JavaPairRDD<String, String> pairs = tuple2JavaRDD.mapToPair(pf);

			//group the RDD by key
			JavaPairRDD<String, Iterable<String>> parted =  pairs.groupByKey(Integer.parseInt(Long.toString(parallelism)));

			//int i = pairs.collect().size();
			m_forEach.m_processor = (IterableTupleProcessor)getProcessor();
			parted.foreach(m_forEach);

			writeOffsetsOnSuccess(getZk(), getDependency(Platform.class));


		} catch (Exception e) {
			e.printStackTrace();
		}

		//TODO: make sure this return is right
		return null;
	}

	private ForEachFunction m_forEach;



	private static final AtomicLong sequence = new AtomicLong(1);
	public static long generate(){
		 long l = sequence.getAndIncrement();
		 if (l == Long.MAX_VALUE ) {
			 sequence.set(1);
		 }
			return l;
		}



	private class GroupedPairFunction implements PairFunction<Tuple2<String, String>, String, String> {
		private static final long serialVersionUID = 1L;

		public long targetParallelism;

		@Override
		public Tuple2<String, String> call(Tuple2<String, String> stringStringTuple2) throws Exception {

			long part = generate() % targetParallelism;
			return new Tuple2<>(stringStringTuple2._1 + "|" + part, stringStringTuple2._2);
		}
	}

	private class GroupedPairFunctionNoOp implements PairFunction<Tuple2<String, String>, String, String> {
		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<String, String> call(Tuple2<String, String> stringStringTuple2) throws Exception {
			return stringStringTuple2;
		}
	}

	private static class ForEachFunction implements VoidFunction<Tuple2<String, Iterable<String>>>, Dependent {

		private static final long serialVersionUID = 1L;

		IterableTupleProcessor m_processor;

		@Override
		public void call(Tuple2<String, Iterable<String>> tuple2) throws Exception {
			//do our impersonation for this tenant using the tenant ID TODO: are keys always the tenant ID?

			try {

				SecurityUtil.setThreadSystemAccess();

				//TODO: decide if a key object is actually better than string parsing
				String tenantID = tuple2._1;
				if (tuple2._1.indexOf("|") >  0) { //extract tenant from key
					 tenantID = tuple2._1.substring(0, tuple2._1.indexOf("|"));
				}

				Impersonation.impersonateTenant(tenantID);
				System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");

				//process the iterable
				m_processor.process(tenantID, tuple2._2);

			} catch (Exception e) {

				//handle the exception within the DG platform and rethrow to spark
				new PlatformClient(getDependency(PlatformMgr.class)).logError(new StreamProcessingException("error processing " + m_processor, e), NodeRole.SPARK_WORKER);

			}

		}

	}

	//TODO: refactor further
	//compute parallelism based on record count and settings
	private long computeParallelism(long count) {
		long target  = KafkaDriver.default_parallelism; //todo make dynamic
		long computed = 1;


		if (count < target) {
			computed = count;
		}
		else if (getOptimalRowsPerTask() > 0) {

			double countD = Double.parseDouble(Long.toString(count));
			double optimalD = Double.parseDouble(Long.toString(getOptimalRowsPerTask()));
			double ratio = countD/optimalD;

			//compute whole number of partitions and percentage remainder
			long paraLong = count / getOptimalRowsPerTask();
			double remainder = ratio - paraLong;

			if (paraLong == 0) {
				computed = 1;
			}
			else {
				//allocate an extra partition on remainders >.4
				if (remainder > .4) {//TODO: is there an instance where contolling this fraction would matter much?
					computed = paraLong + 1;
				}
				else {
					computed = paraLong;
				}
			}

			if (computed > KafkaDriver.default_parallelism) {
				logger.debug("Parallelism computed above default...using default of " + KafkaDriver.default_parallelism);
				computed = KafkaDriver.default_parallelism;

			}

		}
		else {
			//split up using the default parallelism
			computed = KafkaDriver.default_parallelism;
		}

		logger.debug(getCheckpointId() + ": Parallelism computed as " + computed + " based on count of " + count +
			" and optimal records of " + getOptimalRowsPerTask() + " Default parallelism is: " + KafkaDriver.default_parallelism);
		return computed;
	}


}
