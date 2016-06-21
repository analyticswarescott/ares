package com.aw.unity;

import java.util.List;
import java.util.Set;

import com.aw.common.Tag;
import com.aw.common.Taggable;

/**
 * A type of data with its own set of fields. The data model of the product must define data types for all supported
 * data in the system.
 *
 *
 *
 */
public interface DataType extends Taggable {

	//separates parent and chld data type names
	public static final String SEPARATOR = ".";

	/**
	 * @return Name of this data type
	 */
	public String getName();

	/**
	 * @return Description of this data type
	 */
	public String getDescription();

	/**
	 * @return The fields found in data of this type.
	 */
	public Field[] getFields();

	/**
	 * @param field
	 * @return Whether this data has this field.
	 */
	public boolean hasField(Field field);

	/**
 	 * @param name The name of the field
	 * @return Whether the field is in this data type
	 */
	public boolean hasField(String name);

	/**
	 * Get the field with the given name
	 *
	 * @param name The name of the field requested
	 * @return The field, or null if no field with that name was found
	 */
	public Field getField(String name);

	/**
	 * Get an ordinal number for this field that is consistent for and specific to this data type.
	 * This removes the need to keep a hash map for every event's field values - instead they can
	 * be kept on the event after the first time they are requested, balancing memory usage with
	 * cpu usage.
	 *
	 * The ordinal must always be between 0, up to and excluding getFields().length.
	 *
	 * If the field doesn't exist in the type, an InvalidDataException must be thrown.
	 *
	 * @param field The field being requested
	 * @return The ordinal for the field on this type.
	 */
	public int getOrdinal(Field field);

	/**
	 * @return The name of the unity data source that provides this data.
	 */
	public String getDataSource();

	/**
	 * @return A field whose value will uniquely identify this data.
	 */
	public Field getIDField();

	/**
	 * @return The time field that indicates when this data occurred as an event - may be null if this type of data doesn't have this concept
	 */
	public Field getTimeField();

	/**
	 * @return Child data types of this data type
	 */
	public List<DataType> getSubTypes();

	/**
	 * @return The data type repository this data type belongs to
	 */
	public DataTypeRepository getDataTypeRepository();

	/**
	 * @return The unity instance this data type belongs to
	 */
	public UnityInstance getUnity();

	/**
	 * reference the comnon tags field
	 */
	@Override
	public Set<Tag> getTags();

}
