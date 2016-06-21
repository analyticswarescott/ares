package com.aw.unity;

/**
 * The data model metadata as is seen by a single tenant.
 *
 * TODO: merge this with UnityInstance
 *
 *
 */
public interface UnityMetadata {

	/**
	 * Get a data type by name
	 *
	 * @param dataType
	 * @return
	 */
	public DataType getDataType(String dataType);

	/**
	 * @return All known fields
	 */
	public FieldRepository getFieldRepository();

	/**
	 * @return All known data types
	 */
	public DataTypeRepository getDataTypeRepository();

	/**
	 * @return The filter factory
	 */
	public FilterFactory getFilterFactory();

}

