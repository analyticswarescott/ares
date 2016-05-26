package com.aw.compute.referencedata;

/**
 * Reference data that has a particular ID in addition to its type. The ID provides context for the specific reference data
 * type to know exactly what data is needed.
 *
 *
 *
 */
public interface IDReferenceData extends ReferenceData {

	/**
	 * @return The id of the specific subset of reference data of this type
	 */
	public ReferenceDataID getID();

	/**
	 * @param id The id of the specific subset of reference data of this type
	 */
	public void setID(ReferenceDataID id);

}
