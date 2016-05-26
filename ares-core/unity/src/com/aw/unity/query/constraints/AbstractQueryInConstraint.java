package com.aw.unity.query.constraints;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aw.unity.UnityInstance;
import org.apache.log4j.Logger;

import com.aw.common.exceptions.InvalidRequestException;
import com.aw.unity.Field;
import com.aw.unity.defaults.DefaultUnityResults;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.QueryAttribute.Order;

/**
 * A query in clause builds a list of values that are returned from the query built from its values. Examples follow:
 *
 * <li> field="user" values=[ "10" ] get 10 values from a straight count query grouped by user
 * <li> field="user" values=[ "10", "avg", "bytes" ] get 10 values from a query calculating average bytes per user
 *
 *
 *
 */
public abstract class AbstractQueryInConstraint extends InConstraint implements QueryConstraint {

	static final Logger logger = Logger.getLogger(AbstractQueryInConstraint.class);

	@Override
	public String printCanonical() {
		return getField() + " in " + getOrder().name().toLowerCase() + " " + m_limit + " " + m_aggregate + " " + m_aggField;
	}

	@Override
	public void validate(String path) {

		//build the path to this constraint
		path = path + " -> (" + printCanonical() + ")";

		if (m_aggField == null) {
			throw new InvalidFilterException("in " + getOrder().name().toLowerCase() + " constraint missing aggregate field", path);
		}

		if (m_aggregate == null) {
			throw new InvalidFilterException("in " + getOrder().name().toLowerCase() + " constraint missing aggregate value", path);
		}

		if (m_limit <= 0) {
			throw new InvalidFilterException("in " + getOrder().name().toLowerCase() + " limit must be a positive value", path);
		}

	}

	@Override
	public Collection<Object> getValues() {

		if (!isReady()) {
			throw new InvalidRequestException(
					"cannot run a query with query constraints that aren't ready (" + getClass().getSimpleName() + ")");
		}

		return m_queryValues;

	}
	@Override
	public void setValuesDefault(Collection<Object> values) {

		m_values = values;

		//initialize ourselves
		parseValues();

	}
	private Collection<Object> m_values;

	protected void parseValues() {

		Iterator<Object> valueIter = m_values.iterator();

		//first value is the number of values to limit the in clause to
		m_limit = Integer.parseInt(String.valueOf(valueIter.next()));

		//by default the aggregate is the count
		m_aggregate = Aggregate.COUNT;

		//by default the aggregate field is the count field - just get a count grouped by the constraint field
		m_aggField = UnityInstance.COUNT_FIELD;

		//if any more values, it must be aggregate and a field
		if (valueIter.hasNext()) {

			m_aggregate = Aggregate.valueOf(String.valueOf(valueIter.next()).toUpperCase());
			m_aggField = getField().getUnity().getMetadata().getFieldRepository().getField(String.valueOf(valueIter.next()));

		}

	}

	/**
	 * @return Whether to order the aggregation ascending or descending
	 */
	protected abstract Order getOrder();

	@Override
	public Query getQuery(Query parent) {

		Query ret = new Query();

		//match data types
		ret.setDataTypes(parent.getDataTypes());

		//set up filter
		Filter filter = parent.getFilter().deepCopy();
		removeNotReady(filter);
		ret.setFilter(filter);

		//set up attributes

		//grouping field is always the constraint field, since these are the values we need
		QueryAttribute attrib = new QueryAttribute(getField(), Order.NONE, Aggregate.NONE);

		//add the aggregate, the numeric attribute that will determine which values make the in clause
		QueryAttribute agg = new QueryAttribute(m_aggField, getOrder(), m_aggregate);

		//set the attribute array
		ret.setAttributes(new QueryAttribute[] { attrib, agg });

		//set the limit
		ret.setSize(m_limit);

		ret.initialize();

		ret.validate();

		//we now have the query
		return ret;

	}

	@Override
	public void processResults(DefaultUnityResults results) {

		Set<Object> values = new HashSet<Object>();

		//get the values
		for (List<Object> row : results) {
			values.add(row.get(0));
		}

		//set the values - we are now ready
		m_queryValues = values;

		logger.info("query values for in clause: " + m_queryValues);

	}

	/**
	 * Remove filter query constraints that aren't ready
	 *
	 * @param filter
	 */
	private void removeNotReady(Filter filter) {

		Iterator<Filter> iter = filter.getFilters().iterator();
		while (iter.hasNext()) {
			Filter cur = iter.next();

			//if we are not ready, remove us
			if (cur instanceof QueryConstraint && !((QueryConstraint)cur).isReady()) {

				iter.remove();

			}

			//else just recurse
			else if (cur instanceof FilterGroup) {

				//recurse
				removeNotReady(cur);

				//remove empty groups
				if (cur.getFilters().size() == 0) {
					iter.remove();
				}

			}

		}

	}

	@Override
	public boolean isReady() {
		//we are ready if we have the values from the query
		return m_queryValues != null;
	}

	/**
	 * @return The values from the query that was executed
	 */
	public Collection<Object> getQueryValues() { return m_queryValues; }
	public void setQueryValues(Collection<Object> queryValues) { m_queryValues = queryValues; }
	private Collection<Object> m_queryValues;

	//the number of values to limit this in clause to
	private int m_limit;

	private Aggregate m_aggregate;

	//the field that will be used to aggregate to determine which values are in the in clause
	Field m_aggField;

}
