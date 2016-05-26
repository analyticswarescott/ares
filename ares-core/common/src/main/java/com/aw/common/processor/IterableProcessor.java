package com.aw.common.processor;

import java.io.Serializable;

import com.aw.common.exceptions.ProcessingException;

public interface IterableProcessor extends Serializable {

	/**
	 * Process a string from a kafka queue.
	 *
	 * @param string The string data from kafka
	 */
	public void process(String string, Iterable<String> messages) throws ProcessingException;

}
