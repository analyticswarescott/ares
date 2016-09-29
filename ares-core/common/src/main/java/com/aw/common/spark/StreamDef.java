package com.aw.common.spark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.common.system.EnvironmentSettings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.messaging.Topic;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Document body that defines a new Direct Stream to be started by a Spark driver
 */
public class StreamDef {

	public StreamDef() {
	}

	public StreamDef(String configName, String configValue) {
		configData.put(configName, configValue);
	}

	public String toString() {
        ObjectMapper mapper = new ObjectMapper();

        //only write out non-nulls
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("bad json for stream definition " + this.getProcessorId(), e);
        }

    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject(JSONUtils.objectToString(this));
    }

    public String getProcessorName(Tenant tenant) {
    	if (this.tenant) {
            return tenant.getTenantID() + "_" + processorId;
        }

    	else {
        	return  processorId;
        }
    }

	/**
	 * resolves tenant ID
	 * @return
	 */
	@JsonIgnore
	public List<String> getSourceTopicNames(Tenant tenant) throws Exception{

		List<String> ret = new ArrayList<String>();
		for (Topic t: getSourceTopic()) {
			ret.add(Topic.toTopicString(tenant.getTenantID(), t));
		}

		return ret;

	}



    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configData == null) ? 0 : configData.hashCode());
		result = prime * result + ((processorId == null) ? 0 : processorId.hashCode());
		result = prime * result + ((sourceTopic == null) ? 0 : sourceTopic.hashCode());
		result = prime * result + ((streamHandlerClass == null) ? 0 : streamHandlerClass.hashCode());
		result = prime * result + ((targetDriver == null) ? 0 : targetDriver.hashCode());
		result = prime * result + (tenant ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StreamDef other = (StreamDef) obj;
		if (configData == null) {
			if (other.configData != null)
				return false;
		} else if (!configData.equals(other.configData))
			return false;
		if (processorId == null) {
			if (other.processorId != null)
				return false;
		} else if (!processorId.equals(other.processorId))
			return false;
		if (sourceTopic == null) {
			if (other.sourceTopic != null)
				return false;
		} else if (!sourceTopic.equals(other.sourceTopic))
			return false;
		if (streamHandlerClass == null) {
			if (other.streamHandlerClass != null)
				return false;
		} else if (!streamHandlerClass.equals(other.streamHandlerClass))
			return false;
		if (targetDriver == null) {
			if (other.targetDriver != null)
				return false;
		} else if (!targetDriver.equals(other.targetDriver))
			return false;
		if (tenant != other.tenant)
			return false;
		return true;
	}

	public boolean isTenant() { return tenant; }
	public void setTenant(boolean tenant) { this.tenant = tenant; }
	private boolean tenant;

	public boolean isSystem() { return system; }
	public void setSystem(boolean system) { this.system = system; }
	private boolean system;

	/**
	 * optional flag to indicate if a stream is expected to contain multiple tenant keys
	 * @return
	 */
	public boolean isGlobal() { return m_global; }
	public void setisGlobal(boolean global) { m_global = global; }
	private boolean m_global = false;


	public String getTargetDriver() { return targetDriver; }
	public void setTargetDriver(String targetDriver) { this.targetDriver = targetDriver; }
	private String targetDriver;

	/**
	 * TODO: rename to streamId ?
	 *
	 * @return The id for this stream
	 */
	public String getProcessorId() { return processorId; }
	public void setProcessorId(String processorId) { this.processorId = processorId; }
	private String processorId;

	/**
	 * @return A specific processing function for this processor. If not defined, one will be used based on the processor typeS
	 */
	public String getStreamHandlerClass() { return streamHandlerClass; }
	public void setStreamHandlerClass(String streamHandlerClass) { this.streamHandlerClass = streamHandlerClass; }
	private String streamHandlerClass;

	/**
	 * @return Source topics consumed by this stream def
	 */
	public List<Topic> getSourceTopic() { return sourceTopic; }
	public void setSourceTopic(List<Topic> sourceTopic) { this.sourceTopic = sourceTopic; }
	private List<Topic> sourceTopic;

	public Topic getDestTopic() { return this.destTopic;  }
	public void setDestTopic(Topic destTopic) { this.destTopic = destTopic; }
	private Topic destTopic;

	public Map<String, Object> getConfigData() {  //TODO: create Testable interface for defs to implement to allow test divergence

		return configData;
	}
	public void setConfigData(Map<String, Object> processorData) { configData = processorData; }
	private Map<String, Object> configData = new HashMap<String, Object>();


	public Map<String, String> getConfigDataTest() { return configDataTest; }
	public void setConfigDataTest(Map<String, String> configDataTest) { this.configDataTest = configDataTest; }
	private Map<String, String> configDataTest;


	/**
	 * optional property to set a number of rows to target for each task (e.g. optimal row count for ES bulk insert)
	 * @return
	 */

	public int getOptimalEventsPerTask() {return optimalEventsPerTask;}
	public void setOptimalEventsPerTask(int optimalRowsPerTask) {this.optimalEventsPerTask = optimalRowsPerTask;}
	private int optimalEventsPerTask = 0;


}
