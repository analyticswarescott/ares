package com.aw.compute.streams.processor;

/**
 * A processing context for bundles. This holds information needed to process a bundle.
 *
 *
 *
 */
public interface BundleContext {

	/**
	 * @return the tenant for this bundle
	 */
	public String getTenantID();

	/**
	 * @return the bundle id
	 */
	public String getBundleID();

	/**
	 * @return machine id
	 */
	public String getMachineID();

}
