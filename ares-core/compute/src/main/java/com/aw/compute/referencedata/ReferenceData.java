package com.aw.compute.referencedata;

import com.aw.common.exceptions.ProcessingException;

/**
 * Any kind of reference data should extend this interface.
 *
 *
 *
 */
public interface ReferenceData {

	/**
	 * May be called by the framework to force a refresh of reference data
	 */
	public void refreshNow() throws ProcessingException;

	/**
	 * @return The manager for this reference data
	 */
	public ReferenceDataManager getManager();
	public void setManager(ReferenceDataManager manager);

}
