package com.aw.common.system;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonValue;

public class EnvironmentSettings {

	public static final String MYSQL="mysql";
	public static final String POSTGRES="postgres";

 public static final Logger logger = LoggerFactory.getLogger(EnvironmentSettings.class);
    private static String getPWD() {
        String currentDirectory;
        File file = new File(".");
        currentDirectory = file.getAbsolutePath();
        return currentDirectory;
    }

    private static String determineSparkLibHome() {

		String aresBaseHome = getAresBaseHome();
		System.out.println(" determineSparkLibHome getAresBaseHome returns " + aresBaseHome);

        return aresBaseHome + "/ares-core/compute/target" + File.separatorChar + "lib";
    }

    private static String determineConfDirectory() {

		Exception e = new Exception();
		e.printStackTrace();

		String alh = getAppLayerHome();
		System.out.println(" determineConfDirectory getAppLayerHome returns: " + alh);

			return getAppLayerHome() + "/conf";
    }

    public enum Setting {
        // required environment variables - the system will not function
        // properly without setting these

        /**
         * setting for REST and NODE services to use to determine if bootstrapping is needed
         */
        FIRST_NODE(false, true, "false"),

        /**
         * connect host:port for first node, used when node doesn't have platform information yet
         */
        FIRST_NODE_CONNECT(false, true, null),


		DB_VENDOR(false, true, POSTGRES),

		/**
		 * Setting to determine if a viable platform should always be started immediately
		 */
		MULTI_NODE(false, false, "false"),

        ARES_REPORTING(false, false, null),

		ARES_BASE_HOME(false, true, null),

		//ARES_HOME(false, true, getPWD()),
		ARES_HOME(false, true, null),

        CONF_DIRECTORY(false, true, determineConfDirectory()),


        ARES_DATA(false, true, getAppLayerHome() + "/data"),

        SPARK_LIB_HOME(false, true, determineSparkLibHome()),

        ARES_SPARK_HOME(false, true, getAppLayerHome() + File.separatorChar + "spark"),

        /**
         * Root directory for configuration database on a node that hosts one
         */
        CONFIG_DB_ROOT(false, false, getDgData() + File.separatorChar + "config_db"),

        JETTY_HEADER_SIZE_KB(false, false, "256"),

        // optional  properties
        DG_REPORTING_SERVICE_BASE_URL(false, true, "http://localhost:8080"),
        DISABLE_AUTH_VALIDATION(false, false, "false"),
        ALLOW_ORIGIN_HOST(false, false, "http://localhost:8080"),
        DEVELOPMENT_MODE(false, false, "false"),
        TEST_MODE(false, false, "false"),

        /**
         * settable injection class for use in rest layer dependency injection, would generally extend AbstractBinder
         */
        DEPENDENCY_INJECTOR(false, true, null),

        /**
         * Directory holding the platform.json on the local node - used only for
         * node service communication and boostrap starting of first node.
         */
        PLATFORM_PATH(false, true, getConfDirectory() + File.separator + "platform.json"),
		PLATFORM_START(false, true, "true"),

		MANUAL_STARTUP(false, true, "false"),

		//TODO: remove some of these?
        MANUAL_SERVICES_START(false, false, "false"),

        CLEAR_DOCS_ON_STARTUP(false, false, "false"),
        SERVICE_SHARED_SECRET(false, false, "sharedsecret"),
        ARES_HOST(false, true, "localhost"),
        LOG4J_CONFIG_FILE(false, false, getConfDirectory() + "/log4j.properties");


        public final boolean required;
        public boolean checkSystemProperty;
        public final String defaultValue;

        Setting(boolean required, boolean checkSystemProperty, String defaultValue) {
            this.required = required;
            this.checkSystemProperty = checkSystemProperty;
            this.defaultValue = defaultValue;
        }

        public static Setting forValue(String val) {
            return Setting.valueOf(val.toUpperCase());
        }


        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }

    }

    public static boolean isFirstNode() {
        return "true".equals(fetch(Setting.FIRST_NODE));
    }

	public static boolean isPlatformStartEnabled() {
		return "true".equals(fetch(Setting.PLATFORM_START));
	}

    public static boolean isAuthDisabled() {
        return "true".equals(fetch(Setting.DISABLE_AUTH_VALIDATION));
    }

    public static boolean isDevelopmentMode() {
        return "true".equals(fetch(Setting.DEVELOPMENT_MODE));
    }

    public static boolean isTestMode() {
        return "true".equals(fetch(Setting.TEST_MODE));
    }

    public static boolean isClearDocsOnStartup() {
        return "true".equals(fetch(Setting.CLEAR_DOCS_ON_STARTUP));
    }

    public static boolean isStartServicesManually() {
        return "true".equals(fetch(Setting.MANUAL_SERVICES_START));
    }

    public static String getAllowedOriginHost() {
        return fetch(Setting.ALLOW_ORIGIN_HOST);
    }

    /**
     * Will always be terminated with a file separator /
     *
     * @return The directory holding the platform path
     */
    public static String getPlatformPath() {
    	String ret = fetch(Setting.PLATFORM_PATH);
    	return ret;
    }

    /**
     * @return the platform connect string
     */
    public static String getFirstNodeConnect() {
    	return fetch(Setting.FIRST_NODE_CONNECT);
    }

    public static String getDgReporting() {
        return fetch(Setting.ARES_REPORTING);
    }

    public static String getHost() {
    	return fetch(Setting.ARES_HOST);
    }


	/**
	 * This is the home
	 * @return
	 */
	public static String getAresBaseHome() {
		return fetch(Setting.ARES_BASE_HOME);
	}

    public static String getAppLayerHome() {
        return fetch(Setting.ARES_HOME);
    }

    public static String getDgData() {
        return fetch(Setting.ARES_DATA);
    }

    public static String getSparkLibHome() {

		System.out.println("getSparkLibHome called");

        return fetch(Setting.SPARK_LIB_HOME);
    }

    public static String getConfDirectory() {
        try {
            return fetch(Setting.CONF_DIRECTORY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

	public static String getDBVendor() {
		return fetch(Setting.DB_VENDOR);
	}

    public static String getServiceSharedSecret() {
        return fetch(Setting.SERVICE_SHARED_SECRET);
    }

    public static String getLog4JConfigFile() {
        return fetch(Setting.LOG4J_CONFIG_FILE);
    }

    public static String getAresSparkHome() {
        return fetch(Setting.ARES_SPARK_HOME);
    }

    public static int getJettyHeaderSizeKb() {
        return Integer.parseInt(fetch(Setting.JETTY_HEADER_SIZE_KB));
    }

    public static String fetch(Setting setting) {

        String propName = setting.name().toUpperCase();
        String value = System.getenv(propName);

        if (value == null && setting.checkSystemProperty) {
            value = System.getProperty(propName);
        }

        if (value != null) {
            return value;
        }

        if (setting.required) {
            throw new RuntimeException("Missing required environment variable " + setting.name());
        } else {
            // can be null
            return setting.defaultValue;
        }
    }

    private static String fetch(String name) {
        Setting setting = Setting.valueOf(name);
        if (setting == null) {
            return null;
        }

        return fetch(setting);
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("windows");
    }

}
