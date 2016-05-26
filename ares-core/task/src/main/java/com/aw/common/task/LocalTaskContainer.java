package com.aw.common.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.aw.common.cluster.AbstractLocalMember;
import com.aw.common.cluster.Cluster;
import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.LocalMember;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskNotFoundException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.restcluster.RestCluster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Local task container on a node within the service cluster
 *
 *
 *
 */
public class LocalTaskContainer extends AbstractLocalMember implements TaskContainer, LocalMember, TaskListener {

	static final Logger LOGGER = Logger.getLogger(LocalTaskContainer.class);

	@Inject @com.google.inject.Inject
	public LocalTaskContainer(TimeSource timeSource, PlatformMgr platformMgr, TaskService service, DocumentMgr docMgr, RestCluster restCluster) {
		super(service, platformMgr);

		this.time = timeSource;
		this.service = service;
		this.docMgr = docMgr;
		this.restCluster = restCluster;

	}

	LocalTaskContainer() {
		super(null, null);
	}

	@Override
	public UUID executeTask(TaskDef taskDef) throws Exception {

		LOGGER.info("executing task " + taskDef.getTenant().getTenantID() + "/" + taskDef.getType());

		//get a guid for the task
		UUID id = UUID.randomUUID();

		//create the task
		final Task task = taskDef.newTask();

		//initialize the task
		task.initialize(new DefaultTaskContext(this, this.time, taskDef, Tenant.forId(taskDef.getTenant().getTenantID()), service, docMgr));

		//update the running task
		RunningTask runningTask = service.getRunningTask(taskDef);
		runningTask.setGuid(id);
		service.setRunningTask(taskDef, runningTask);

		//register it
		taskById.put(id, task);
		defById.put(id, taskDef);

		//execute task
		executor.submit(new TaskCallable(taskDef, task, this));

		//return the guid for it
		return id;

	}

	@Override
	public synchronized void onComplete(TaskResult taskResult) {

		try {

			//do nothing if we aren't part of the cluster
			taskById.remove(taskResult.getTask().getGuid());
			defById.remove(taskResult.getTask().getGuid());

			if (!isJoined()) {
				return;
			}

			//populate task execution results
			service.onComplete(taskResult);

			platformMgr.handleLog("task completed: " + taskResult.getTask().getGuid() + " (" + taskResult.getTask().getClass().getSimpleName() + ")", NodeRole.REST);

		} catch (Exception e) {

			platformMgr.handleException(e, NodeRole.REST);

		}

	}

	@Override
	public TaskStatus getStatus(UUID guid) throws TaskException {

		Task task = taskById.get(guid);

		if (task == null) {
			throw new TaskNotFoundException("could not find task " + guid);
		}

		TaskStatus ret = task.getStatus();
		ret.setTaskDef(defById.get(guid));

		return ret;

	}

	@Override
	@JsonIgnore
	protected boolean determineReadiness() throws ClusterException {
		return true; //we are always ready
	}

	/**
	 * @return Status for all running tasks
	 */
	@JsonIgnore
	public List<TaskStatus> getTaskStatus() throws TaskException {

		List<TaskStatus> ret = new ArrayList<TaskStatus>();

		for (UUID guid : taskById.keySet()) {
			ret.add(getStatus(guid));
		}

		return ret;

	}

	public Task getTask(UUID guid) {
		return taskById.get(guid);
	}

	/**
	 * Creates a task controller within this container that will handle task execution
	 */
	@Override
	public void executeLeadership(Cluster cluster) {

		try {

			SecurityUtil.setThreadSystemAccess();

			//execute a task controller when we become leader
			controller = ((TaskService)cluster).newController(this);
			controller.execute();

		} catch (Exception e) {

			platformMgr.handleException(e, NodeRole.REST);

			try {

				//throttle exceptions
				Thread.sleep(1000L);

			} catch (InterruptedException ie) {
				//ignore interrupted exceptions
			} finally {
			}

		}

	}

	@Override
	public void cleanup() throws Exception {

		for (Task task : taskById.values()) {
			task.stop();
		}

	}

	@Override
	public void stopLeadership(Cluster cluster) {

		if (controller != null) {
			controller.setRunning(false);
		}

	}

	public void shuttingDown() {

		for (Task task : taskById.values()) {
			task.shuttingDown();
		}

	}

	/**
	 * @return now based on this container's current time source
	 */
	@JsonIgnore
	public Instant now() { return time.now(); }

	public String getHost() { return EnvironmentSettings.getHost();  }

	private TaskController controller;

	TaskService getService() { return service; }
	private TaskService service;

	PlatformMgr getPlatformMgr() { return super.platformMgr; }

	/**
	 * executing tasks by id
	 */
	private Map<UUID, Task> taskById = new HashMap<>();

	/**
	 * executing task defs by id
	 */
	private Map<UUID, TaskDef> defById = new HashMap<>();

	private TimeSource time;

	//task service
	void setExecutor(ListeningExecutorService executor) { this.executor = executor; }
	private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	private DocumentMgr docMgr;

	RestCluster getRestCluster() { return restCluster; }
	private RestCluster restCluster;

}
