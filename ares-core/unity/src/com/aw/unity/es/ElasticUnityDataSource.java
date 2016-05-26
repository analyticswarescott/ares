package com.aw.unity.es;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.exceptions.InitializationException;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.es.ElasticIndex;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.roles.Elasticsearch;
import com.aw.unity.DataType;
import com.aw.unity.UnityDataSourceBase;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityRunner;
import com.aw.unity.exceptions.DataSourceException;
import com.aw.unity.query.Query;
import com.aw.util.ListMap;

/**
 * Represents an elasticsearch data source in unity.
 *
 *  TODO: isolate json etc
 *
 *
 *
 */
public class ElasticUnityDataSource extends UnityDataSourceBase implements TenantAware {

    public static final Logger logger = Logger.getLogger(ElasticUnityDataSource.class);

    //refresh our index list once per this interval max
    static Duration INDEX_REFRESH_INTERVAL = Duration.ofMinutes(5);

    public ElasticUnityDataSource() throws Exception {
    }

    @Override
    public void initialize(UnityInstance instance, Provider<Platform> platformProvider) {

    	super.initialize(instance, platformProvider);

    	Platform platform = platformProvider.get();
        try {

            List<PlatformNode> esNodes = platform.getNodes(NodeRole.ELASTICSEARCH);
            if (esNodes.size() > 0) {

            	//get the cluster name
            	String clusterName = platform.getNode(NodeRole.ELASTICSEARCH).getSetting(Elasticsearch.CLUSTER_NAME);

            	//create the client for the cluster
            	Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
                client = TransportClient.builder().settings(settings).build();

                for (PlatformNode esNode : esNodes) {

                	//add the known elasticsearch nodes
	                client.addTransportAddress(
                    new InetSocketTransportAddress(new InetSocketAddress(
                    			esNode.getHost(),
                    			esNode.getSettingInt(Elasticsearch.ES_TRANSPORT_PORT)
                    		)
                    	)
            		);

                }

            }

            else {
            	logger.warn("No elasticsearch node detected in platform, not connecting to elasticsearch");
            }

        } catch (Exception e) {
        	throw new InitializationException("Error loading elastic data source", e);
        }

    }

     /**
      * Execute the query, returning the active runner
      */
     @Override
     public UnityRunner execute(Query query) {
    	 ElasticUnityRunner ret = new ElasticUnityRunner();

    	 //initialize and execute the runner
    	 ret.initialize(this, query);
    	 ret.execute();

    	 return ret;
     }

     /**
      * get the earliest time we can query for this time sliced elasticsearch index
      *
      * @param index
      * @return
      */
     Instant getEarliest(Tenant tenant, ElasticIndex index) throws DataSourceException {

    	 //make sure we've refreshed our time slices recently
    	 checkTimeSlices(tenant);

    	 List<String> indexes = timeSlices.get(index);

    	 //deal with no indices somehow
    	 if (indexes == null || indexes.size() == 0) {
    		 return Instant.now();
    	 }

    	 String earliest = indexes.get(0);
    	 try {

        	 return index.getEarliest(tenant, earliest);

    	 } catch (ConfigurationException e) {
    		 throw new DataSourceException("error getting earliest time for index " + earliest);
    	 }

     }

     /**
      * get the latest time we can query for this time sliced elasticsearch index
      * @param index
      * @return
      */
     Instant getLatest(Tenant tenant, ElasticIndex index) throws DataSourceException {

    	 //make sure we've refreshed our time slices recently
    	 checkTimeSlices(tenant);

    	 List<String> indexes = timeSlices.get(index);

    	 //deal with no indices somehow
    	 if (indexes == null || indexes.size() == 0) {
    		 return Instant.now();
    	 }

		 String latest = indexes.get(indexes.size() - 1);
    	 try {

        	 return index.getLatest(tenant, latest);

    	 } catch (ConfigurationException e) {
    		 throw new DataSourceException("error getting latest time for index " + latest);
    	 }

     }

     void checkTimeSlices(Tenant tenant) throws DataSourceException {

    	 if (!Instant.now().isAfter(nextCheck)) {
    		 return;
    	 }

    	 try {

        	 String[] indices = getIndicesFromClient();

        	 ListMap<ElasticIndex, String> newTimeSlices = new ListMap<ElasticIndex, String>();

        	 for (String strIndex : indices) {

        		 ElasticIndex index = null;

        		 try {

            		 //determine the index
            		 index = ElasticIndex.valueOf(strIndex.substring(0, strIndex.indexOf('_')).toUpperCase());

        		 } catch (Exception e) {
        			 logger.warn("unrecognized index prefix for index: " + strIndex + " : " + e.getMessage());
        			 throw new DataSourceException("invalid index detected, cannot continue query: " + index, e);
        		 }

        		 //only add indexes this tenant owns
        		 if (index.belongsTo(tenant, strIndex)) {
        			 newTimeSlices.add(index, strIndex);
        		 }

        	 }

        	 //sort, time slices must sort lexicographically
        	 for (List<String> indexList : newTimeSlices.values()) {
        		 Collections.sort(indexList);
        	 }

        	 //update the time slices
        	 timeSlices = newTimeSlices;

        	 //wait until next time to refresh after this is done
        	 nextCheck = Instant.now().plus(INDEX_REFRESH_INTERVAL);

    	 } catch (Exception e) {

    		 //wait for a little while if there is an exception to avoid spamming this
    		 nextCheck = Instant.now().plus(Duration.ofSeconds(10));

    		 throw new DataSourceException("error checking index time slices", e);

    	 }

     }

     String[] getIndicesFromClient() {
    	 return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().concreteAllIndices();
     }

     /**
      * @param dataType The data type whose index is needed
      * @return The elasticsearch index for this data type
      */
     public ElasticIndex getIndex(DataType dataType) {
    	 ElasticIndex index = m_indexMap.get(dataType.getName());

    	 //use the default index if we can't find a specific one
    	 if (index == null) {
    		 index = m_defaultIndex;
    	 }

    	 return index;
     }

     public QueryBuilder searchForDatatypes(DataType... datatypes) {

    	 QueryBuilder ret = null;

		 if (datatypes == null || datatypes.length == 0) {
			 ret = null;
		 }

		 else if (datatypes.length == 1) {
			 ret = QueryBuilders.typeQuery(datatypes[0].getName());
		 }

		 else {
			 BoolQueryBuilder bool = QueryBuilders.boolQuery();
			 for (DataType type : datatypes) {
				 bool = bool.should(QueryBuilders.typeQuery(type.getName()));
			 }
			 ret = bool;
		 }

    	 return ret;

     }

     /**
      * @return The default index for this tenant
      */
     public ElasticIndex getTenantDefaultIndex() { return m_defaultIndex; }

     public ElasticIndex getDefaultIndex() { return m_defaultIndex; }
     public void setDefaultIndex(ElasticIndex defaultIndex) { m_defaultIndex = defaultIndex; }
     private ElasticIndex m_defaultIndex;

     public Client getClient() { return client; }
     void setClient(TransportClient client) { this.client = client; }
     private TransportClient client;

     /**
      * @return A map of data types to non-default indexes
      */
     public Map<String, ElasticIndex> getIndexMap() { return m_indexMap; }
     public void setIndexMap(Map<String, ElasticIndex> indexMap) { m_indexMap = indexMap; }
     private Map<String, ElasticIndex> m_indexMap = new HashMap<String, ElasticIndex>();

     private ListMap<ElasticIndex, String> timeSlices = new ListMap<ElasticIndex, String>();

     Instant nextCheck = Instant.MIN;

}
