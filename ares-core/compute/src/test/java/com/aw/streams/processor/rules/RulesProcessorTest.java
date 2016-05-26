package com.aw.streams.processor.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.spark.api.java.function.VoidFunction;
import org.junit.Test;

import com.aw.test.DGStreamUnitTest;

import scala.Tuple2;

/**
 * Eventually this will test the rules engine - for now it's just partitioning data
 *
 *
 *
 */
public class RulesProcessorTest extends DGStreamUnitTest {

	@Test
	public void spark_test() throws Exception {

		//when we have topic data this can run
//		JavaSparkContext ctx = newSparkContext();
//
//		JavaPairRDD<String, org.codehaus.jettison.json.JSONObject> pairRDD = ctx
//			.textFile(getTopicPath(TOPIC_MACHINE_EVENT_DLP), 1)
//			.map(new JsonMapping())
//			.mapToPair(new FieldGrouping("sfd"))
//			.partitionBy(new HashPartitioner(m_numPartitions))
//			.persist(StorageLevel.MEMORY_ONLY_SER());
//
//		pairRDD.foreach(new CountPartitionedKeys());

	}

	static class CountPartitionedKeys implements VoidFunction<Tuple2<String,org.codehaus.jettison.json.JSONObject>> {
		private static final long serialVersionUID = 1L;

		Map<String, AtomicLong> m_map = new HashMap<String, AtomicLong>();

		@Override
		public synchronized void call(Tuple2<String, org.codehaus.jettison.json.JSONObject> tuple) throws Exception {

			AtomicLong aLong = m_map.get(tuple._1());
			if (aLong == null) {
				aLong = new AtomicLong();
				m_map.put(tuple._1(), aLong);
			}
			aLong.incrementAndGet();

		}

	}

	/**
	 * TODO: match partitions to some spark setting like cluster size?
	 *
	 * @return
	 */
	public int getNumPartitions() { return m_numPartitions; }
	public void setNumPartitions(int numPartitions) { m_numPartitions = numPartitions; }
	private int m_numPartitions = 10;


}
