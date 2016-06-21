package com.aw.unity;

import java.util.UUID;

import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.query.Query;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Runs unity queries against a particular data source.
 *
 * Note that when a runner changes state to complete, it MUST notify all on this object.
 *
 *
 */
public interface UnityRunner {

	public enum ResponseFormat {
		UNITY_DATA, //get the result as a list of Data objects
		UNITY_ROWS, //get the result as a list of Object lists
		POJO, //build pojos from the raw data
		ODATA, //get the results in OData compatible form
	}

	public enum State {

		/**
		 * The runner has been created but not executed yet
		 */
		CREATED,

		/**
		 * Execute has been called, the query is in progress
		 */
		RUNNING,

		/**
		 * The query has been completed, results are available
		 */
		COMPLETE,

		/**
		 * There was an error executing the query
		 */
		ERROR;

		@JsonCreator
		public synchronized static State fromString(String str) {
			return valueOf(str.toUpperCase());
		}

	}

	/**
	 * Initialize the unity runner.
	 *
	 * @param dataSource The data source we are requesting data from
	 * @param query The query we are executing
	 */
	public void initialize(UnityDataSource dataSource, Query query);

	/**
	 * @return The state of this runner
	 */
	public State getState();

	/**
	 * @return The globally unique id for this query
	 */
	public UUID getGuid();

	/**
	 * Run the query, does not block
	 */
	public void execute();

	//TODO: progress, partial results

	/**
	 * Gets the results of the query in the requested format.
	 *
	 * @param format The format of the results
	 * @return The results
	 */
	 public <T extends UnityResults> T getResults(ResponseFormat format) throws Exception;

	/**
	 * Gets the results of the query in pojo form
	 *
	 * @param format The format of the results
	 * @return The results
	 */
	 public <T> PojoUnityResults<T> getResults(Class<T> pojoType) throws Exception;

	/**
	  * @return The error message related to the query run, if any - this would only be meaningful if the runner state is ERROR
	  */
	 public String getErrorMessage();
}
