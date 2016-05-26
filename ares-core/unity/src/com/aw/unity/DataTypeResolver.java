package com.aw.unity;

/**
 * Something that can resolve raw data to unity DataTypes
 *
 *
 */
public interface DataTypeResolver {

	/**
	 * Property identifying the resolver type
	 */
	public static final String TYPE = "type";

	/**
	 * Resolve data to a data type
	 *
	 * @param rawData
	 * @param repo
	 * @return
	 */
	public DataType resolve(Object rawData);

}
