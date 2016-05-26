package com.aw.unity;

import javax.inject.Provider;

import com.aw.platform.Platform;
import com.aw.unity.query.Query;

public interface UnityDataSource {

	/**
	 * @return The name of this data source
	 */
	public String getName();

	/**
	 * Initialize the data source given the unity instance
	 *
	 * @param instance The unity instance this data source belongs to
	 */
	public void initialize(UnityInstance instance, Provider<Platform> platform);

	/**
	 * Execute a query against this data source.
	 *
	 * @param query The query
	 * @return The executing query (i.e. runner)
	 */
	public UnityRunner execute(Query query);

	/**
	 * Called by the framework to enforce security around which fields are visible in a query
	 *
	 * @param query
	 * @return
	 */
	public Query enforceFieldSecurity(Query query) throws Exception;

	/**
	 * Called by the framework to enforce security around what data is visible (i.e. which rows are visible)
	 *
	 * @param query
	 * @return
	 */
	public Query enforceDataSecurity(Query query) throws Exception;

	/**
	 * @return The unity instance that data source is associated with
	 */
	public UnityInstance getUnity();

}
