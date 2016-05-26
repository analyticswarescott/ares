package com.aw.common.task;

import java.util.HashMap;
import java.util.Map;

/**
 * Status for a task
 *
 *
 *
 */
public class TaskStatus {

	/**
	 * possible task states
	 */
	public enum State {
		PENDING,
 		RUNNING,
		SUCCESSFUL,
		FAILED,
	}

	/**
	 * @return The state of the task
	 */
	public State getState() { return this.state;  }
	public void setState(State state) { this.state = state; }
	private State state;

	/**
	 * @return Status message
	 */
	public String getStatusMessage() { return this.statusMessage;  }
	public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
	private String statusMessage;

	/**
	 * @return Progress, 0 -> 1, ignored for perpetual tasks
	 */
	public double getProgress() { return this.progress;  }
	public void setProgress(double progress) { this.progress = progress; }
	private double progress = 0;

	/**
	 * @return Status properties for this task
	 */
	public Map<String, Object> getProperties() { return this.properties;  }
	public void setProperties(Map<String, Object> properties) { this.properties = properties; }
	private Map<String, Object> properties = new HashMap<>();

	/**
	 * @return the task definition for this task
	 */
	public TaskDef getTaskDef() { return this.taskDef;  }
	public void setTaskDef(TaskDef taskDef) { this.taskDef = taskDef; }
	private TaskDef taskDef;

}
