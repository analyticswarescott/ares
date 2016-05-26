package com.aw.common.task;

import static com.aw.common.util.JSONUtils.updateFromString;

import java.time.Instant;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.JSONable;
import com.aw.common.util.TimeSource;
import com.aw.document.DocName;
import com.aw.document.DocTenant;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.body.IBodyInitializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * A task definition describes the execution of a task
 *
 *
 *
 */
public class TaskDef implements TaskConfig, IBodyInitializable, JSONable, Comparable<TaskDef> {

	//name of key storing task-type-specific config data
	static final String CONFIG_KEY = "config";

	static final String TASK_TYPE = "task_type";

	@Override
	public void initialize(Object data, DocumentHandler docs) throws Exception {

		String strData = String.valueOf(data);
		JSONObject json = new JSONObject(strData);

		//update our basic properties
		updateFromString(strData, this);

		//build the type
		type = toTaskType(taskTypeName, docs);

		config = json.optJSONObject(CONFIG_KEY);

		Preconditions.checkNotNull(schedule, "schedule cannot be null in task " + getType().getTypeName(), getName());

	}

	@Override
	public String toJSON() {

		try {

			JSONObject ret = new JSONObject(JSONUtils.objectToString(this, false, true));
			ret.put(CONFIG_KEY, config);
			ret.put(TASK_TYPE, type.getTypeName());

			return ret.toString(4);

		} catch (JSONException e) {
			//TODO: add a throws to toJSON
			throw new RuntimeException("invalid taskdef config format", e);
		}

	}

	/**
	 * @return the task type
	 */
	@JsonIgnore
	public TaskType getType() { return this.type;  }
	@JsonIgnore
	public void setType(TaskType type) { this.type = type; }
	private TaskType type;

	@JsonProperty(value="task_type")
	public String getTaskTypeName() { return this.taskTypeName;  }
	public void setTaskTypeName(String taskTypeString) { this.taskTypeName = taskTypeString; }
	private String taskTypeName;

	@Override
	public <T> T getConfigScalar(String property, Class<T> type) throws ConfigurationException {

		if (config == null) {
			return null;
		}

		try {

			String[] path = property.split("\\.");
			Object obj = JSONUtils.get(config, path);

			//if we have what we need already, just return it
			if (obj.getClass() == type) {
				return (T)obj;
			}

			//otherwise parse the json into a pojo
			else {
				return JSONUtils.objectFromString(obj.toString(), type);
			}

		} catch (Exception e) {
			throw new ConfigurationException("error getting scalar " + property, e);
		}

	}

	@Override
	public <T> List<T> getConfigVector(String property, Class<T> type) throws ConfigurationException {

		if (config == null) {
			return null;
		}

		try {

			Object object = JSONUtils.get(config, property.split("\\."));
			return JSONUtils.listFromString(object.toString(), type);

		} catch (Exception e) {
			throw new ConfigurationException("error getting scalar " + property, e);
		}

	}

	@Override
	public int compareTo(TaskDef o) {

		//use system time to be consistent, only used for ordering
		return getNextExecutionTime(TimeSource.SYSTEM_TIME).compareTo(o.getNextExecutionTime(TimeSource.SYSTEM_TIME));

	}

	public Instant getNextExecutionTime(TimeSource timeSource) {
		return schedule.nextTimeAfter(timeSource.now());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((taskTypeName == null) ? 0 : taskTypeName.hashCode());
		result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
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
		TaskDef other = (TaskDef) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (taskTypeName == null) {
			if (other.taskTypeName != null)
				return false;
		} else if (!taskTypeName.equals(other.taskTypeName))
			return false;
		if (tenant == null) {
			if (other.tenant != null)
				return false;
		} else if (!tenant.equals(other.tenant))
			return false;
		return true;
	}

	/**
	 * get the task type object for the given task type string
	 *
	 * @param taskTypeString The task type string
	 * @param docs the system document handler
	 * @return the task type for the given task type string
	 * @throws Exception
	 */
	private TaskType toTaskType(String taskTypeString, DocumentHandler docs) throws Exception {

		//get the task document for the task type
		Document taskTypeDoc = docs.getDocument(DocumentType.TASK_TYPE, taskTypeString);

		//get the task type from the document
		TaskType taskType = taskTypeDoc.getBodyAsObject(TaskType.class);

		return taskType;

	}

	/**
	 * @return a new, uninitialized instance of a task
	 */
	public Task newTask() throws Exception {

		//create a new task instance
		Task ret = type.getClassType().newInstance();

		//return the result
		return ret;

	}

	/**
	 * @return the task execution schedule
	 */
	public TaskSchedule getSchedule() { return this.schedule;  }
	public void setSchedule(TaskSchedule schedule) { this.schedule = schedule; }
	private TaskSchedule schedule;

	/**
	 * @return the tenant that is associated with this task def
	 */
	@DocTenant
	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	@Override
	public String toString() {
		return getName() + "/" + getTenant().getTenantID() + "/" + getTaskTypeName();
	}

	/**
	 * @return the globally unique name for this task definition
	 */
	@DocName
	public String getName() { return this.name;  }
	public void setName(String name) { this.name = name; }
	private String name;

	public double getMemoryResourceWeighting() { return this.memoryResourceWeighting; }
	public void setMemoryResourceWeighting(double memoryResourceWeighting) { this.memoryResourceWeighting = memoryResourceWeighting; }
	private double memoryResourceWeighting;

	public double getCpuResourceWeighting() { return this.cpuResourceWeighting; }
	public void setCpuResourceWeighting(double cpuResourceWeighting) { this.cpuResourceWeighting = cpuResourceWeighting; }
	private double cpuResourceWeighting;

	/**
	 * Raw task configuration data
	 */
	public void setConfig(JSONObject config) { this.config = config; }
	private JSONObject config;

}
