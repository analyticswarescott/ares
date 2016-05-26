package com.aw.common.task;

/**
 * wraps task execution for common error handling and task results
 *
 *
 *
 */
public class TaskCallable implements Runnable {

	private Task task;
	private TaskListener listener;
	private TaskDef taskDef;

	public TaskCallable(TaskDef taskDef, Task task, TaskListener listener) {
		this.task = task;
		this.listener = listener;
		this.taskDef = taskDef;
	}

	@Override
	public void run() {

		//will hold the task result
		TaskResult ret = null;

		try {

			//execute the task
			task.execute();

			//build the result
			ret = new TaskResult(taskDef, task);

		} catch (Throwable t) {

			//on any exception or error, update task with resulting throwable
			ret = new TaskResult(taskDef, task, t);

		} finally {

			//return the result to the listener
			listener.onComplete(ret);

		}

	}

}
