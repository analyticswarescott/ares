package com.aw.unity.dg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DG bundle field reference information
 *
 *
 *
 */
public class BundleFields {

	//known fields that map to common fields
	public static final String DETAIL_ID = "medid";
	public static final String EVENT_ID = "meid";
	public static final String TIME = "esu";
	public static final String OT_DESC = "ot_desc";
	public static final String FILE_MD5 = "imageMD5";
	public static final String FILE_SHA1 = "imageSHA1";
	public static final String FILE_SHA256 = "imageSHA256";
	public static final String FILE_SIZE = "fs";
	public static final String FILE_CREATE = "fcl";
	public static final String FILE_MODIFY = "fml";
	public static final String FILE_VERSION = "fv";
	public static final String FILE_CAPTURE = "cf";
	public static final String MACHINE_ID = "mid";
	public static final String BUNDLE_ID = "bid";
	public static final String USER_NAME = "un";
	public static final String PROCESS_FLAGS = "pf";
	public static final String SEQUENCE_NUMBER = "seq";
	public static final String PRODUCT_NAME = "pn";
	public static final String PRODUCT_VERSION = "pv";
	public static final String REGISTRY_VALUE = "regv";

	/**
	 * A map of locally scoped DG fields to their common field equivalent
	 */
	public static Map<String, CommonField> BUNDLE_TO_COMMON = Collections.unmodifiableMap(new HashMap<String, CommonField>() {{

		//map from aw meaw field names to common fields
		put(TIME, CommonField.DG_TIME);
		put(DETAIL_ID, CommonField.DG_GUID);
		put(EVENT_ID, CommonField.DG_GUID);
		put(FILE_SIZE, CommonField.DG_FILE_SIZE);
		put(FILE_MD5, CommonField.DG_FILE_MD5);
		put(FILE_SHA1, CommonField.DG_FILE_SHA1);
		put(FILE_SHA256, CommonField.DG_FILE_SHA256);
		put(FILE_CREATE, CommonField.DG_FILE_CREATE);
		put(FILE_MODIFY, CommonField.DG_FILE_MODIFY);
		put(SEQUENCE_NUMBER, CommonField.DG_SEQ);
		put(USER_NAME, CommonField.DG_USER);
		put(PROCESS_FLAGS, CommonField.DG_PF);
		put(FILE_CAPTURE, CommonField.DG_CF);
		put(PRODUCT_NAME, CommonField.DG_PN);
		put(PRODUCT_VERSION, CommonField.DG_PV);
		put(REGISTRY_VALUE, CommonField.DG_RV);
		put(FILE_VERSION, CommonField.DG_FV);

	}});

}
