package com.aw.compute.streams.processor.framework;

import java.io.Serializable;

import com.aw.common.processor.IterableProcessor;

/**
 * Processes string data from a kafka queue. The thread context is set to the related tenant for this string.
 *
 *
 */
public interface IterableTupleProcessor extends IterableProcessor, TupleProcessor, Serializable {


	/**
	 * @return The tuple function that will deliver data in the way this processor expects
	 */
	@Override
	default public TupleProcessorFunction getTupleFunction() { return new GroupedProcessorFunction(this); }

}
