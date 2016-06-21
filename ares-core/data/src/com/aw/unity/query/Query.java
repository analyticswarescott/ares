package com.aw.unity.query;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aw.common.Validatable;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityMetadata;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.query.FilterGroup.GroupOperator;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A query represents a complete request for data from the system. This would include a list of fields (UAttrib), both dimensions and measures,
 * as well as any applicable filter to reduce the results.
 *
 *
 *
 */
public class Query implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	// Should this detail query return all known attributes?
	private boolean allAttributes;

	private UnityMetadata _meta;

    public UnityMetadata getMeta() {
        return _meta;
    }

	public Query () {
	}

	/**
	 * copy constructor for modifications
	 *
	 * NOTE: this does NOT do a deep copy of the query. Any changes to data within the query needs to leave the original query's
	 * contents unmodified.
	 *
	 * @param query
	 */
	public Query(Query query) {
		m_attributes = query.m_attributes;
		m_countOnly = query.m_countOnly;
		m_dataTypes = query.m_dataTypes;
		m_detail = query.m_detail;
		m_dimensions = query.m_dimensions;
		m_filter = query.m_filter;
		m_measures = query.m_measures;
		m_name = query.m_name;
		m_size = query.m_size;
	}

	@Override
	public void validate() {

		//make sure we're initialized
		initialize();

		if (!isDetail()) {

			//validate we have at least one attribute
			if (m_attributes == null) {
				throw new InvalidQueryException("query attributes cannot be missing");
			}

			if (m_attributes.length == 0) {
				throw new InvalidQueryException("query must have at least 1 attribute");
			}

		}

		//validate each attribute if we have any
		if (m_attributes != null) {

			//validate each attribute
			for (QueryAttribute attrib : m_attributes) {
				if (attrib == null) {
					throw new InvalidQueryException("query contains null attribute");
				}
				attrib.validate();
			}

		}

		//validate the filter
		if (m_filter != null) {
			m_filter.validate();
		}

	}

	/**
	 * @return Gets the fields in the order they were requested in the attribute array
	 */
	public List<Field> getFields() {
		List<Field> ret = new ArrayList<>();
		for (QueryAttribute att : m_attributes) {
			ret.add(att.getField());
		}
		return ret;
	}

	/**
	 * Return the data source name required for this query, or null if none could be found
	 */
	public String getDataSource() {

		String ret = null;
		if (getDataTypes() != null) {
			for (DataType dataType : m_dataTypes) {
				if (dataType.getDataSource() != null) {
					ret = dataType.getDataSource();
				}
			}
		}

		return ret;

	}

	/**
	 * Add the given filter as a new constraint on the query. Returns the modified query.
	 *
	 * Note that, as always, the original query remains unchanged.
	 *
	 * @param filter
	 * @return
	 */
	public Query add(Filter filter) {

		Query ret = new Query(this);

		//just build a new filter using the existing one - this will leave the original query unmodified
		Filter original = ret.m_filter;
		ret.m_filter = new FilterGroup(GroupOperator.AND);

		//just and together the two constraints
		ret.m_filter.getFilters().add(original);
		ret.m_filter.getFilters().add(filter);

		//no more changes allowed after initialization
		ret.initialize();

		//return the newly created query
		return ret;

	}

	/**
	 * Get all attributes selected in this query, using the data type fields if nothing else
	 * is specified. Only common fields among the data types will be returned if fallback is
	 * used. Fallback will only be used if both dataTypeFallback is true, and there are
	 * no explicitly defined attributes on the attributes property for this query.
	 *
	 * @param dataTypeFallback Whether to fall back to the data type attributes if no attributes are explicitly defined
	 * @return The attributes requested by this query
	 */
	public List<QueryAttribute> getAttributes(boolean dataTypeFallback) {

		List<QueryAttribute> ret = new ArrayList<>();
		if (getAttributes() != null && getAttributes().length > 0) {
			for (QueryAttribute attrib : getAttributes()) {
				ret.add(attrib);
			}
		}

		//else fall back if requested
		else if (dataTypeFallback) {
			if (m_dataTypes == null || m_dataTypes.length == 0) {
				throw new InvalidQueryException("Query must contain explicitly requested attributes or data types");
			}

			addDataTypeFields(ret);
		}

		return ret;


	}

	/**
	 * Adds data type fields, no duplicates
	 * @param attributes The attribute list to add to
	 */
	private void addDataTypeFields(List<QueryAttribute> attributes) {
		Set<Field> set = new HashSet<>();
		for (DataType dataType : m_dataTypes) {
			for (Field field : dataType.getFields()) {

				//no duplicates
				if (!set.contains(field)) {

					//set default properties for the attribute
					set.add(field);
					attributes.add(new QueryAttribute(field, QueryAttribute.Order.NONE, Aggregate.NONE));

				}

			}
		}
	}

	/**
	 * Once a query is initialized, it is immutable. This call must remain idempotent.
	 */
	public void initialize() {

		//idempotence
		if (m_initialized) {
			return;
		}

		m_dimensions.clear();
		m_measures.clear();
		for (QueryAttribute attrib : getAttributes(true)) {

			//null aggregate is equivalent to NONE
			Aggregate agg = attrib.getAggregate();
			if (agg == null) {
				agg = Aggregate.NONE;
			}

			//timestamp grouping and NONE aggregate types are simply "group by" clauses
			switch (agg) {
				case SECOND:
				case MINUTE:
				case HOUR:
				case DAY:
				case WEEK:
				case MONTH:
				case QUARTER:
				case YEAR:
				case NONE:
					m_dimensions.add(attrib);
					break;
				default:
					m_measures.add(attrib);
					break;
			}

		}

		// Set the all_attributes flag if no attributes have been specified for this detail query
		if (m_detail && m_dimensions.isEmpty() && m_measures.isEmpty()) {
			this.allAttributes = true;
		}

		m_initialized = true;

	}
	private boolean m_initialized = false;

	//throw an exception if initialized
	private void onModify() {
		if (m_initialized)	 {
			throw new UnsupportedOperationException("Cannot modify a query once it is initialized");
		}
	}

	public List<QueryAttribute> getDimensions() { return m_dimensions;	}
	@JsonIgnore
	private List<QueryAttribute> m_dimensions = new ArrayList<QueryAttribute>();

	public List<QueryAttribute> getMeasures() { return m_measures; }
	@JsonIgnore
	private List<QueryAttribute> m_measures = new ArrayList<QueryAttribute>();

	/**
	 * @return The attributes explicitly requested in the query
	 */
	@JsonIgnore
	public QueryAttribute[] getAttributes() { return m_attributes; }
	@JsonIgnore
	public void setAttributes(QueryAttribute[] attributes) { onModify(); m_attributes = attributes; }
	@JsonIgnore
	private QueryAttribute[] m_attributes;

	@JsonIgnore
	public DataType[] getDataTypes() { return m_dataTypes; }
	@JsonIgnore
	public Query setDataTypes(DataType... dataTypes) { onModify(); m_dataTypes = dataTypes; return this; } //builder pattern, return this
	@JsonIgnore
	private DataType[] m_dataTypes;

	/**
	 * @return The name of the query
	 */
	public String getName() { return m_name; }
	public void setName(String name) { m_name = name; }
	private String m_name;

	@JsonIgnore
    public Filter getFilter() { return m_filter; }
	@JsonIgnore
	public void setFilter(Filter filter) { onModify(); m_filter = filter; }
	@JsonIgnore
	private Filter m_filter;

	public boolean isDetail() { return m_detail; }
	public void setDetail(boolean detail) { onModify(); m_detail = detail; }
	private boolean m_detail = false;

	public boolean isAllAttributes() {
		return allAttributes;
	}

	public void setAllAttributes(boolean allAttributes) {
		this.allAttributes = allAttributes;
	}

	public int getSize() { return m_size; }
	public void setSize(int top) { onModify(); m_size = top; }
	private int m_size = 0;

	public boolean getCountOnly() { return m_countOnly; }
	public void setCountOnly(boolean countOnly) { onModify(); m_countOnly = countOnly; }
	private boolean m_countOnly = false;

	public TimeRange getTimeRange() { return m_timeRange; }
	public void setTimeRange(TimeRange timeRange) { m_timeRange = timeRange; }
	private TimeRange m_timeRange;

	/**
	 * @return custom start time
	 */
	public Instant getStartTime() { return m_startTime; }
	public void setStartTime(Instant startTime) { m_startTime = startTime; }
	private Instant m_startTime;

	/**
	 * @return custom end time
	 */
	public Instant getEndTime() { return m_endTime; }
	public void setEndTime(Instant endTime) { m_endTime = endTime; }
	private Instant m_endTime;

	public String getTimeZone() { return m_timeZone; }
	public void setTimeZone(String timeZone) { m_timeZone = timeZone; }
	private String m_timeZone = new SimpleDateFormat("Z").format(new Date()); //default to system time zone

	/**
	 * @return the unity instance this query belongs to
	 */
	@JsonIgnore
	public UnityInstance getUnity() { return this.unity;  }
	public void setUnity(UnityInstance unity) { this.unity = unity; }
	private UnityInstance unity;

	/**
	 * @return query context is an identifier that may be needed within the data source to determine how to execute the query - if unsure what to use for this value, leave it null
	 */
	public String getContext() { return this.context;  }
	public void setContext(String context) { this.context = context; }
	private String context = null;

}
