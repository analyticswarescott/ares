package com.aw.compute.referencedata;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.TenantAware;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.document.DocumentHandler;
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

/**
 * A lookup of operation type descriptions
 *
 *
 */
public class OperationTypeLookupData extends AbstractPlatformRestData implements ReferenceDataMap<String, String>, TenantAware, Dependent {

	public static final String OPERATION_TYPE = "ot";

	private static final String PATH = com.aw.util.Statics.VERSIONED_REST_PREFIX + "/unity/resolvers";

	private static final Duration TTL = Duration.ofMinutes(5); //5 minute TTL for this stuff, should change very rarely

	@Inject @com.google.inject.Inject
	public OperationTypeLookupData(Provider<Platform> platform) throws Exception {
		super(NodeRole.REST, Rest.PORT, platform);
		setTTL(TTL);
	}

	@Override
	protected String getCheckPath() {
		return PATH;
	}

	@Override
	protected String getPullPath() {
		return PATH;
	}

	@Override
	public int size() {
		return m_lookup.size();
	}

	@Override
	protected void refresh(String pullPayload) throws ProcessingException {
		update();
	}

	@Override
	public String get(String key) throws ProcessingException {

		//check that we have recent data on each get
		check();

		//our return value
		String ret = null;

		//operation type description is the type name
		DataType datatype = m_lookup.get(key);
		if (datatype != null) {
			ret = datatype.getName();
		}

		return ret;

	}

	protected void update() throws StreamProcessingException {

		try {

			//get a new unity instance
			UnityInstance instance = getNewUnity();

			//get the data type repo
			DataTypeRepository repo = instance.getMetadata().getDataTypeRepository();

			for (DataTypeResolver resolver : repo.getResolvers()) {

				if (resolver instanceof DefaultJSONDataTypeResolver) {
					processResolver((DefaultJSONDataTypeResolver)resolver);
				}

			}

		} catch (Exception e) {
			throw new StreamProcessingException("error loading new operation type mapping data", e);
		}

	}

	protected UnityInstance getNewUnity() throws Exception {
		return new DefaultUnityFactory().getInstance(getTenantID(), getDependency(DocumentHandler.class), ComputeInjector.get().getProvider(Platform.class));
	}

	/**
	 * Takes all of the value mappings for operation type can caches them in the look map. If there are duplicates, the last mapping
	 * with a value for the operation type will end up in the map.
	 *
	 * @param resolver The resolver to process
	 */
	private void processResolver(DefaultJSONDataTypeResolver resolver) {

		//don't process null data
		if (resolver == null || resolver.getMappings() == null) {
			return;
		}

		for (Mapping mapping : resolver.getMappings()) {

			//for now just look for a sign that it's an "ot" value in the json
			if (mapping != null && //if we have a mapping
				mapping.getPath().length > 0 && //and at least one json path entry
			    mapping.getPath()[mapping.getPath().length - 1].equals(OPERATION_TYPE)) { //and it ends in the OPERATION_TYPE key

				m_lookup.putAll(mapping.getValueMap()); //add it to our mapping

			}

		}

	}

	//our internal lookup for ot mapping -> description
	private Map<String, DataType> m_lookup = new HashMap<String, DataType>();

}
