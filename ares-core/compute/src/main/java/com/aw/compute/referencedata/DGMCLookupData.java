package com.aw.compute.referencedata;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.roles.DGMC;

/**
 * A reference data map from the DGMC, things we need the DGMC to lookup.
 *
 * TODO: no implemented yet
 *
 *
 *
 */
public class DGMCLookupData extends AbstractPlatformRestData implements ReferenceDataMap<String, String> {

	/**
	 * The available lookup types for the DGMC
	 *
	 *
	 */
	public enum TypeID implements ReferenceDataID {

		//TODO: based on incoming json field names?
		UN,
		PT,
		DRPT,
		DST,
		DDT,
		SEA,
		MRT,
		SDT,
		REG_VT,

	}

	@Inject @com.google.inject.Inject
	public DGMCLookupData(Provider<Platform> platform) throws Exception {
		super(NodeRole.DGMC, DGMC.PORT, platform);
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public String get(String key) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	protected boolean shouldRefresh(String checkPayload) throws StreamProcessingException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	protected void refresh(String pullPayload) throws StreamProcessingException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	protected String getCheckPath() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	protected String getPullPath() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/**
	 * @return The identifier representing the type of data to look up from the DGMC
	 */
	public TypeID getTypeID() { return m_typeID; }
	public void setTypeID(TypeID typeID) { m_typeID = typeID; }
	private TypeID m_typeID;

}
