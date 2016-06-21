package com.aw.util;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.regex.Pattern;

import com.aw.common.auth.DefaultUser;
import com.aw.common.auth.User;

public class Statics {


	public static final String RELATIVE_DOCS_PATH = "../../conf/defaults";
	//public static final String CONFIG_DIR_NAME = "ares-apps";

	//public static final String RELATIVE_DOCS_PATH = "../../conf/defaults";
	//public static final String CONFIG_DIR_NAME = "ares-apps";

	/**
	 * java time utc zone id
	 */
	public static ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

	/**
	 * utf-16 little endian
	 */
	public static final Charset UTF_16_LE = Charset.forName("UTF-16LE");

	/**
	 * utf-16 big endian
	 */
	public static final Charset UTF_16_BE = Charset.forName("UTF-16BE");

	/**
	 * utf-16 character set, byte order based on byte order marker (BOM)
	 */
	public static final Charset UTF_16 = Charset.forName("UTF-16");

	/**
	 * utf-8 character set
	 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * default character set - when in doubt, start with this
	 */
	public static final Charset CHARSET = UTF_8;

	/**
	 * Common indent factor for json when pretty printing
	 */
	public static final int JSON_INDENT_FACTOR = 4;

	/**
	 * The official DG author as a string
	 */
	public static final String DG_USER = "aw";

	/**
	 * Default DG user
	 */
	public static final User SYSTEM_USER = new DefaultUser(DG_USER);

	/**
	 * Current REST version
	 */
	public static final String REST_VERSION = "1.0";

	public static final String REST_PREFIX = "/rest";

	/**
	 * The prefix for all rest mappings in the platform
	 */
	public static final String VERSIONED_REST_PREFIX = REST_PREFIX + "/" + REST_VERSION;
	public static final String PROP_ELASTICSEARCH_ENABLED = "ELASTICHSEARCH_ENABLED";


	public static final String PROP_POSTGRES_ENABLED = "PROP_POSTGRES_ENABLED";

	/**
	 * Whether we should log platform errors - true by default
	 */
	public static final String PROP_LOG_PLATFORM_ERRORS = "aw.log.platform.errors";

	/**
	 * System property to set if platform start should be disabled
	 */
	public static final String PROP_PLATFORM_START_ENABLED = "PLATFORM_START";

	/**
	 * In case of problems caching platform, this will hold the previous platform document
	 */
	public static final String PLATFORM_PREVIOUS = "platform-previous.json";

	/**
	 * The cached platform
	 */
	public static final String PLATFORM_CURRENT = "platform.json";

	/**
	 * Supported schemes for the rest service
	 */
	public static final String[] SCHEMES = new String[] { "http" };

	/**
	 * error message key for standard error message format
	 */
	public static final String ERROR_MESSAGE = "message";

	/**
	 * prefix used to specify a resolvable variable
	 */
	public static final String VARIABLE_PREFIX = "${";

	/**
	 * suffix used to specify a resolvable variable
	 */
	public static final String VARIABLE_SUFFIX = "}";

	/**
	 * regular expression that will match a variable and capture a variable name
	 */
	public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

}
