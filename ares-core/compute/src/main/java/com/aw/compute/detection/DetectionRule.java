package com.aw.compute.detection;

/**
 * Any rule in the detection framework that is meant to detect something.
 *
 *
 *
 */
public interface DetectionRule {

	public static final long NO_FIRING = -1L;

	/**
	 * @return An ID globally unique across all detection rule types
	 */
	public String getId();

	/**
	 * A string guid identifying the rule. This is kept a string as opposed to a GUID in case the guid is generated in a format
	 * that is not compatible with the java UUID format.
	 *
	 * @return A name globally unique to this type of detection rule
	 */
	public String getName();

	/**
	 * @return a string identifier uniquely identifying this type of detection rule
	 */
	public String getType();

}
