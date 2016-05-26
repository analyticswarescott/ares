package com.aw.common.task;

import java.time.Instant;
import java.util.UUID;

import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskInitializationException;

/**
 *
 *
 *
 */
public interface Task {

	/**
	 * @param ctx Task context
	 * @param config Task configuration data
	 */
	public void initialize(TaskContext ctx) throws TaskInitializationException;

	/**
	 * @return The status of the task
	 */
	public TaskStatus getStatus() throws TaskException;

	/**
	 * Execute the task
	 *
	 * @throws Exception
	 */
	public void execute() throws Exception;

	/**
	 * @return the time this task started, will be null if not executing
	 */
	public Instant getStartTime();

	/**
	 * @return The guid of this task
	 */
	public UUID getGuid();

	/**
	 * @param uuid The guid, set before task execution by the framework
	 */
	public void setGuid(UUID uuid);

	/**
	 * Forcibly stop the task
	 */
	public void stop();

	/**
	 * let task know we are shutting down
	 */
	public void shuttingDown();

}
