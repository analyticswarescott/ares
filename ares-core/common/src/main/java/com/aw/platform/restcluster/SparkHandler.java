package com.aw.platform.restcluster;

import com.aw.common.spark.DriverRegistrationResponse;
import org.codehaus.jettison.json.JSONObject;

/**
 * Any entity that implements this interface will be able to handle control messages from spark.
 *
 * This interface needs to be refactored to remove raw json objects.
 *
 *
 *
 */
public interface SparkHandler {

	/**
	 * Handle a driver registration
	 *
	 * TODO: change JSONObject to a real object
	 *
	 * @param driverInfo
	 * @return
	 * @throws Exception
	 */
	public DriverRegistrationResponse registerDriver(JSONObject driverInfo) throws Exception;

	/**
	 * Handle a processor registeration
	 *
	 * @param driverName
	 * @param processorName
	 */
	public void registerProcessor(String driverName, String processorName) throws Exception;

}
