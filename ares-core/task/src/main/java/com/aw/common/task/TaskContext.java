package com.aw.common.task;

import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformMgr;

/**
 * Context provided to tasks when executing
 *
 *
 *
 */
public interface TaskContext extends TaskConfig {

	/**
	 * @return The task def for this task
	 */
	public TaskDef getTaskDef();

	/**
	 * @return the container in which this task is running
	 */
	public TaskContainer getContainer();

	/**
	 * @return the platform manager
	 */
	public PlatformMgr getPlatformMgr();

	/**
	 * @return the current time
	 */
	public TimeSource getTimeSource();

	/**
	 * @return The tenant executing the task
	 */
	public Tenant getTenant();

	/**
	 * @return The task service
	 */
	public TaskService getTaskService();

	/**
	 * @return the document manager
	 */
	public DocumentMgr getDocMgr();

}
