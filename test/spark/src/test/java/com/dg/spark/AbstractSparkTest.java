package com.aw.spark;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.After;
import org.junit.Before;

import com.aw.common.tenant.Tenant;
import com.aw.document.DocumentAware;
import com.aw.unity.Data;
import com.aw.util.ListMap;

import scala.Tuple2;

/**
 * Spark only test, using local mode with the ui disabled
 *
 * @author jlehmann
 *
 */
public class AbstractSparkTest implements DocumentAware {

	public static final int DEFAULT_WORKER_THREADS = 4;
	public static final int DEFAULT_BATCH_DURATION = 2;

	@Before
	public void before() throws Exception {

		SparkConf conf = new SparkConf()
				.set("spark.ui.enabled", "false")
				.setAppName("test_app")
				.setMaster("local[" + getWorkerThreads() + "]");

		//register test document handler
		this.context = new JavaSparkContext(conf);
		this.ssContext = new JavaStreamingContext(this.context, Durations.milliseconds(1000));

		//set up checkpointing
		new File("./data/spark").mkdirs();
		this.ssContext.checkpoint("./data/spark");

		//install system docs
		getDocumentMgr().addDocHandler(Tenant.SYSTEM, new TestDocumentHandler());

	}

	@After
	public void after() {

		this.context.close();

	}

	/**
	 * Create a set of RDDs up to maxRDDSize in size
	 *
	 * @param data The map of test data
	 * @param maxRDDSize The maximum size of each RDD
	 * @return The return RDD queue
	 */
	protected Queue<JavaRDD<Tuple2<String, String>>> toRDDs(ListMap<String, Data> data, int maxRDDSize) throws Exception {

		Queue<JavaRDD<Tuple2<String, String>>> ret = new LinkedList<>();

		List<Tuple2<String, String>> curRDD = new ArrayList<>();
		for (String key : data.keySet()) {

			for (Data value : data.get(key)) {

				curRDD.add(new Tuple2<String, String>(key, value.toJsonString(false, true, false)));

				//split and start a new RDD when the size limit is reached
				if (curRDD.size() == maxRDDSize) {
					ret.add(this.context.parallelize(curRDD));
					curRDD = new ArrayList<>();
				}

			}

		}

		return ret;

	}

	/**
	 * Override to set spark worker thread count - defaults to 4
	 */
	protected int getWorkerThreads() {
		return DEFAULT_WORKER_THREADS;
	}

	protected int getBatchDurationSeconds() {
		return DEFAULT_BATCH_DURATION;
	}

	//the spark context
	protected JavaSparkContext context;

	//the spark streaming context
	protected JavaStreamingContext ssContext;

}


