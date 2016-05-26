package com.aw.compute.streams.processor.framework;

import org.apache.spark.streaming.api.java.JavaDStream;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.spark.StreamDef;
import com.aw.common.util.Utils;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.exceptions.InvalidConfigurationException;

import scala.Tuple2;

/**
 * A default stream handler will split the stream based on standard Spark routing - functions will be used to
 * key the data by tenant and the data will be routed with no additional transformations.
 *
 *
 *
 */
public class DefaultStreamHandler extends AbstractStreamHandler {

	@Override
	public void handle(JavaDStream<Tuple2<String, String>> stream) throws StreamProcessingException {

		if (m_function == null) {
			throw new StreamProcessingException("function not defined for default stream");
		}

		//process the RDDs with our function
		stream.foreachRDD(m_function);
	}


	/**
	 * initialize
	 */
	public void init(StreamDef streamDef) throws InitializationException {

		try {

			//configure us here
			Utils.initializeFromMap(streamDef.getConfigData(), this);

			if (getProcessorClass() == null) {
				throw new InitializationException("missing processor class: " + streamDef.getConfigData().toString());
			}

			//create our processor - must be a tuple processor
			m_processor = getProcessorClass().newInstance();

			//initialize the processor
			m_processor.init(streamDef);

			//get the tuple function for this processor
			m_function = m_processor.getTupleFunction();

			//check the id for checkpointing
			m_function.setCheckpointId(getName());

			m_function.setTenant(getTenant()); //passing null will store offsets in the System zookeeper hive

			m_function.setZk(getZk());

			//TODO: this is just an initial iteration of a parallelism-control framework
			m_function.setIsGlobal(streamDef.isGlobal());
			m_function.setOptimalRowsPerTask(streamDef.getOptimalEventsPerTask());

			if (m_function == null) {
				throw new InvalidConfigurationException("missing processor function for stream def " + streamDef.getProcessorName(getTenant()));
			}

		} catch (Exception e) {
			throw new InitializationException("error loading stream", e);
		}

	}

	@SuppressWarnings("unchecked")
	public <T extends TupleProcessor> T getProcessor() { return (T)m_processor; }
	public void setProcessor(TupleProcessor processor) { m_processor = processor; }
	private TupleProcessor m_processor;

	public Class<? extends TupleProcessor> getProcessorClass() { return m_processorClass; }
	public void setProcessorClass(Class<? extends TupleProcessor> processorClass) { m_processorClass = processorClass; }
	private Class<? extends TupleProcessor> m_processorClass;

	//the function to process tuples - this can vary based on the processor type
	private TupleProcessorFunction m_function;

}
