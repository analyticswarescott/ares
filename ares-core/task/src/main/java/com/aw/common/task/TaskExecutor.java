package com.aw.common.task;

import java.util.UUID;

import com.aw.common.task.exceptions.TaskException;

/**
 * Anything that can execute tasks
 *
 *
 *
 */
public interface TaskExecutor {


	/**
	 * Execute a task within a task container
	 *
	 * @param taskDef The task definition whose task must be executed
	 */
	public UUID executeTask(TaskDef taskDef) throws Exception;

	/**
	 * get status for the given task
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaskStatus getStatus(UUID guid) throws TaskException;

}
