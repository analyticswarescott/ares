package com.aw.common.task;

/**
 * Listens for task events
 *
 *
 *
 */
public interface TaskListener {

	/**
	 * called on completion of a task
	 *
	 * @param result the result of a task execution
	 */
	public void onComplete(TaskResult result);

}
