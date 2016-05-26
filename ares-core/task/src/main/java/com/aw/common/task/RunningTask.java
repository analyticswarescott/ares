package com.aw.common.task;

import java.util.UUID;

import com.aw.common.task.TaskStatus.State;
import com.aw.common.tenant.Tenant;

/**
 * a task currently executing
 *
 *
 *
 */
public class RunningTask {

	public RunningTask() {
	}

	public RunningTask(String taskName, Tenant tenant, TaskContainer container, TaskType taskType) {
		this.taskName = taskName;
		this.tenant = tenant;
		this.container = container.getHost();
		this.taskType = taskType;
		state = State.PENDING;
	}

	/**
	 * @return the container host on which this task is executing
	 */
	public String getContainer() { return this.container;  }
	public void setContainer(String container) { this.container = container; }
	private String container;

	/**
	 * @return the guid for this task
	 */
	public UUID getGuid() { return this.guid;  }
	public void setGuid(UUID guid) { this.guid = guid; }
	private UUID guid;

	public State getState() { return this.state;  }
	public void setState(State state) { this.state = state; }
	private State state;

	/**
	 * @return the tenant id whose task is running
	 */
	public Tenant getTenant() { return this.tenant;  }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	private Tenant tenant;

	/**
	 *
	 * @return The name of the task def that was running
	 */
	public String getTaskName() { return this.taskName; }
	public void setTaskName(String taskName) { this.taskName = taskName; }
	private String taskName;

	/**
	 * @return the task type
	 */
	public TaskType getTaskType() { return this.taskType; }
	public void setTaskType(TaskType taskType) { this.taskType = taskType; }
	private TaskType taskType;

}
