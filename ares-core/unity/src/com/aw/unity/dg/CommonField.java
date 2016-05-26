package com.aw.unity.dg;

import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.json.JSONFieldRef;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Common DG fields within Unity
 *
 * TODO: Create a unity-aw module
 *
 *
 *
 */
public enum CommonField {

	/**
	 * Globally unique id for a piece of data
	 */
	DG_GUID(FieldType.GUID),

	/**
	 * The time something occurred, in the general sense
	 */
	DG_TIME(FieldType.TIMESTAMP),

	/**
	 * The md5 hash of a file
	 */
	DG_FILE_PATH(FieldType.STRING),

	/**
	 * A tenant id
	 */
	DG_TENANT(FieldType.GUID),

	/**
	 * The file size
	 */
	DG_FILE_SIZE(FieldType.LONG),

	/**
	 * File extension
	 */
	DG_FILE_EXT(FieldType.STRING),

	/**
	 * The md5 hash of a file
	 */
	DG_FILE_MD5(FieldType.GUID),

	/**
	 * The sha1 hash of a file
	 */
	DG_FILE_SHA1(FieldType.GUID),

	/**
	 * The sha256 hash of a file
	 */
	DG_FILE_SHA256(FieldType.GUID),

	/**
	 * Local end time
	 */
	DG_LET(FieldType.TIMESTAMP),

	/**
	 * Local start time
	 */
	DG_LST(FieldType.TIMESTAMP),

	/**
	 * User
	 */
	DG_USER(FieldType.STRING),

	/**
	 * Subtype
	 */
	DG_ST(FieldType.STRING),

	/**
	 * Capture flag (has file capture?)
	 */
	DG_CF(FieldType.BOOLEAN),

	/**
	 * Process flags
	 */
	DG_PF(FieldType.STRING),

	/**
	 * Timestamp of file creation
	 */
	DG_FILE_CREATE(FieldType.TIMESTAMP),

	/**
	 * Timestamp of last file modification
	 */
	DG_FILE_MODIFY(FieldType.TIMESTAMP),

	/**
	 * The name of something
	 */
	DG_NAME(FieldType.STRING),

	/**
	 * The description of something
	 */
	DG_DESCRIPTION(FieldType.STRING),

	/**
	 * Bundle ID
	 */
	DG_BID(FieldType.GUID),

	/**
	 * Machine ID
	 */
	DG_MID(FieldType.GUID),

	/**
	 * Sequence number
	 */
	DG_SEQ(FieldType.LONG),

	/**
	 * Product name
	 */
	DG_PN(FieldType.STRING),

	/**
	 * Registry value
	 */
	DG_RV(FieldType.STRING),

	/**
	 * Product version
	 */
	DG_PV(FieldType.STRING),

	/**
	 * File version
	 */
	DG_FV(FieldType.STRING),

	/**
	 * Unity data type
	 */
	DG_UTYPE(FieldType.STRING),

	/**
	 * A comment string
	 */
	DG_COMMENT(FieldType.STRING),

	/**
	 *
	 */
	DG_TAGS(FieldType.STRING),

	/**
	 * the short name of the detection rule that fired
	 */
	DG_DET_NAME(FieldType.STRING),

	/**
	 * the type of detection rule that fired
	 */
	DG_DET_TYPE(FieldType.STRING),

	/**
	 * the guid of the rule
	 */
	DG_DET_ID(FieldType.GUID),

	/**
	 * a guid for the particular alarm firing
	 */
	DG_ALARM_ID(FieldType.GUID),

	/**
	 * alert information
	 */
	DG_ALERT(FieldType.OBJECT, "alert", true),

	/**
	 * custom data
	 */
	DG_CUSTOM_DATA(FieldType.OBJECT, "custom_data", true),

	/**
	 * an array of string values
	 */
	DG_VALUES(FieldType.STRING, null, true),

	/**
	 * the scope of something
	 */
	DG_SCOPE(FieldType.STRING),

	/**
	 * the type of something
	 */
	DG_TYPE(FieldType.STRING),

	/**
	 * array of digital guardian policy information
	 */
	DG_POLICY(FieldType.OBJECT, "policy", true);

	private CommonField(FieldType type) {
		this(type, null, false);
	}

	private CommonField(FieldType type, String typeName, boolean array) {

		m_field = new Field(toString(), type, typeName, array);
		m_ref = new JSONFieldRef(toString(), new String[] { toString() });

	}

	public String toString() {
		return name().toLowerCase();
	}

	/**
	 * @return The common field as a json field ref
	 */
	public JSONFieldRef asRef() {
		return m_ref;
	}
	private JSONFieldRef m_ref;

	/**
	 * @return The common field as a unity field
	 */
	public Field asField() {
		return m_field;
	}
	private Field m_field;

	/**
	 * Will return null if not found
	 *
	 * @param str
	 * @return
	 */
	public static CommonField tryFromString(String str) {
		try {
			return CommonField.valueOf(str.toUpperCase());
		} catch (Exception e) {
			return null; //no such common field
		}
	}

	@JsonCreator
	public static CommonField fromString(String str) {
		return valueOf(str.toUpperCase());
	}

	public static final String DG_GUID_STRING = "dg_guid";
	public static final String DG_TIME_STRING = "dg_time";
	public static final String DG_DESCRIPTION_STRING = "dg_description";
	public static final String DG_NAME_STRING = "dg_name";
	public static final String DG_UTYPE_STRING = "dg_utype";
	public static final String DG_TAGS_STRING = "dg_tags";
	public static final String DG_FILE_NAME_STRING = "dg_file_name";
	public static final String DG_FILE_PATH_STRING = "dg_file_path";

}
