package com.aw.common.task;

import com.aw.common.task.TaskStatus.State;

/**
 * Result of executing a task
 *
 *
 *
 */
public class TaskResult {

	/**
	 * creates a failed task result
	 *
	 * @param thrown any throwable thrown during execution
	 */
	public TaskResult(TaskDef taskDef, Task task, Throwable thrown) {
		this.task = task;
		this.thrown = thrown;
		this.taskDef = taskDef;
		this.state = TaskStatus.State.FAILED;
	}

	/**
	 * creates a successful task result
	 */
	public TaskResult(TaskDef taskDef, Task task) {
		this.task = task;
		this.taskDef = taskDef;
		this.state = TaskStatus.State.SUCCESSFUL;
	}

	/**
	 * @return if failed, will hold the exception or error thrown
	 */
	public Throwable getThrown() { return thrown; }
	private Throwable thrown;

	/**
	 * @return the state of the task upon completion of its execution
	 */
	public State getState() { return state; }
	private State state;

	/**
	 * @return the task that executed
	 */
	public Task getTask() { return this.task; }
	private Task task;

	/**
	 * @return the task definition used to execute the task
	 */
	public TaskDef getTaskDef() { return this.taskDef;  }
	private TaskDef taskDef;

}
