package com.aw.compute.streams.processor.framework;

import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;

/**
 * Anything that processes unity data.
 *
 *
 */
public interface DataProcessor {

	/**
	 * Process a piece of data. The thread context is already scoped to the tenant.
	 *
	 * @param data The data to process
	 */
	public void process(Data data) throws StreamProcessingException;
}

