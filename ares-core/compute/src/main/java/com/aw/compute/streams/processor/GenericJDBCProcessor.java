package com.aw.compute.streams.processor;

import com.aw.common.processor.AbstractIterableProcessor;
import com.aw.common.rdbms.DBMgr;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.AbstractIterableDataProcessor;
import com.aw.compute.streams.processor.framework.IterableTupleProcessor;
import com.aw.platform.Platform;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.unity.jdbc.UnityJDBCClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Process an Iterable of strings into Elasticsearch
 *
 * The following properties can be set in the configuration for this processor:
 *
 * <li>
 */
public class GenericJDBCProcessor extends AbstractIterableDataProcessor implements Dependent, IterableTupleProcessor {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "table_name";

	public static final String TARGET_DB = "target_db";



    private String table_name;



	private DBMgr dbMgr;

    @Override
    public void process(String tenant, Iterable<String> messages) throws StreamProcessingException {

       try {
           logger.debug(" saving iterable to JDBC: " + table_name);

    	   //just convert to a single list
    	   List<Data> data = toData(messages);



		  // DBMgr dbMgr = getProviderDependency(DBMgr.class).get();

		   UnityJDBCClient jdbcClient = new UnityJDBCClient(getTargetDBConfig());
		   jdbcClient.bulkInsert(Tenant.forId(tenant), "", data.get(0).getType(), data);



        }
        catch (Exception ex) {
             throw new StreamProcessingException("JDBC update failed tenant=" + tenant + " table name " + table_name, ex);
        }

    }



    @Override
    public void init(StreamDef streamDef) throws ProcessorInitializationException{
		super.init(streamDef);
        try {
            table_name = configData.get(TABLE_NAME).toString().toUpperCase();
			this.configData = streamDef.getConfigData();
        }
        catch (Exception ex) {
            throw new ProcessorInitializationException(ex.getMessage(), ex);
        }
    }

	@Override
	protected Map<String, String> getRefDBConfig() {
		Object refDB = configData.get(REF_DB);

		if (refDB == null) {
			return (Map<String, String>) configData.get(TARGET_DB);
		}

		return (Map<String, String>) configData.get(REF_DB);

	}


	protected Map<String, String> getTargetDBConfig() {

			return (Map<String, String>) configData.get(TARGET_DB);

	}
}
