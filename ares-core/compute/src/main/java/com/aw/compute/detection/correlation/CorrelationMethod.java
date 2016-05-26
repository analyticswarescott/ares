package com.aw.compute.detection.correlation;

import java.io.Serializable;

import com.aw.unity.Data;

/**
 * A correlation method is a way by which we tie (or correlate) data together.
 *
 *
 *
 */
public interface CorrelationMethod extends Serializable {

	/**
	 * Builds a correlation key given the data
	 *
	 * @param data The data for which we will build a correlation key
	 * @return The correlation key
	 */
	public String toKey(Data data);

}
