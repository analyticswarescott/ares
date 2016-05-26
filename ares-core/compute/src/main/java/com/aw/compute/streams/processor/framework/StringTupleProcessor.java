package com.aw.compute.streams.processor.framework;

import com.aw.common.processor.StringProcessor;

/**
 * Processes string data from a kafka queue. The thread context is set to the related tenant for this string.
 *
 *
 */
public interface StringTupleProcessor extends StringProcessor, TupleProcessor {

	/**
	 * @return The tuple function that will deliver data in the way this processor expects
	 */
	@Override
	default public TupleProcessorFunction getTupleFunction() { return new ScalarProcessorFunction(this); }

}
