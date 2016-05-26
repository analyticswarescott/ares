package com.aw.compute.streams.processor.framework;

/**
 * A processor that processes tuples
 *
 *
 *
 */
public interface TupleProcessor extends Processor {

	/**
	 * @return The tuple function to deliver data to this processor
	 */
	public TupleProcessorFunction getTupleFunction();

}
