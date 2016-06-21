package com.aw.unity.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.exceptions.InvalidRequestException;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.unity.UnityDataSource;
import com.aw.unity.UnityResults;
import com.aw.unity.UnityRunner;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.DefaultUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.odata.ODataUnityResults;
import com.aw.unity.query.constraints.QueryConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class for common code related to running queries
 *
 *
 */
public abstract class AbstractUnityRunner implements UnityRunner, SecurityAware {

	public static final Logger logger = LoggerFactory.getLogger(AbstractUnityRunner.class);

	@Override
	public void initialize(UnityDataSource dataSource, Query query) {
		m_dataSource = dataSource;
		m_query = query;
		m_guid = UUID.randomUUID(); //get us a unique id
		m_tenantID = getTenantID();
		m_userID = getUserID();
	}

	@Override
	public UUID getGuid() {
		return m_guid;
	}

	@Override
	public void execute() {

		//build all queries that have to run for this top level query to run
		List<Query> queries = getQueries(m_query.getFilter());

		//add the top level query itself last
		queries.add(m_query);

		//set the iterator for the queries that must run
		m_queries = queries.iterator();
		m_total = queries.size();
		m_completed = 0;

		//start executing
		executeNext();

		//update our state appropriately
		setState(State.RUNNING);

	}

	private void executeNext() {

		//get the query to execute next
		m_curQuery = getFinalQuery(m_queries.next());

		//execute the query
		execute(m_curQuery);

	}

	/**
	 * Get all queries that are needed to complete this filter
	 *
	 * @param filter The filter to run
	 * @return The list of queries that must run to complete this filter
	 */
	private List<Query> getQueries(Filter filter) {

		List<Query> ret = new ArrayList<Query>();
		getQueries(filter, ret);
		return ret;

	}

	private void getQueries(Filter filter, List<Query> queries) {

		if (filter instanceof QueryConstraint) {

			//add the constraint query to the list
			QueryConstraint constraint = (QueryConstraint)filter;
			Query query = constraint.getQuery(m_query);
			queries.add(query);

			//cache the mapping to the query constraint
			m_queryConstraints.put(query, constraint);

		}

		//else recurse
		else {

			for (Filter child : filter.getFilters()) {
				getQueries(child, queries);
			}

		}

	}

	@Override
	public <T extends UnityResults> T getResults(ResponseFormat format) throws Exception {

		//make sure we're completed
		checkComplete();

		//we're compelted, get the results
		switch (format) {
			case ODATA: return (T)getOData(getFinalQuery(m_query));
			case UNITY_ROWS: return (T)getRowData(getFinalQuery(m_query));
			case UNITY_DATA: return (T)getData(getFinalQuery(m_query));
			default: throw new InvalidQueryException("Unsupported format for query: " + format);
		}
	}

	@Override
	public <T> PojoUnityResults<T> getResults(Class<T> pojoType) throws Exception {

		checkComplete();

		return getPojoData(getFinalQuery(m_query), pojoType);

	}

	private void checkComplete() throws Exception {
		if (m_state != State.COMPLETE) {
			throw new InvalidRequestException("query must be completed before getting the results");
		}
	}

	/**
	 * @return The final query that will actually be used to get data from the data source
	 */
	protected Query getFinalQuery(Query query) {

		//TODO: apply security

		//query = m_dataSource.getRowSecurityClass().enforceRowSecurity(m_query, context, inst.getSecurityRoleResolver(context.getTenantID()));
		//query = m_dataSource.getColSecurityClass().enforceColumnSecurity(m_query, context);

		return query;

	}

	/**
	 * Called when the latest executed query is complete
	 */
	protected void onQueryComplete() {

		try {

			//make sure we're set up as the tenant/user
			SecurityUtil.setThreadSystemAccess();
			Impersonation.impersonateTenant(m_tenantID, m_userID);

			//query is complete, increase counter
			m_completed++;

			//see if we need to update a query constraint
			QueryConstraint constraint = m_queryConstraints.get(m_curQuery);
			if (constraint != null) {
				logger.info("applying constraint query results to " + constraint);
				constraint.processResults(getRowData(m_curQuery));
			}

			//we're complete if all queries are complete
			if (m_completed == m_total) {
				setState(State.COMPLETE);
			}

			//else run the next query
			else {
				executeNext();
			}

		} catch (Exception e) {

			//TODO: store the exception to return it to the caller
			logger.error("error completing query", e);
			setState(State.ERROR);

		}

	}

	/**
	 * Execute the query as-is - any security constraints have already been applied
	 *
	 * @param query The query ready to run
	 */
	protected abstract void execute(Query query);

	/**
	 * @return Data in unity format
	 */
	protected abstract DefaultUnityResults getRowData(Query query);

	/**
	 * @return Data in unity format
	 */
	protected abstract DataUnityResults getData(Query query);

	/**
	 * @return Data in OData format
	 */
	protected abstract ODataUnityResults getOData(Query query);

	/**
	 * @param query The query that was used to get the results
	 * @param type The type of pojos to return
	 * @return The pojo unity results
	 */
	protected abstract <T> PojoUnityResults<T> getPojoData(Query query, Class<T> type);

	/**
	 * @return The data source being used by this runner
	 */
	@JsonIgnore
	public UnityDataSource getDataSource() { return m_dataSource; }
	private UnityDataSource m_dataSource;

	public State getState() { return m_state; }
	protected void setState(State state) {

		//if we are complete, notify any threads waiting about state changes
		synchronized (this) {
			m_state = state;

			notifyAll();
		}

	}
	private State m_state = State.CREATED;

	/**
	 * @return The query itself, available to subclasses
	 */
	protected Query getQuery() { return m_query; }

	//the runner guid
	private UUID m_guid;

	//the top level query
	private Query m_query;

	//the current query executing
	private Query m_curQuery = null;

    //the queries to execute
    private Iterator<Query> m_queries = null;

    //the queries that have been completed
    private int m_completed = 0;

    //the total number of queries that have to complete
    private int m_total = 0;

    //the map of queries to query constraints
    private Map<Query, QueryConstraint> m_queryConstraints = new HashMap<Query, QueryConstraint>();

    private String m_tenantID;
    private String m_userID;

}
