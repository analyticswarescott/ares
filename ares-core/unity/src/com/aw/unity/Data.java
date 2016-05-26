package com.aw.unity;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data is anything that can be filtered or generally processed by the platform in some way.
 *
 *
 *
 */
public interface Data extends Serializable {

	/**
	 * @return The type of data this represents
	 */
	public DataType getType();

	/**
	 * Get the value for the given field. It will be typed accordingly.
	 *
	 * @param field
	 * @return
	 */
	public Object getValue(Field field);

	/**
	 * @return A globally unique ID for this piece of data - globally unique at least for this tenant
	 */
	public String getGuid();

	/**
	 * Returns a json representation of this unity data object. The specifics of how this transform occurs
	 * is not defined here, but the contract of this method is that the returned json will NOT contain any
	 * json keys that are not defined in the unity type if strict=true. If struct=false, the json keys will
	 * not be cleaned based on the type, but epochTime=true would still cause the timestamp to be formatted
	 * as long millis since the 1970 epoch.
	 *
	 * @param pretty If the json should be formatted in a readable way with newlines and indentation. If pretty=false, the json MUST be single line with no extra whitespace.
	 * @param epochTime If timestamps should bein epoch millis numeric format, otherwise it will be formatted according to unity's date format
	 * @param strict If the json should ONLY contain key fields that match the type
	 * @return Returns The converted data to json
	 * @throws Exception If anything goes wrong
	 */
	public String toJsonString(boolean pretty, boolean epochTime, boolean strict);

	/**
	 * @return The time this data occurred - if no time is available for this type of data, null may be returned
	 */
	public Instant getTime();

	/**
	 * @return the unity instance that generated this data
	 */
	public UnityInstance getUnity();

}

