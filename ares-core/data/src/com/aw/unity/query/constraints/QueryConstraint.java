package com.aw.unity.query.constraints;

import com.aw.unity.defaults.DefaultUnityResults;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.Query;

/**
 * A constraint that must issue a query to be applied
 *
 *
 *
 */
public interface QueryConstraint extends FilterConstraint {

	/**
	 * @return The constraint as a query, given the parent query
	 */
	public Query getQuery(Query parent);

	/**
	 * When this query constraint's results are complete, this method will be used to feed those results back to the constraint.
	 *
	 * @param results
	 */
	public void processResults(DefaultUnityResults results);

	/**
	 * @return Whether this constraint needs to issue its query or not - generally, once processResults() is called, thsi should always return true
	 */
	public boolean isReady();

}
