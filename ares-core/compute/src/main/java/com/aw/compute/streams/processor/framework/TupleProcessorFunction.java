package com.aw.compute.streams.processor.framework;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

/**
 * Defines the function that must be implemented for the DefaultStreamHandler to use.
 */
public abstract class TupleProcessorFunction extends SparkCheckpointer implements Function<JavaRDD<Tuple2<String, String>>, Void> {

	private static final long serialVersionUID = 1L;

	public TupleProcessorFunction() {
		this(null);
	}

	public TupleProcessorFunction(Processor processor) {
		super(null, null);
		m_processor = processor;
	}

    public Processor getProcessor() { return m_processor; }
	public void setProcessor(Processor processor) { m_processor = processor; }
	private Processor m_processor;

	public int getOptimalRowsPerTask() {return optimalRowsPerTask;}
	public void setOptimalRowsPerTask(int optimalRowsPerTask) {this.optimalRowsPerTask = optimalRowsPerTask;}
	private int optimalRowsPerTask = 0;

	public boolean isGlobal() {return isGlobal;}
	public void setIsGlobal(boolean isGlobal) {this.isGlobal = isGlobal;}
	private boolean isGlobal;

}
