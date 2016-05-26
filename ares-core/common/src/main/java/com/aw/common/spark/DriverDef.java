package com.aw.common.spark;

import com.aw.common.util.JSONUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Map;

/**
 * Document body that defines a new Direct Stream to be started by a Spark driver
 */
public class DriverDef {


    public String toString() {
        ObjectMapper mapper = new ObjectMapper();

        //only write out non-nulls
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("bad json for driver definition ", e);
        }

    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject(JSONUtils.objectToString(this));
    }

	public boolean isSupervised() {
		return m_supervised;
	}

	public void setSupervised(boolean supervised) {
		this.m_supervised = supervised;
	}

	public boolean isEnabled() {
		return m_enabled;
	}

	public void setEnabled(boolean enabled) {
		this.m_enabled = enabled;
	}

	public String getDriverClass() {
		return m_driver_class;
	}

	public void setDriverClass(String driver_class) {
		this.m_driver_class = driver_class;
	}

	public int getSparkExecutorCores() {
		return m_spark_executor_cores;
	}

	public void setSparkExecutorCores(int spark_executor_cores) {
		this.m_spark_executor_cores = spark_executor_cores;
	}

	public String getSparkExecutorMemory() {
		return m_spark_executor_memory;
	}

	public void setSparkExecutorMemory(String spark_executor_memory) {
		this.m_spark_executor_memory = spark_executor_memory;
	}

	public int getBatchIntervalSeconds() {
		return m_batch_inteval_seconds;
	}

	public void setBatchIntevalSeconds(int batch_inteval_seconds) {
		this.m_batch_inteval_seconds = batch_inteval_seconds;
	}

	public int getHeartbeatInterval() {
		return heartbeat_interval;
	}

	public void setHeartbeatInterval(int heartbeat_interval) {
		this.heartbeat_interval = heartbeat_interval;
	}

	public int getWorkPollInterval() {
		return m_work_poll_interval;
	}

	public void setWorkPollInterval(int work_poll_interval) {
		this.m_work_poll_interval = work_poll_interval;
	}

	public int getWorkPollDelay() {
		return m_work_poll_delay;
	}

	public void setWorkPollDelay(int m_work_poll_delay) {
		this.m_work_poll_delay = m_work_poll_delay;
	}

	public Map<String, String> getSparkConfigOptions() {
		return m_spark_config_options;
	}

	public void setSparkConfigOptions(Map<String, String> spark_config_options) {
		this.m_spark_config_options = spark_config_options;
	}

	private boolean m_supervised = true;
	private boolean m_enabled = true;
	private String m_driver_class;
	private int m_spark_executor_cores = 1;
	private String m_spark_executor_memory = "1g";
	private int m_batch_inteval_seconds = 30;
	private int heartbeat_interval = 3;
	private int m_work_poll_interval = 30;
	private int m_work_poll_delay = 5;
	private Map<String, String> m_spark_config_options;

	public String getExtraJavaOptions() { return m_extraJavaOptions; }
	public void setExtraJavaOptions(String extraJavaOptions) { m_extraJavaOptions = extraJavaOptions; }
	private String m_extraJavaOptions;


}
