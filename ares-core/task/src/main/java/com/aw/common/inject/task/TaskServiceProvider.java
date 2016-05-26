package com.aw.common.inject.task;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.task.TaskService;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentHandler;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;

/**
 * Dependency provider for the task service
 *
 *
 *
 */
public class TaskServiceProvider implements Provider<TaskService> {

	@Inject @com.google.inject.Inject
	public TaskServiceProvider(Provider<PlatformMgr> platformMgr, Provider<Platform> platform, Provider<DocumentHandler> docs, 	TimeSource timeSource) {
		this.service = new TaskService(platformMgr.get(), platform, docs, timeSource);
	}

	@Override
	public TaskService get() {
		return service;
	}

	private TaskService service;

}
