package com.aw.unity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

import com.aw.common.AbstractTaggable;
import com.aw.common.Tag;
import com.aw.unity.exceptions.InvalidDataException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A field is something that stores a particular value (or array of values). It has consistent semantic meaning
 * across the system, across multiple DataTypes. For example:
 *
 * <li> An "Alert" might have a "process_md5" in it.
 * <li> An "Event" might have a "process_md5" in it.
 * <li> An EDR scan might have a "process_md5" in it.
 *
 * These may all physically appear in separate areas in each DataType's physical representation, but they mean the
 * same thing. These fields all represent the same Field instance. Within a "Data" instance of each of these 3
 * DataTypes, the same process_md5 Field instance can be used to retrieve the process_md5 value from that data.
 *
 *
 */
public class Field extends AbstractTaggable implements Comparable<Field>, Serializable {

	/**
	 * A field tag used for fields that represent measures
	 */
	public static final Tag TAG_MEASURE = Tag.valueOf("measure");

	/**
	 * A field tag used for fields that represent dimensions
	 */
	public static final Tag TAG_DIMENSION = Tag.valueOf("dimension");

	/**
	 * The single field that is used to indicate the field value is a constant
	 */
	public static Field CONSTANT = new Field("constant", FieldType.STRING);

	public Field() {
	}

	public Field(String name, FieldType type) {
		setName(name);
		setType(type);
	}

	public Field(String name, FieldType type, String objectType, boolean array) {
		this(name, type);
		setObjectType(objectType);
		setArray(array);
	}

	public Field(String name, FieldType type, String displayName, String description, Tag... tags) {
		setName(name);
		setType(type);
		setDisplayName(displayName);
		setDescription(description);

		//add tags if any
		if (tags != null) {
			getTags().addAll(Arrays.asList(tags));
		}
	}

	public Field(String name, FieldType type, boolean array) {
		setName(name);
		setType(type);
		setArray(array);
	}

	public Object fromString(String str) {

		try {

			return m_type.getValueParser().parse(null, this, str);

		} catch (Exception e) {
			throw new InvalidDataException("Can't parse value " + str + " for type " + m_type + ": " + e.getMessage(), e);
		}

	}

	/**
	 * @return The data type name, either will be objectType or the field name if that's null
	 */
	public String getDataType() {
		if (m_type == FieldType.OBJECT) {
			if (m_objectType == null) {
				return m_name;
			}

			else {
				return m_objectType;
			}
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return m_name;
	}

	/**
	 * Based on name
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		return result;
	}

	/**
	 * Based on name
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		return true;
	}

	/**
	 * Order based on name, case insensitive
	 */
	@Override
	public int compareTo(Field o) {
		return m_name == null || o == null || o.m_name == null ? -1 : m_name.compareToIgnoreCase(o.m_name);
	}

	/**
	 * @return The field name
	 */
	public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	/**
	 * @return The field type
	 */
	public FieldType getType() { return m_type; }
	public void setType(FieldType type) { m_type = type; }
	private FieldType m_type;

	/**
	 * TODO: localization
	 * @return Friendly name of the field
	 */
	public String getDisplayName() { return m_displayName; }
	public void setDisplayName(String displayName) { m_displayName = displayName; }
	private String m_displayName;

	/**
	 * @return The unity data type this field holds, if type is object
	 */
	public String getObjectType() { return m_objectType; }
	public void setObjectType(String objectType) { m_objectType = objectType; }
	private String m_objectType;

	/**
	 * TODO: localization
	 * @return The field description
	 */
	public String getDescription() { return m_description; }
	public void setDescription(String description) { m_description = description; }
	private String m_description;

	/**
	 * @return Whether the field is an array of values
	 */
	public boolean isArray() { return m_array; }
	public void setArray(boolean array) { m_array = array; }
	private boolean m_array;


	public Reference getReference() {return reference;}
	public void setReference(Reference reference) {this.reference = reference;}
	private Reference reference;

	/**
	 * @return The unity instance this field belongs to
	 */
	@JsonIgnore
	public UnityInstance getUnity() { return unity; }
	public void setUnity(UnityInstance unity) { this.unity = unity; }
	private UnityInstance unity;

	/**
	 * @return Tags associated with this field
	 */
	@JsonProperty("tags")
	public Set<Tag> getTags() { return super.getTags(); }

}
