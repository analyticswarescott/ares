package com.aw.compute.streams.processor.framework;

import com.aw.common.rest.security.TenantAware;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.DataProcessingException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;

/**
 * Processes kafka queue as unity data.
 *
 *
 */
public abstract class AbstractDataProcessor implements DataProcessor, TenantAware, Dependent {

	public void process(String string) throws StreamProcessingException {

		try {

			//turn it into data
			Data data = getDependency(UnityInstance.class).newData(string);

			//process it
			process(data);

		} catch (Exception e) {
			throw new DataProcessingException("Error processing data for tenant " + getTenantID(), e);
		}

	}

}
