package com.aw.unity;

/**
 * A data type repository knows of all data types in the system, and can resolve the data type that a piece of
 * raw data represents.
 *
 *
 */
public interface DataTypeRepository {

	/**
	 * @return All data types known to the repository
	 */
	public DataType[] getDataTypes();

	/**
	 * Resolves a data type from raw data.
	 *
	 * @param rawData
	 * @return
	 */
	public DataType resolveDataType(Object rawData);

	/**
	 * Get a data type by name
	 *
	 * @param name
	 * @return
	 */
	public DataType getDataType(String name);

	/**
	 * @return Any data type resolvers used by this data type repository
	 */
	public DataTypeResolver[] getResolvers();

}
