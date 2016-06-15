package com.aw.compute.referencedata;


import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rdbms.DBMgr;
import com.aw.compute.inject.Dependent;
import com.aw.platform.Platform;

import java.util.HashMap;

/**
 * lazy creator of lookups
 */
public class GenericLookupDataMgr implements Dependent, ReferenceData {

	public HashMap<String, GenericLookupData> lookups = new HashMap<>();

	public GenericLookupData getLookup(String referenceType) throws Exception{

		if (!lookups.containsKey(referenceType)) {
			GenericLookupData gld = new GenericLookupData(referenceType, getDependency(Platform.class), getProviderDependency(DBMgr.class));
			lookups.put(referenceType, gld);
		}
		return  lookups.get(referenceType);
	}

	@Override
	public void refreshNow() throws ProcessingException {
		//TODO: think nothing is required here
	}

	@Override
	public ReferenceDataManager getManager() {
		return null;
	}

	@Override
	public void setManager(ReferenceDataManager manager) {

	}
}
