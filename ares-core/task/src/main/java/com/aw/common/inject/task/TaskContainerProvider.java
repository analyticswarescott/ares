package com.aw.common.inject.task;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.task.LocalTaskContainer;
import com.aw.common.task.TaskService;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;

/**
 * Provider for local task container dependency
 *
 *
 *
 */
public class TaskContainerProvider implements Provider<LocalTaskContainer> {

	@Inject @com.google.inject.Inject
	public TaskContainerProvider(Provider<TimeSource> timeSource, Provider<PlatformMgr> platformMgr, Provider<TaskService> service, DocumentMgr docMgr, RestCluster restCluster) {
		container = new LocalTaskContainer(timeSource.get(), platformMgr.get(), service.get(), docMgr, restCluster);
	}

	@Override
	public LocalTaskContainer get() {
		return container;
	}


	private LocalTaskContainer container;

}
