package com.aw.compute.referencedata;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rdbms.DBConfig;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.document.DocumentHandler;
import com.aw.document.jdbc.JDBCProvider;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.roles.Rest;
import com.aw.unity.DataType;
import com.aw.unity.DataTypeRepository;
import com.aw.unity.DataTypeResolver;
import com.aw.unity.UnityInstance;
import com.aw.unity.defaults.DefaultUnityFactory;
import com.aw.unity.json.DefaultJSONDataTypeResolver;
import com.aw.unity.json.DefaultJSONDataTypeResolver.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * A lookup of operation type descriptions
 *
 *
 */
public class GenericLookupData extends AbstractReferenceData implements ReferenceDataMap<String, String>, TenantAware, Dependent {

	public static final Logger logger = LoggerFactory.getLogger(GenericLookupData.class);

	private static final Duration TTL = Duration.ofMinutes(5);

	protected Platform platform;
	protected Map<String, String> refDBConfig;

	protected String referenceType;

	@Inject @com.google.inject.Inject
	public GenericLookupData(String referenceType, Map<String, String> refDBConfig) throws Exception {
		this.platform = platform;
		this.refDBConfig = refDBConfig;
		this.referenceType = referenceType;
		setTTL(TTL);
	}



	@Override
	public String get(String key) throws ProcessingException {

		//check that we have recent data on each get
		check();

		//our return value
		String ret = null;

		//TODO: support MRU in front of backing store for very large data sets
		ret = m_lookup.get(key);

		return ret;

	}

	protected void update() throws StreamProcessingException {

		try {
			logger.error(" DEBUG: updating from source ");


			JDBCProvider provider = (JDBCProvider) Class.forName(refDBConfig.get(DBConfig.DB_PROVIDER)).newInstance();


			try (Connection conn = DBMgr.getConnection(provider.getJDBCURL(refDBConfig, Tenant.forId(getTenantID()))
				, refDBConfig.get(DBConfig.DB_USER), refDBConfig.get(DBConfig.DB_PASS)))

			{
				String sql = " select ref_key, ref_value from " + referenceType;
				PreparedStatement ps = conn.prepareStatement(sql);

				ResultSet rs = ps.executeQuery();

				m_lookup.clear(); //todo: make safer by loading map twice and comparing?
				while (rs.next()) {
					logger.error(" DEBUG: read record from source ");
					m_lookup.put(rs.getString("ref_key"), rs.getString("ref_value"));
				}

			}

			//read from source

		} catch (Exception e) {
			throw new StreamProcessingException("error loading new lookup data", e);
		}

	}

	protected UnityInstance getNewUnity() throws Exception {
		return new DefaultUnityFactory().getInstance(getTenantID(), getDependency(DocumentHandler.class), ComputeInjector.get().getProvider(Platform.class));
	}


	private Map<String, String> m_lookup = new HashMap<String, String>();

	@Override
	protected void onTTLExpired() throws ProcessingException {
		update();
	}

	@Override
	public int size() {
		return 0;
	}
}
