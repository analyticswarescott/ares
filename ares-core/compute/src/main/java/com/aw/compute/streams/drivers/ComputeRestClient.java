package com.aw.compute.streams.drivers;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.DriverRegistrationResponse;
import com.aw.compute.streams.exceptions.DriverInitializationException;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;

import javax.inject.Provider;

/**
 * REST calls for driver and processor operations
 */
public class ComputeRestClient extends PlatformClient {
 public static final Logger logger = LoggerFactory.getLogger(ComputeRestClient.class);

 	public ComputeRestClient(Provider<Platform> platform) {
 		super(platform);
	}

    public DriverRegistrationResponse register(String driverRootName, String driverName, String driverID) throws Exception {

        SecurityUtil.setThreadSystemAccess();
        try {

        	//TODO: make this a pojo!
            JSONObject driverInfo = new JSONObject();

            driverInfo.put("driver_id", driverID);
            driverInfo.put("driver_root_name", driverRootName);
            driverInfo.put("driver_name", driverName);

            return super.registerDriver(driverInfo);

        }
        catch (Exception ex) {

            throw new DriverInitializationException(ex.getMessage());
        }

    }
}
