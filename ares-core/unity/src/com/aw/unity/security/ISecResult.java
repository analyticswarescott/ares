package com.aw.unity.security;

public interface ISecResult {

	/**
	 * Apply security requirements to data rows after retrieval
	 * @param row
	 * @param secDef
	 * @throws Exception
	 */
	public void alterRow(Object row, Object secDef) throws Exception;


	//TODO: likely need to set caches for performance if lookups are required
}
