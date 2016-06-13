package com.aw.unity.es;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aw.common.util.es.ESKnownIndices;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalMultiBucketAggregation;
import org.elasticsearch.search.aggregations.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.VersionedObject;
import com.aw.unity.Data;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.UnityDataSource;
import com.aw.unity.UnityInstance;
import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.DefaultUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.dg.CommonField;
import com.aw.unity.exceptions.DataSourceException;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.exceptions.InvalidQueryException;
import com.aw.unity.exceptions.QueryExecutionException;
import com.aw.unity.json.JSONDataType;
import com.aw.unity.odata.ODataUnityResults;
import com.aw.unity.query.AbstractUnityRunner;
import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.FilterGroup;
import com.aw.unity.query.Query;
import com.aw.unity.query.QueryAttribute;
import com.aw.unity.query.QueryAttribute.Aggregate;
import com.aw.unity.query.QueryAttribute.Order;
import com.aw.unity.query.datatypes.TimestampBetweenHandler;
import com.aw.unity.query.datatypes.TimestampGteLteHandler;
import com.aw.unity.query.datatypes.TimestampInEqHandler;

/**
 * A runner for elasticsearch within Unity.
 *
 * @author shill, jlehmann
 */
public class ElasticUnityRunner extends AbstractUnityRunner implements ActionListener<SearchResponse>, TenantAware {

    public static final Logger logger = Logger.getLogger(ElasticUnityRunner.class);

    private static final String SOURCE_DOCUMENT = "_source";
    private static final String TYPE_FIELD = "_type";

    private UnityInstance unity;

    public ElasticUnityRunner() {
    }

    @Override
    public void initialize(UnityDataSource dataSource, Query query) {
    	super.initialize(dataSource, query);

    	this.unity = dataSource.getUnity();

    	//set a typed version here
    	elastic = (ElasticUnityDataSource)dataSource;
    }

    @Override
    protected void execute(Query query) {

    	try {

    		//run the query, save the future
        	future = runQuery(query);

        	//listen for updates
        	future.addListener(this);

    	} catch (Exception e) {
    		throw new QueryExecutionException("Error running unity query", e);
    	}

    }
    private ListenableActionFuture<SearchResponse> future;

    @Override
    public void onFailure(Throwable e) {
    	m_failure = e;

    	logger.error("Error executing query", new Exception(e));

    	//set our state accordingly
    	setState(State.ERROR);
    }
    private Throwable m_failure;

    /**
     * When we are called back to handle the response just store it
     */
    @Override
    public void onResponse(SearchResponse response) {

    	//we were called back with the response - rememeber it and update our state accordingly
    	m_response = response;

    	//call the framework to tell it the query has completed
    	onQueryComplete();

    }

    /**
	 * If we already have it return it, otherwise this will block.
	 *
     * @return The search response
     */
    private SearchResponse getResponse() {
    	try {

    		//get the response or wait for it if it's not ready yet
    		SearchResponse response = m_response != null ? m_response : future.actionGet();

        	return response;

    	} catch (Exception e) {
    		throw new QueryExecutionException("Error executing query", e);
    	}
    }
    SearchResponse m_response;

    @Override
    public String getErrorMessage() {
    	if (m_failure != null) {
    		return m_failure.getMessage();
    	}

    	else {
    		return null;
    	}
    }

    @Override
    protected ODataUnityResults getOData(Query query) {

//    	//check that everything is ready
//    	if (m_future.isDone() && m_future.getRootFailure() != null) {
//    		throw new QueryExecutionException("Error running query", m_future.getRootFailure());
//    	}
//
//    	//use the response if we already got it, or wait for it
//		SearchResponse response = getResponse();
//
//    	if (query.isDetail()) {
//
//            List<Map<String, Object>> ret = new ArrayList<>();
//            SearchHits hits = response.getHits();
//
//            int rownum = 1;
//            for (SearchHit hit : hits) {
//                Map<String, Object> m = hit.sourceAsMap();
//                if ( m == null ) {
//                    final Map<String, Object> toPut = new HashMap<>();
//                    hit.fields().keySet().stream()
//                            .forEach((key) -> toPut.put(key, hit.fields().get(key).getValue()));
//                    m = toPut;
//                }
//                m.put("GenKey", Integer.toString(rownum));
//                ret.add(m);
//                rownum++;
//            }
//
//            return new ODataUnityResults(ret);
//        } else {
//            throw new InvalidQueryException(" Odata for ES aggregate (detail:false) queries not yet supported");
//        }
    	throw new UnsupportedOperationException("not implemented yet");

    }

    @Override
    protected DefaultUnityResults getRowData(Query query) {

    	try {

            List<List<Object>> flat = null;
            SearchResponse response = getResponse();

            if (query.isDetail()) {
                flat = get2DDetail(response, query);
            }

            if (!query.isDetail()) {
                //flatten to 2D
                flat = get2D(response, query);
            }

            //build the unity format response json
            DefaultUnityResults results = new DefaultUnityResults(query.getFields(), flat);

            return results;

    	} catch (Exception e) {
    		throw new QueryExecutionException("error building results", e);
    	}

    }

    @Override
    protected DataUnityResults getData(Query query) {

    	List<Data> results = new ArrayList<Data>();

    	try {

        	SearchResponse response = getResponse();

        	for (SearchHit hit : response.getHits()) {

            	//reconstitute from json

            	JSONObject jsonData = new JSONObject(hit.sourceAsString());

            	//build the next data instance
            	DataType type = unity.getMetadata().getDataType(hit.type());
            	Data data = unity.newData(jsonData, type);

            	results.add(data);

        	}

    	} catch (Exception e) {
    		e.printStackTrace();

    		throw new QueryExecutionException("error building results", e);
    	}

    	return new DataUnityResults(results);

    }

    @Override
    protected <T> PojoUnityResults<T> getPojoData(Query query, Class<T> type) {

    	List<VersionedObject<T>> pojos = new ArrayList<VersionedObject<T>>();

    	try {

        	SearchResponse response = getResponse();

        	for (SearchHit hit : response.getHits()) {

            	//reconstitute from json

            	JSONObject jsonData = new JSONObject(hit.sourceAsString());

            	//build the next data instance
            	DataType dataType = unity.getMetadata().getDataType(hit.type());
            	Data data = unity.newData(jsonData, dataType);

            	//ask unity to convert data to pojo
            	T pojo = unity.toPojo(data, type);

            	//add the resulting pojo
            	pojos.add(new VersionedObject<T>(hit.getVersion(), pojo));

        	}

    	} catch (Exception e) {
    		e.printStackTrace();

    		throw new QueryExecutionException("error building results", e);
    	}

    	//return the results
    	return new PojoUnityResults<>(pojos);

    }

    private ListenableActionFuture<SearchResponse> runQuery(Query query) throws Exception {

    	SearchRequestBuilder sr = buildSearchRequest(query);

    	//include version
    	sr.setVersion(true);

    	//run the query
    	return sr.execute();

    }

    public SearchRequestBuilder buildSearchRequest(Query query) throws Exception {

    	//start with the filter
        QueryBuilder queryBuilder = processFilters(query);

        // index name is entity namespace for ES queries -- tenantID is index name prefix
        String[] indices = getIndices(Tenant.forId(getTenantID()), query);

        SearchRequestBuilder sr = elastic.getClient().prepareSearch(indices);

        //types are the es types
        if (query.getDataTypes() != null && query.getDataTypes().length > 0) {
        	List<String> types = new ArrayList<String>();
        	for (DataType type : query.getDataTypes()) {
        		types.add(type.getName());
        	}
        	sr.setTypes(types.toArray(new String[types.size()]));
        }

		sr.setSize(query.getSize() == 0 ? 10 : query.getSize());

        //add filters
        sr.setQuery(queryBuilder);

        //add aggs if required
        if (!query.isDetail()) {

			if (query.getDimensions().isEmpty()) {
				addAggsOnly(sr, query);
			} else {
				sr = addTermsAndAggs(sr, query);
			}

        }

        else {

        	//limit to only query fields to minimize bandwidth
            for (QueryAttribute attribute : query.getAttributes(false)) {

            	//don't include the reserved count field - shouldn't be here anyway
            	if (!attribute.getField().getName().equals(UnityInstance.COUNT_FIELD.getName())) {
					final String jsonPath = getJsonPath(query, attribute.getField());

					// No need to add this attribute, we are going to be adding all attributes below
					if (!query.isAllAttributes()) {
						sr.addField(jsonPath);
					}

					// Add sorting, if specified
					if (Order.NONE != attribute.getOrder()) {
						sr.addSort(jsonPath, attribute.getOrder() == Order.DESC ? SortOrder.DESC : SortOrder.ASC);
					}

            	}

            }

            // if a detail query with all_attributes set to true, get _source
            if (query.isAllAttributes()) {
            	sr.addField("_version");
            	sr.addField(TYPE_FIELD);
            	sr.addField(SOURCE_DOCUMENT);
            }

        }

        return sr;

    }

	/**
	 * Adds aggregates to the search request.  Use this method when there are no dimensions to project these values to.
	 * @param sr The search request to add aggregates to.
	 * @param query The query containing any aggregates.
     */
	private void addAggsOnly(SearchRequestBuilder sr, Query query) {
		//now add the measures
		for (QueryAttribute measure : query.getMeasures()) {

			final String name = getJsonPath(query, measure.getField());
			final String aggName = getAggregateName(measure);

			switch (measure.getAggregate()) {
				case AVG: {
					final AvgBuilder avgBuilder = AggregationBuilders.avg(aggName).field(name);
					sr.addAggregation(avgBuilder);
					break;
				}
				case SUM: {
					final SumBuilder sumBuilder = AggregationBuilders.sum(aggName).field(name);
					sr.addAggregation(sumBuilder);
					break;
				}
				case COUNT_DISTINCT: {
					final CardinalityBuilder cardinalityBuilder = AggregationBuilders.cardinality(aggName).field(name).precisionThreshold(1000);
					sr.addAggregation(cardinalityBuilder);
					break;
				}
				case COUNT: {
					final ValueCountBuilder countBuilder = AggregationBuilders.count(aggName).field(name);
					sr.addAggregation(countBuilder);
					break;
				}
				default:
					throw new IllegalArgumentException("URunElastic: unsupported ES measure aggregation type");

			}

		}
	}

	String[] getIndices(Tenant tenant, Query query) throws DataSourceException, ConfigurationException {

		Set<ESKnownIndices> indices = new HashSet<ESKnownIndices>();

		//use the requested index if applicable, it will be in the context
		if (query.getContext() != null) {
			indices.add(getContext(query));
		}

		//if no data types, use the default index
		else if (query.getDataTypes() == null || query.getDataTypes().length == 0) {
    		indices.add(elastic.getDefaultIndex());
    	}

		//else use indices based on the data types
		else {
    		for (DataType dataType : query.getDataTypes()) {
    			ESKnownIndices index = elastic.getIndex(dataType);

    			//should not be null
    			if (index != null) {
    				indices.add(index);
    			}
    		}
    	}

    	//TODO: multi-tenant queries

    	//look for a dg_time filter which is what we time slice on
    	Collection<FilterConstraint> timeConstraints = query.getFilter().findAll(CommonField.DG_TIME.asField());

    	//return list
    	List<String> ret = new ArrayList<String>();

    	for (ESKnownIndices index : indices) {

    		//add index
    		ret.add(index.getAllIndices(tenant));

    	}

    	//convert to array and return
    	return ret.toArray(new String[ret.size()]);

    }

    private SearchRequestBuilder addTermsAndAggs(SearchRequestBuilder sr, Query query) throws Exception {

    	ValuesSourceAggregationBuilder<?> current = null;
        ValuesSourceAggregationBuilder<?> root = null;

        //first add the dimensions
        for (QueryAttribute dim : query.getDimensions()) {

        	//get the field and aggregate type
            Field field = dim.getField();
            Aggregate agg = dim.getAggregate() == null ? Aggregate.NONE : dim.getAggregate();
            String aggName = getAggregateName(dim);

        	//if it's a date grouping we need to figure that out here
        	DateHistogramInterval dateInterval = determineDateInterval(dim.getAggregate());

        	//if it's a date grouping, do that here
        	ValuesSourceAggregationBuilder<?> sub = null;
        	if (dateInterval != null) {
            	sub = AggregationBuilders.dateHistogram(aggName).
            			timeZone(query.getTimeZone()).
            			field(getJsonPath(query, field)).
            			interval(dateInterval).
            			format(agg.getDateFormat());
        	}


        	else {
            	TermsBuilder builder = AggregationBuilders.terms(getAggregateName(dim))
                        .field(getJsonPath(query, field))
                        .size(0);

            	sub = builder;
        	}

            if (current != null) {

            	//add the sub aggregation
                current.subAggregation(sub);

                //move down the tree
                current = sub;


            } else {

            	current = sub;

                root = current;

            }

        }

		// This should never happen.  addAggsOnly() handles the case where we have no dimensions to project aggregates against
		if (current == null) {
			throw new IllegalArgumentException("at least one dimension is required!");
		}

        //we need the last term for ordering, potentially
        TermsBuilder lastTerm = current instanceof TermsBuilder ? (TermsBuilder)current : null;

        //now add the measures
        for (QueryAttribute measure : query.getMeasures()) {

			Order order = measure.getOrder() == null ? Order.NONE : measure.getOrder();
			String name = measure.getField().getName();
			String aggName = getAggregateName(measure);

			switch (measure.getAggregate()) {

				case AVG:
					current.subAggregation(AggregationBuilders.avg(aggName).field(name));
					break;

				case SUM:
					current.subAggregation(AggregationBuilders.sum(aggName).field(name));
					break;

				case COUNT_DISTINCT:
					//TODO: allow setting precision threshhold and rehash attributes of this agg
					current.subAggregation(AggregationBuilders.cardinality(aggName).field(name).precisionThreshold(1000));
					break;

				case COUNT:
					//apply count ordering to the last dimension in the grouped query - this will order the document count
					break;

				default:
					throw new Exception("URunElastic: unsupported ES measure aggregation type");

			}

			if (order != Order.NONE && lastTerm != null) {
				lastTerm.order(toOrder(aggName, measure.getAggregate(), order));
			}


        }

        if (root != null) {
        	sr.addAggregation(root);
        }

        //don't worry about hits, only get aggregated data
        sr.setSearchType(SearchType.QUERY_THEN_FETCH);
        sr.setSize(0); //everything

        return sr;
    }

    private org.elasticsearch.search.aggregations.bucket.terms.Terms.Order toOrder(String aggName, Aggregate agg, Order order) throws Exception {

    	org.elasticsearch.search.aggregations.bucket.terms.Terms.Order ret = null;

    	switch (agg) {
    		case COUNT: ret = org.elasticsearch.search.aggregations.bucket.terms.Terms.Order.count(order == Order.ASC); break;
    		default: ret = org.elasticsearch.search.aggregations.bucket.terms.Terms.Order.aggregation(aggName, "value", order == Order.ASC); break;
    	}

    	return ret;

    }

    /**
     * Given the aggregate type, get the date interval for elasticsearch
     *
     * @param agg
     * @return
     */
    private DateHistogramInterval determineDateInterval(Aggregate agg) {
    	DateHistogramInterval dateInterval = null;
    	switch (agg) {
            case SECOND:
            	dateInterval = DateHistogramInterval.SECOND;
            	break;
            case MINUTE:
            	dateInterval = DateHistogramInterval.MINUTE;
            	break;
            case HOUR:
            	dateInterval = DateHistogramInterval.HOUR;
            	break;
            case DAY:
            	dateInterval = DateHistogramInterval.DAY;
            	break;
            case WEEK:
            	dateInterval = DateHistogramInterval.WEEK;
            	break;
            case MONTH:
            	dateInterval = DateHistogramInterval.MONTH;
            	break;
            case QUARTER:
            	dateInterval = DateHistogramInterval.QUARTER;
            	break;
            case YEAR:
            	dateInterval = DateHistogramInterval.YEAR;
            	break;
            default:
            	//no date interval in this case
            	break;
    	}
    	return dateInterval;
    }

    /**
     * For aggregations we support one path, so find a path and return it
     *
     * @param query
     * @param field
     * @return
     */
    protected String getJsonPath(Query query, Field field) {

    	DataType[] types = unity.getMetadata().getDataTypeRepository().getDataTypes();
    	boolean defaultIndex = true;
    	if (query != null) {
    		if (query.getDataTypes() != null && query.getDataTypes().length > 0) {
	    		types = query.getDataTypes();
	    		defaultIndex = false;
	    	} else if (query.getContext() != null) {
	    		defaultIndex = false;
	    	}
    	}

    	//see if we have multiple paths
    	Set<String> paths = new HashSet<String>();

		//use the first path we find - we only support one path in the case of grouping
		for (DataType type : types) {

			//if we are looking only at default indexes, make sure the type maps to the default index
			boolean shouldCheck = false;
			if (defaultIndex) {
				ESKnownIndices index =  elastic.getIndex(type);

				//if index is null in the mapping, it's a default index type
				shouldCheck = index == null || index.equals(elastic.getTenantDefaultIndex());
			}

			//if context is set, check those types
			else if (query.getContext() != null) {
				ESKnownIndices context = getContext(query);
				shouldCheck = elastic.getIndex(type) == context;
			}

			//else just check
			else {
				shouldCheck = true;
			}

			//once we find a type with this field, just return the path for it
			if (shouldCheck && type.hasField(field)) {
				paths.add(StringUtils.join(((JSONDataType)type).getPath(field), "."));
			}

		}

		if (paths.size() > 0) {

			//if the name is a path, return that
			if (paths.contains(field.getName())) {
				return field.getName();
			}

			else {
				//else just return the first path
				return paths.iterator().next();
			}

		}

		else {

			throw new InvalidQueryException("could not find field in any data type for this query (" + field + ")");

		}

    }

    private ESKnownIndices getContext(Query query) {
    	return ESKnownIndices.valueOf(query.getContext().toUpperCase());
    }

    private List<List<Object>> get2DDetail(SearchResponse sr, Query query) throws Exception {

    	List<List<Object>> ret = new ArrayList<List<Object>>();

        SearchHits hits = sr.getHits();

        for (SearchHit hit : hits) {

            List<Object> row = new ArrayList<Object>();
            Map<String, ?> m = hit.sourceAsMap();
            if (m == null) {
                m = hit.fields();
            }

            for (QueryAttribute attrib : query.getAttributes(true)) {
                addColToRow(attrib, m, row);
            }

            ret.add(row);

        }

        return ret;

    }

    private void addColToRow(QueryAttribute att, Map<String, ?> m, List<Object> row) throws Exception {

        //if this attribute is of type constant, then grab the correct value
//        if (UAttrib.ROLE_CONSTANT.equalsIgnoreCase(att.getRole())) {
//            r.put(alias, att.getConstant());
//            return;
//        }

        Object val = m.get(att.getField().getName());
        if (val instanceof SearchHitField) {
            SearchHitField hf = (SearchHitField) val;
            val = hf.getValues().get(0).toString();
        }


        row.add(val);

    }

    private List<List<Object>> get2D(SearchResponse sr, Query query) throws Exception {

    	if (sr == null) {
    		//empty array
    		return new ArrayList<>();
    	}

        //the final results
        List<List<Object>> ret = new ArrayList<>();

        //now for this set of aggregate names, build the rows
        toResults(sr.getAggregations(), query, new HashMap<Field, Object>(), ret, 0, null);

        //top N
        if (query.getSize() > 0) {

        	//reduce to first N if applicable
        	if (ret.size() > query.getSize()) {
        		ret = ret.subList(0, query.getSize());
        	}

        }

        return ret;

    }

    private void toResults(Aggregations aggregations, Query query, Map<Field, Object> values, List<List<Object>> results, int dimension, MultiBucketsAggregation.Bucket parent) throws Exception {

    	if (dimension < query.getDimensions().size()) {

    		QueryAttribute dim = query.getDimensions().get(dimension);

    		//get the next group of buckets
    		InternalMultiBucketAggregation<?, ? extends InternalTerms.Bucket> agg = aggregations.get(getAggregateName(dim));

    		if (agg == null) {
    			throw new QueryExecutionException("could not find expected aggregation: " + getAggregateName(dim));
    		}

    		//process the buckets
    		for (MultiBucketsAggregation.Bucket bucket : agg.getBuckets()) {

    			//push the value
    			values.put(dim.getField(), bucket.getKeyAsString());

	    		//process the next level in, if any
	    		toResults(bucket.getAggregations(), query, values, results, dimension + 1, bucket);

	    		//pop the value
	    		values.remove(dim.getField());

    		}

    	}

    	//else finish the row with the measures
    	else {
    		addMeasures(values, query, aggregations, parent, results);
    	}

    }

    private void addMeasures(Map<Field, Object> values, Query query, Aggregations aggs, MultiBucketsAggregation.Bucket parent, List<List<Object>> rows) {

    	Iterator<Aggregation> aggIter = aggs.iterator();
    	for (QueryAttribute attrib : query.getMeasures()) {

    		Aggregate agg = attrib.getAggregate();
    		if (attrib.getAggregate() == null) {
    			agg = Aggregate.NONE;
    		}

			if (parent == null) {
				// No parent document count to grab
				final Aggregation aggregation = aggs.get(getAggregateName(attrib));
				values.put(attrib.getField(), ((SingleValue) aggregation).value());
			} else {
				switch (agg) {

					case COUNT:
						//just use doc count for count
						values.put(attrib.getField(), parent.getDocCount());
						break;

					default:
						//else it should be a single value
						Aggregation svAgg = aggIter.next();

						//get the number
						values.put(attrib.getField(), ((SingleValue) svAgg).value());

						break;

				}
			}

    	}

    	//create the new result row in the originally requested order
    	List<Object> row = new ArrayList<Object>(values.size());
    	for (QueryAttribute attrib : query.getAttributes()) {

    		Object value = values.get(attrib.getField());
    		row.add(value);

    	}
    	rows.add(row);

    	//now remove the measures from our map
    	for (QueryAttribute field : query.getMeasures()) {
    		values.remove(field.getField());
    	}

    }

    private String getAggregateName(QueryAttribute attrib) {
    	return attrib.getField().getName() + ":" + attrib.getAggregate();
    }

    /**
     * Build a single grouped row of multiple dimensions and a single measure
     *
     * @param dimensions The dimensions
     * @param measure The measure
     * @return The json array holding these values
     */
    private JSONArray buildRow(LinkedList<String> dimensions, String measure) {
    	JSONArray ret = new JSONArray(dimensions);
    	ret.put(measure);
    	return ret;
    }

    private QueryBuilder processFilters(Query query) throws Exception {

        Filter fg = query.getFilter();
    	QueryBuilder ret = toQueryBuilder(fg);

    	//if no filter at all, match all
    	if (ret == null) {
    		ret = QueryBuilders.matchAllQuery();
    	}

    	return ret;

    }

    public QueryBuilder toQueryBuilder(Filter filter) throws Exception {

    	QueryBuilder ret = null;

    	//return null if the filter is disabled
    	if (filter == null) {
    		//match all if there is no filter
    		return ret;
    	}

    	else if (!filter.isEnabled()) {
    		return ret;
    	}

    	if (filter instanceof FilterConstraint) {
    		ret = toQueryBuilder((FilterConstraint)filter);
    	}

    	else if (filter instanceof FilterGroup) {
    		ret = toQueryBuilder((FilterGroup)filter);
    	}

    	else {
    		throw new Exception("Unrecognized filter type: " + filter.getClass().getName());
    	}

        //apply not
        if (ret != null && filter.isNot()) {
        	ret = QueryBuilders.notQuery(ret);
        }

    	return ret;

    }

    /**
     * Build a QueryBuilder from a group.
     *
     * @param filter
     * @return
     * @throws Exception
     */
    private QueryBuilder toQueryBuilder(FilterGroup filter) throws Exception {

    	//if empty, pass all
    	if (filter.getFilters().size() == 0) {
    		return null;
    	}

    	QueryBuilder ret = null;
    	for (Filter child : filter.getFilters()) {

    		//get the child filter builder
    		QueryBuilder fb = toQueryBuilder(child);

    		//skip empty constraints
    		if (fb == null) {
    			continue;
    		}

    		//create the appropriate group for these children
    		//have to take this approach because for some reason elasticsearch
    		//has no GroupQueryBuilder interface with an add() method
    		if (ret == null) {

    			//just one for now, no bool needed - singleton
				ret = fb;

    		}

    		else {

    			//make a bool when needed
    			if (!(ret instanceof BoolQueryBuilder)) {
	    			switch (filter.getOperator()) {

	        			case AND:
	        				//all must match
	        				ret = QueryBuilders.boolQuery().must(ret);
	        				break;

	        			case OR:
	        				//any 1 can match
	        				ret = QueryBuilders.boolQuery().should(ret);
	        				break;

	    			}
    			}

    			//we now need to add the new one to the bool
    			switch (filter.getOperator()) {

	    			case AND:
	    				((BoolQueryBuilder)ret).must(fb);
	    				break;

	    			case OR:
	    				((BoolQueryBuilder)ret).should(fb);
	    				break;

    			}

			}

    	}

    	return ret;
    }

    /**
     * Build a QueryBuilder from a constraint, or leaf node, in the filter tree.
     *
     * @param constraint
     * @return
     * @throws Exception
     */
    public QueryBuilder toQueryBuilder(FilterConstraint constraint) throws Exception {

    	QueryBuilder ret = null;

    	Field field = constraint.getField();
        ConstraintOperator op = constraint.getOperator();
        Collection<Object> values = constraint.getValues();

        //make sure nothing is null
        if (field == null) {
        	throw new InvalidQueryException("field cannot be null on constraint");
        }

        if (op == null) {
        	throw new InvalidQueryException("operator cannot be null on constraint");
        }

        if (values == null) {
        	throw new InvalidQueryException("value(s) cannot be null on constraint");
        }

        Iterator<Object> valueIter = values.iterator();

        //begin to handle operators
        switch (op) {

        	case GTE:

        		if (field.getType() == FieldType.TIMESTAMP) {
        			ret = handleTimestampGteLte(constraint);
        		}

        		else {
        			ret = addConstraint(field, (String s) -> QueryBuilders.rangeQuery(s).gte(Double.parseDouble(valueIter.next().toString())));
        		}
        		break;

        	case LTE:

        		if (field.getType() == FieldType.TIMESTAMP) {
        			ret = handleTimestampGteLte(constraint);
        		}

        		else {
        			ret = addConstraint(field, (String s) -> QueryBuilders.rangeQuery(s).lte(Double.parseDouble(valueIter.next().toString())));
        		}

        		break;

	        case BETWEEN:

	        	if (field.getType() == FieldType.TIMESTAMP) {
	        		ret = handleTimestampBetween(constraint);
	        	}

	        	else {

		        	//TODO: support other numeric types
		        	ret = addConstraint(field, (String s) -> QueryBuilders.rangeQuery(s)
		        						.gte(((Number)valueIter.next()).doubleValue())
		        						.lt(((Number)valueIter.next()).doubleValue()));
	        	}

	        	break;

	        case EQ:

	        	if (field.getType() == FieldType.TIMESTAMP) {
	        		ret = handleTimestampInEq(constraint);
	        	}

	        	else {
		            ret = addConstraint(field, (String s) -> QueryBuilders.termsQuery(s, constraint.getValues()));
	        	}

	        	break;

	        case EXISTS:

	        	ret = addConstraint(field, (String s) -> QueryBuilders.existsQuery(s));
	        	break;

	        case CONTAINS:

	        	ret = addConstraint(field, (String s) -> QueryBuilders.wildcardQuery(s, "*" + valueIter.next() + "*"));
	        	break;

	        case IN_BOTTOM:
	        case IN_TOP:
	        case IN:

	        	if (field.getType() == FieldType.TIMESTAMP) {
	        		ret = handleTimestampInEq(constraint);
	        	}

	        	else {
	        		ret = addConstraint(field, (String s) -> QueryBuilders.termsQuery(s, constraint.getValues()));
	        	}

	        	break;

	        case STARTS_WITH:

	        	ret = addConstraint(field, (String s) -> QueryBuilders.prefixQuery(s, valueIter.next().toString()));
	        	break;

	        case MATCHES:

	        	ret = addConstraint(field, (String s) -> QueryBuilders.regexpQuery(s, valueIter.next().toString()));
	        	break;

        }

        return ret;

    }

    /**
     * Handle custom elasticsearch timestamp filtering
     *
     * @param constraint The timestamp constraint
     * @return The query builder for this constraint
     */
    private QueryBuilder handleTimestampBetween(FilterConstraint constraint) {
    	Field field = constraint.getField();

    	Instant start = TimestampBetweenHandler.getStartTime(constraint);
    	Instant end = TimestampBetweenHandler.getEndTime(constraint);
    	return addConstraint(field, (String s) -> QueryBuilders.rangeQuery(s).gte(start.toEpochMilli()).lt(end.toEpochMilli()));
    }

    /**
     * Handle custom elasticsearch timestamp filtering
     *
     * @param constraint The timestamp constraint
     * @return The query builder for this constraint
     */
    private QueryBuilder handleTimestampInEq(FilterConstraint constraint) {
    	Field field = constraint.getField();

    	Instant start = TimestampInEqHandler.getStartTime(constraint);
    	Instant end = TimestampInEqHandler.getEndTime(constraint);

    	QueryBuilder ret = addConstraint(field,  new ConstraintGenerator() {

			@Override
			public QueryBuilder generate(String jsonPath) {
		    	RangeQueryBuilder qb = QueryBuilders.rangeQuery(jsonPath).gte(start.toEpochMilli());
		    	if (end != null) {
		    		qb = qb.lt(end.toEpochMilli());
		    	} else {
		    		qb = qb.lte("now");
		    	}
		    	return qb;
			}

		});

    	return ret;

    }

    /**
     * Handle custom elasticsearch timestamp filtering
     *
     * @param constraint The timestamp constraint
     * @return The query builder for this constraint
     */
    private QueryBuilder handleTimestampGteLte(FilterConstraint constraint) {
    	Field field = constraint.getField();

    	Instant date = TimestampGteLteHandler.getTimestamp(constraint);

    	switch (constraint.getOperator()) {
    		case GTE:
    	    	return addConstraint(field, (s) -> QueryBuilders.rangeQuery(s).gte(date.toEpochMilli()));
    		case LTE:
    	    	return addConstraint(field, (s) -> QueryBuilders.rangeQuery(field.getName()).lte(date.toEpochMilli()));
    		default:
    			throw new InvalidFilterException("unsupported constraint operator here: " + constraint.getOperator().name().toLowerCase());
    	}
    }

    /**
     * Generate a constraint for each json path based on the data types in this query. The point of this
     * method is to support the possibility that the same unity field exists in different json structures
     * in the same query.
     *
     * @param field
     * @param generator
     */
    private QueryBuilder addConstraint(Field field, ConstraintGenerator generator) {

    	//if no query, just assume the field name equals the path since we have no more information on types
    	if (getQuery() == null) {
    		return generator.generate(getJsonPath(getQuery(), field));
    	}

    	//key off of path to detect if we have more than one
    	Map<String, QueryBuilder> constraints = new HashMap<String, QueryBuilder>();

    	//build per type - must be json data type
    	DataType[] types = getQuery().getDataTypes();
    	if (types != null && types.length > 0) {

    		for (DataType type : types) {

    			//determine the json path - use path from json data type if it's that type
    			String jsonPath = field.getName();
    			if (!jsonPath.startsWith("_") && type instanceof JSONDataType) {
    				jsonPath = StringUtils.join(((JSONDataType)type).getPath(field));
    			}

    			//build the constraint for this json path if we don't have one already
    			if (jsonPath != null && !constraints.containsKey(jsonPath)) {
    				constraints.put(jsonPath, generator.generate(jsonPath));
    			}

    		}

    	} else {

    		//else find first type
    		return generator.generate(getJsonPath(null, field));

    	}

    	QueryBuilder ret = null;
    	if (constraints.size() > 1) {

    		//OR these together in this case - same field constraint, different json paths
    		BoolQueryBuilder bool = QueryBuilders.boolQuery();
    		for (QueryBuilder qb : constraints.values()) {
    			bool.should(qb);
    		}
			ret = bool;
    	}

    	else if (constraints.size() == 1) {
    		ret = constraints.values().iterator().next();
    	}

    	return ret;

    }

    private interface ConstraintGenerator {
    	QueryBuilder generate(String jsonPath);
    }

    void setElastic(ElasticUnityDataSource elastic) { this.elastic = elastic; }
    ElasticUnityDataSource elastic;

}
