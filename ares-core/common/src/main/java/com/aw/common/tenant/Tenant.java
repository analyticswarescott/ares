package com.aw.common.tenant;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.auth.User;
import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.es.ElasticIndex;
import com.aw.platform.PlatformSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Object to interact with Tenant metadata
 *
 * Tenants can contain a json structure that mirrors the platform configuration, containing overrides for platform settings related to tenant-specific
 * resources. For example, es sharding, replication factor, kafka replication factor, etc.
 *
 * An example follows:
 *
 *
 *
 */
@JsonIgnoreProperties({"operation", "sn", "ln", "srv", "db", "ccs", "lc", "ob"})
@SuppressWarnings("deprecation")
public class Tenant implements TenantAware, Serializable {

	private static final long serialVersionUID = 1L;

	public static final Logger logger = LoggerFactory.getLogger(Tenant.class);

    public static final String SYSTEM_TENANT_ID = "0";
    public static final String SYSTEM_TENANT_UID = User.SYSTEM.getUserid(); //TODO: use a user object instead of strings
    public static final String SYSTEM_TENANT_USER_NAME = "aw";
    public static final String SYSTEM_TENANT_LOCALE = "en-us";

	public static final Tenant SYSTEM = new Tenant(SYSTEM_TENANT_ID);

    public Tenant() {
	}

    public Tenant(String tenantID, String longName) {
    	this(tenantID, longName, new TenantSettings());
	}

    public Tenant(String tenantID, String longName, TenantSettings settings) {
    	m_tenantId = tenantID;
    	m_ln = longName;
        this.settings = settings;
	}

    public Tenant(String tenantID) {
        this(tenantID, tenantID);
    }

    public Tenant(String tenantID, PlatformSettings settings) {
        this(tenantID, tenantID);
    }

    public static Tenant forId(String id) {
    	return new Tenant(id);
    }

    public Tenant(JSONObject o) throws IOException {
        build(o.toString());
    }

    private void build(String json) throws IOException {
        //set our properties
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        ObjectReader reader = mapper.readerForUpdating(this);
        reader.readValue(json);

        validate();
    }

    private void validate() {
        //TODO -- define exact validation rules


        return;

    }

    /**
     * @return Stringstatic
     */
    @JsonProperty("env_overrides")
    public HashMap<com.aw.common.system.TenantSettings.Setting, String> getEnvOverrides() { return m_overrides; }
    public void setEnvOverrides(HashMap<com.aw.common.system.TenantSettings.Setting, String> overrides) {
        m_overrides = overrides;
    }
    private HashMap<com.aw.common.system.TenantSettings.Setting, String> m_overrides = new HashMap<com.aw.common.system.TenantSettings.Setting, String>();

    @Override
    public String toString() {
    	return "[tenant id=" + getTenantID() + "]";
    }

    /**
     * @return String
     */
    @Override

    public String getTenantID() { return m_tenantId; }

    public void setTenantID(String tenant_id) {
    	if (m_tenantId != null) {
    		throw new UnsupportedOperationException("cannot set a tenant ID once it has already been set");
    	}
    	m_tenantId = tenant_id;
    }
	@JsonProperty("tid")
    private String m_tenantId;


    /**
     * @return String
     */

	@JsonProperty("mwa")
    public String getURI() { return m_uri; }
    public void setURI(String uri) { m_uri = uri; }
    private String m_uri;



	/**
	 * @return String
	 */

	@JsonProperty("rough_sizing")
	public String getRoughSizing() { return m_rough_sizing; }
	public void setRoughSizing(String rough_sizing) { m_uri = rough_sizing; }
	private String m_rough_sizing;

	/**
	 * get retention time per elasticsearch index
	 *
	 * @param index
	 * @return
	 */
	public Duration getRetention(ElasticIndex index) {

		//TODO: implement tenant settings - for now set retention to 30 days
		return Duration.ofDays(30);

	}

    /**static
     * @return String
     */
    public String getLongName() { return m_ln; }
    public void setLongName(String ln) { m_ln = ln; }

	@JsonProperty("long_name")
    private String m_ln;

    public String getSettingOrEnv(com.aw.common.system.TenantSettings.Setting setting) {
        String ret;
        if (getEnvOverrides().get(setting) == null) {
            ret = EnvironmentSettings.fetch(
                    EnvironmentSettings.Setting.valueOf(setting.toString().toUpperCase()));
        }
        else { ret = getEnvOverrides().get(setting);}

        ret = ret.replace("$TENANT_ID", getTenantID());

        return ret;
    }

    public  static  Tenant  getDefaultTenant() throws Exception {
    	return Tenant.SYSTEM;
    }

    @JsonIgnore
    public JSONObject getAsJSON() throws Exception {

        JSONObject ret = new JSONObject(JSONUtils.objectToString(this));

        //have to manually add tenant as it's ignored in the TenantAware interface
        ret.put("tid", getTenantID());

        return ret;

    }

    /**
     * Based on tenant id
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_tenantId == null) ? 0 : m_tenantId.hashCode());
		return result;
	}

    /**
     * Based on tenant id
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenant other = (Tenant) obj;
		if (m_tenantId == null) {
			if (other.m_tenantId != null)
				return false;
		} else if (!m_tenantId.equals(other.m_tenantId))
			return false;
		return true;
	}

	/**
	 * @return Settings specific to this tenant (es shard count, replication factors, etc)
	 */
	public TenantSettings getSettings() { return this.settings;  }
	public void setSettings(TenantSettings settings) { this.settings = settings; }
	private TenantSettings settings = new TenantSettings();

	/**
	 * @return get the tenant specific index time unit for time slicing, or the default if tenant does not have one defined
	 */
	@JsonIgnore
	public ChronoUnit getIndexTimeUnit() throws ConfigurationException {
		return settings.getSetting(TenantSetting.ES_TIME_UNIT, ChronoUnit.class, ChronoUnit.WEEKS);
	}

}

