package com.aw.common.task;

import java.time.temporal.ChronoUnit;

import com.aw.common.messaging.Topic;
import com.aw.common.processor.FileArchiveProcessor;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskInitializationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.platform.PlatformMgr;
import com.aw.utils.kafka.TopicIterator;

public class TenantArchiveTask extends AbstractTask {

	private TopicIterator topicIterator;
	private PlatformMgr platformMgr;
	private Tenant tenant;
	private TimeSource time;
	private FileArchiveProcessor processor;

	@Override
	public void initialize(TaskContext ctx) throws TaskInitializationException {

		try {

			topicIterator = new TopicIterator(ctx.getPlatformMgr().getPlatform(), ctx.getTaskService().getTenantZkAccessor(), Topic.READY_FOR_ARCHIVE, ctx.getTenant());
			platformMgr = ctx.getPlatformMgr();
			tenant = ctx.getTenant();
			time = ctx.getTimeSource();
			processor = new FileArchiveProcessor(platformMgr, time);

		} catch (Exception e) {
			throw new TaskInitializationException("error initializing topic iterator for tenant archive task, tenant=" + ctx.getTenant().getTenantID(), e);
		}

	}

	@Override
	public void shuttingDown() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void execute() throws Exception {

		Impersonation.impersonateTenant(tenant);

		try {

			//read the latest files from the topic to archive
			processor.process(tenant.getTenantID(), topicIterator);

		} finally {
			Impersonation.unImpersonate();
		}

	}

	public ChronoUnit getArchiveTimeUnit() { return processor.getArchiveTimeUnit(); }

	@Override
	public TaskStatus getStatus() throws TaskException {

		//just a simple status for now
		TaskStatus taskStatus = new TaskStatus();
		taskStatus.setProgress(0);
		taskStatus.setState(State.RUNNING);
		return taskStatus;

	}

}
