package com.aw.compute.streams.processor;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.StringTupleProcessor;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;

public class TenantProcessor implements StringTupleProcessor, Dependent {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(TenantProcessor.class);

    public enum TENANT_SIZING {
        S, M, L
    }

    public enum TENANT_OPERATION {
        CREATE
    }

    @Override
    public void process(String string) throws StreamProcessingException  {
        try {
            //logger.debug(" tenant from string");


			logger.error("processing tenant " + string);
            processTenant(new JSONObject(string));
        } catch (Exception e) {
            //logger.error("error in base TenantProcessor.process()" , e );
            throw  new RuntimeException("error tenant processor " , e);
        }
    }

    @Override
    public void init(StreamDef streamDef) throws ProcessorInitializationException{

    }


    private void processTenant(JSONObject tenantDescriptor) throws Exception{
        logger.warn("Processing tenant:  metadata: " + tenantDescriptor.toString());

        // 0. validate tenant descriptor, apply defaults
        //validate(tenantDescriptor);
        applyDefaults(tenantDescriptor);


/*        Object operation = tenantDescriptor.get("operation");

        if (!TENANT_OPERATION.CREATE.toString().equals(operation)) {
            // for now, only CREATE is supported
            throw new UnsupportedOperationException("Unsupported tenant operation: " + operation);
        }*/
		logger.warn("processing create tenant ");
        processCreateTenant(tenantDescriptor);
    }

    private void processCreateTenant(JSONObject tenantDescriptor) throws Exception{
        // 1. copy defaults to tenant-specific doc db entries
        prepareTenantSpecificDocs(tenantDescriptor);

    }

    private void prepareTenantSpecificDocs(JSONObject tenantDescriptor) throws Exception{
        String tenantID = (String) tenantDescriptor.get("tid");
        logger.warn("Preparing tenant specific docs for tenant ID " + tenantID);

        try {
			final String payload = tenantDescriptor.toString();

			logger.warn("about to provision tenant " + tenantID);
			new PlatformClient(getDependency(Platform.class)).provision(JSONUtils.objectFromString(tenantDescriptor.toString(), Tenant.class));
			logger.warn("tenant provisioned:  " + tenantID);
        }
        catch (Exception ex) {

			//need to see the stack trace here
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.close();
            logger.error("getting error message for tenant provision " + sw.toString());
        }
    }

    private void applyDefaults(JSONObject tenantDescriptor) throws Exception{

        if (!tenantDescriptor.has("operation")) {
            tenantDescriptor.put("operation", TENANT_OPERATION.CREATE.name());
            logger.debug("Applied default operation " + tenantDescriptor.get("operation"));
        }

/*
        if (tenantDescriptor.has("rough_sizing")) {
            Object roughSizing = tenantDescriptor.get("rough_sizing");
            tenantDescriptor.put("rough_sizing", TENANT_SIZING.M.name());
            logger.debug("Applied default sizing " + tenantDescriptor.get("rough_sizing"));
        }*/
    }

    private void validate(JSONObject tenantDescriptor) throws Exception{
        if (tenantDescriptor == null) {
            throw new RuntimeException("Tenant descriptor cannot be empty (null).");
        }

        Object tenantID = tenantDescriptor.get("tid");
        if (tenantID == null) {
            throw new RuntimeException("Tenant descriptor must contain a tid attribute.");
        }


        if (tenantDescriptor.has("operation")) {
            try {
                TENANT_OPERATION.valueOf(tenantDescriptor.getString("operation"));
            } catch (IllegalArgumentException e) {
                // only CREATE operations supported for now
                throw new RuntimeException("Tenant descriptor had unrecognized operation: " + tenantDescriptor.getString("operation"));
            }
        }


/*        if ( tenantDescriptor.has("rough_sizing")  ) {
            Object roughSizing = tenantDescriptor.get("rough_sizing");
            if (roughSizing != null) {
                try {
                    TENANT_SIZING.valueOf((String) roughSizing);
                } catch (IllegalArgumentException e) {
                    // only CREATE operations supported for now
                    throw new RuntimeException("Tenant descriptor had unrecognized sizing: " + roughSizing);
                }
            }
        }*/

    }

}
