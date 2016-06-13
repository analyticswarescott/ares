package com.aw.compute.streams.processor;

import com.aw.common.processor.AbstractIterableProcessor;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.IterableTupleProcessor;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.unity.jdbc.UnityJDBCClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Process an Iterable of strings into Elasticsearch
 *
 * The following properties can be set in the configuration for this processor:
 *
 * <li>
 */
public class GenericJDBCProcessor extends AbstractIterableProcessor implements Dependent, IterableTupleProcessor {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "table_name";


    private String table_name;

    @Override
    public void process(String tenant, Iterable<String> messages) throws StreamProcessingException {

       try {
           logger.debug(" saving iterable to JDBC: " + table_name);

    	   //just convert to a single list
    	   List<Data> data = toData(messages);

		   DBMgr dbMgr = getProviderDependency(DBMgr.class).get();

		   UnityJDBCClient jdbcClient = new UnityJDBCClient(dbMgr);
		   jdbcClient.bulkInsert(Tenant.forId(tenant), "", data.get(0).getType(), data);



        }
        catch (Exception ex) {
             throw new StreamProcessingException("JDBC update failed tenant=" + tenant + " table name " + table_name, ex);
        }

    }

    /**
     * Convert incoming json to platform data using unity
     *
     * @param messages
     * @return
     * @throws Exception
     */
    private List<Data> toData(Iterable<String> messages) throws Exception {

    	List<Data> ret = new ArrayList<Data>();

    	//get the unity data for this json so we can get the ID
    	for (String json : messages) {

    		Data cur = getDependency(UnityInstance.class).newData(json);
    		ret.add(cur);

    	}

    	return ret;

    }

    @Override
    public void init(StreamDef streamDef) throws ProcessorInitializationException{
        try {
            table_name = streamDef.getConfigData().get(TABLE_NAME).toUpperCase();

        }
        catch (Exception ex) {
            throw new ProcessorInitializationException(ex.getMessage(), ex);
        }
    }



}
