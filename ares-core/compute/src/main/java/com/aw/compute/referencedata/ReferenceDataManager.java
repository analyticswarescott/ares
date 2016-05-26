package com.aw.compute.referencedata;

import com.aw.compute.detection.SimpleRulesData;
import com.aw.compute.referencedata.geo.GeoLocationLookupData;

/**
 * Reference data is a term used to describe anything that is needed during data processing within
 * the platform. This class manages access to reference data.
 *
 *
 */
public class ReferenceDataManager {

	public enum ReferenceDataType {

		/**
		 * Simple single event match rules
		 */
		SIMPLE_RULES(SimpleRulesData.class),

		/**
		 * Reference data that will look up operation type descriptions
		 */
		OT_DESCRIPTION_LOOKUP(OperationTypeLookupData.class), //lookup descriptions of "ot" values for bundles (i.e. operation types)

		GEO_LOCATION_LOOKUP(GeoLocationLookupData.class);

		private ReferenceDataType(Class<? extends ReferenceData> type) {
			m_type = type;
		}

		public <T> Class<T> getType() { return (Class<T>)m_type; }
		private Class<?> m_type;

	}

}
