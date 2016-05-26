package com.aw.unity;

/**
 * Builds data from raw
 *
 *
 *
 * @param <T>
 */
public interface DataFactory {

	/**
	 * Build a data object from raw data
	 *
	 * @param rawData The raw data
	 * @return
	 */
	public Data newData(Object rawData, UnityInstance meta);

	/**
	 * Build a new data object given the raw data and a data type
	 *
	 * @param rawData
	 * @param dataType
	 * @return
	 */
	public Data newData(Object rawData, DataType dataType, UnityInstance unity);

	/**
	 * The type of raw data handled by this factory
	 */
	public Class<?> getRawType();

}
