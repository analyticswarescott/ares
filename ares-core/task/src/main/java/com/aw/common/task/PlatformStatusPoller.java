package com.aw.common.task;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskInitializationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.TimeSource;
import com.aw.platform.PlatformMgr;
import com.aw.platform.monitoring.NodeStatus;
import com.aw.platform.monitoring.PlatformStatus;
import com.aw.platform.monitoring.StreamStatus;
import com.aw.platform.monitoring.TenantStatus;
import com.aw.platform.monitoring.TopicPartitionStatus;
import com.aw.platform.restcluster.PlatformController;
import com.fasterxml.jackson.annotation.JsonIgnore;

import kafka.common.FailedToSendMessageException;

/**
 * A task that collects status - it is also its own task source
 *
 *
 *
 */
public class PlatformStatusPoller extends AbstractTask {

	public static final String TYPE = "platform_status";

	private static final Logger LOGGER = Logger.getLogger(PlatformStatusPoller.class);

	//task data, last time this task polled
	private static final String LAST_POLL = "last_poll";

	@Override
	public void initialize(TaskContext context) throws TaskInitializationException {
		this.platformMgr = context.getPlatformMgr();
		this.timeSource = context.getTimeSource();
		this.service = context.getTaskService();
		this.lock = new ReentrantLock();
		this.interval = this.lock.newCondition();
		this.taskDef = context.getTaskDef();
	}

	@JsonIgnore
	@Override
	public TaskStatus getStatus() throws TaskException {

		try {

			TaskStatus ret = new TaskStatus();
			ret.setState(State.RUNNING);

			Instant lastStatus = service.get(taskDef, LAST_POLL, Instant.class);
			if (lastStatus == null) {
				lastStatus = Instant.MIN;
			}

			if (pollCount > 0) {
				ret.setStatusMessage("last status poll: " + lastStatus);
			}

			else {
				ret.setStatusMessage("no polls yet");
			}

			//set properties based on our properties
			JSONUtils.updateFromString(JSONUtils.objectToString(this), ret.getProperties());

			return ret;

		} catch (Exception e) {

			throw new TaskException("error getting status for task", e);

		}


	}

	@Override
	public void execute() throws Exception {

		//set up system access for this thread
		SecurityUtil.setThreadSystemAccess();

		//initialize lastStatus
		lastStatus = service.get(taskDef, LAST_POLL, Instant.class);
		if (lastStatus == null) {
			lastStatus = Instant.now();
		}

		//continually poll for status
		do {

			try {

				lock.lock();

				//wait until next poll TODO: make scheduling something supported at task framework level
				try {

					try {
						poll();
					}
					catch (FailedToSendMessageException fex) {
						//this is very likely to only happen at startup so reduce noise by omitting stack trace
						// -- TODO: base on platform state
						LOGGER.error("Kafka failed send exception: " + fex.getClass().getTypeName() + " : " + fex.getMessage());
					}
					catch (Exception e) {
						LOGGER.error("error polling for status", e);
					}

					interval.awaitUntil(Date.from(timeSource.now().plus(pollFrequency)));

					//break if not running
					if (!running) {
						break;
					}

				} finally {
					lock.unlock();
				}

			} catch (Exception e) {
				errorCount++;
				throw e;
			}

		} while (true);

	}

	void poll() throws Exception {

		PlatformController.PlatformState state = platformMgr.newClient().getPlatformState();
		if (state != PlatformController.PlatformState.RUNNING) {
			LOGGER.warn(" Status poll cancelled due to platform state: " + state + " deferring status polling untill RUNNING state is detected ");
			return;
		}

		//get the platform status from the platform api TODO: this probably needs to change to match what scott is doing
		PlatformStatus status = platformMgr.newClient().getPlatformStatus(lastStatus);


		//add messages to perf_stats
		for (List<NodeStatus> nsList : status.getNodeStatuses().values()) {

			for (NodeStatus ns : nsList ) {
				String stats = JSONUtils.objectToString(ns.getPerfStats()); //TODO: override toString
				platformMgr.sendMessage(Topic.PERF_STAT, Tenant.SYSTEM, stats);
			}
		}

		//add stream status to stream status topic
		for (TenantStatus tenantStatus : status.getTenantStatus()) {
			//nothing to do at this level
			for (StreamStatus streamStatus : tenantStatus.getStreamStatus()) {
				//nothing to do at this level
				for (List<TopicPartitionStatus> topicStatusList : streamStatus.getTopicStatus().values()) {
					//nothing to do at this level
					for (TopicPartitionStatus topicStatus : topicStatusList) {

						//for each partition, for each topic, for each stream, for each tenant, write a topic_status object
						platformMgr.sendMessage(Topic.TOPIC_STATUS, tenantStatus.getTenant(), topicStatus);

					}
				}
			}
		}

		//increment the poll count
		pollCount++;

		//mark the poll time
		lastStatus = timeSource.now();

		//update cluster
		service.put(taskDef, LAST_POLL, lastStatus);

	}

	@Override
	public void stop() {
		lock.lock();
		try {
			setRunning(false);
		} finally {
			lock.unlock();
		}
	}

	public void shuttingDown() {
		//avoid task execution of poll during shutdown
		lock.lock();
		lock.unlock();
	}

	/**
	 * The last time we successfully polled for status
	 */
	public Instant getLastStatus() { return lastStatus; }
	private Instant lastStatus;

	/**
	 * The number of times we've polled for status
	 */
	public long getPollCount() { return pollCount; }
	private long pollCount;

	/**
	 * The number of times we've unsuccessfully polled for status
	 */
	public long getErrorCount() { return errorCount; }
	private long errorCount;

	/**
	 * @param running whether this poller is running
	 */
	void setRunning(boolean running) { this.running = running; }
	private boolean running = true;

	/**
	 * @return frequency of status poll
	 */
	public Duration getPollFrequency() { return this.pollFrequency; }
	void setPollFrequency(Duration pollFrequency) { this.pollFrequency = pollFrequency; }
	private Duration pollFrequency = Duration.of(30, ChronoUnit.SECONDS);

	private PlatformMgr platformMgr;

	private TimeSource timeSource;

	private TaskService service;

	private ReentrantLock lock;

	private Condition interval;

	private TaskDef taskDef;

}

