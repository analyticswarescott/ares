package com.aw.common.processor;

import java.io.Serializable;

import com.aw.common.exceptions.ProcessingException;

/**
 * Anything that processes strings should implement this interface
 *
 *
 *
 */
public interface StringProcessor extends Serializable {

	/**
	 * Process a string from a kafka queue.
	 *
	 * @param string The string data from kafka
	 */
	public void process(String string) throws ProcessingException;

}
