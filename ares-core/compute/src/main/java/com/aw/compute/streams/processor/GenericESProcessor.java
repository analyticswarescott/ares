package com.aw.compute.streams.processor;

import java.util.ArrayList;
import java.util.List;

import com.aw.common.processor.AbstractIterableProcessor;
import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.es.ESKnownIndices;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.IterableTupleProcessor;
import com.aw.platform.Platform;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.unity.es.UnityESClient;

/**
 * Process an Iterable of strings into Elasticsearch
 *
 * The following properties can be set in the configuration for this processor:
 *
 * <li>
 */
public class GenericESProcessor extends AbstractIterableProcessor implements Dependent, IterableTupleProcessor {

	private static final long serialVersionUID = 1L;

	public static final String INDEX_NAME = "index_name";
	public static final String INDEX_TYPE = "index_type";

	private ESKnownIndices index;
    private String index_type;

    @Override
    public void process(String tenant, Iterable<String> messages) throws StreamProcessingException {

       try {
           logger.debug(" about to try to save an Iterable to ES index_name=" + index + " index_type=" + index_type);

    	   //just convert to a single list
    	   List<Data> data = toData(messages);

    	   //if we don't know the type, use unity type name
           if (index_type == null) {

               //testability - use getClient
        	   getClient().bulkInsert(Tenant.forId(tenant), index, data);

           }

           else {

               //testability - use getClient
               getClient().bulkInsert(Tenant.forId(tenant), index, index_type, data);

           }

        }
        catch (Exception ex) {
             throw new StreamProcessingException("tenant=" + tenant + " index_name=" + index + " index_type=" + index_type, ex);
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
            index = ESKnownIndices.valueOf(streamDef.getConfigData().get(INDEX_NAME).toUpperCase());
            index_type = streamDef.getConfigData().get(INDEX_TYPE);
        }
        catch (Exception ex) {
            throw new ProcessorInitializationException(ex.getMessage(), ex);
        }
    }

    public UnityESClient getClient() { return new UnityESClient(getDependency(Platform.class)); }

}
