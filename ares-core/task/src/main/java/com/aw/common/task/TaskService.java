package com.aw.common.task;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.aw.common.zookeeper.structure.ZkPurpose;
import org.apache.log4j.Logger;

import com.aw.common.cluster.ClusterException;
import com.aw.common.cluster.Member;
import com.aw.common.cluster.zk.ZkCluster;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;

/**
 * A service using a single leader and multiple followers
 *
 *
 *
 */
public class TaskService extends ZkCluster {

	private static final Logger LOGGER = Logger.getLogger(TaskService.class);

	public static final String NAME = "tasks";
	public static final String RUNNING_TASKS = "running_tasks";
	public static final String DATA = "data";

	private PlatformMgr platformMgr;

	public TaskService(PlatformMgr platformMgr, Provider<Platform> platform, Provider<DocumentHandler> docs, TimeSource timeSource) {
		super(platform, NAME);
		this.timeSource = timeSource;
		this.docs = docs;
		this.platformMgr = platformMgr;

	}


	public void updateTaskDefDocument(TaskDef def) throws  Exception {

		Document doc = docs.get().getDocument(DocumentType.TASK_DEF, def.getName());
		doc.setBodyFromObject(def);
		docs.get().updateDocument(doc);
		logger.warn(" updated document " + doc.getKey());

	}

	/**
	 * @return all task definitions that need to be executed at some point
	 */
	public Set<TaskDef> getTasks() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		Set<TaskDef> ret = new HashSet<>();
		ret.addAll(docs.get().getBodiesAsObjects(DocumentType.TASK_DEF, TaskDef.class));

		//get system tasks

		Collection<Document> tenants = docs.get().getAllTenants();
		for (Document tenantDoc : tenants) {

			//add tasks for each tenant
			Tenant tenant = new Tenant(tenantDoc.getBody());
			Impersonation.impersonateTenant(tenant);
			try {
				ret.addAll(docs.get().getBodiesAsObjects(DocumentType.TASK_DEF, TaskDef.class));
			} finally {
				Impersonation.unImpersonate();
			}

		}

		return ret;

	}

	/**
	 * @return gets running tasks from the service cluster
	 */
	public List<RunningTask> getRunningTasks() throws Exception {

		ArrayList<RunningTask> ret = new ArrayList<>();

		Collection<Document> tenants = docs.get().getAllTenants();
		for (Document tenantDoc : tenants) {

			//add tasks for each tenant
			Tenant tenant = new Tenant(tenantDoc.getBody());
			Impersonation.impersonateTenant(tenant);
			try {
				ret.addAll(getTenantZkAccessor().getAll(ZkPurpose.RUNNING_TASK, "", RunningTask.class));
			} finally {
				Impersonation.unImpersonate();
			}

		}

		return ret;
	}

	/**
	 * Get the running task for the given task def
	 *
	 * @param def the task def for the desired running task
	 * @return the running task for the given task def
	 * @throws ClusterException if anything goes wrong, including no running task for the given task def
	 */
	public RunningTask getRunningTask(TaskDef def) throws Exception {
		Impersonation.impersonateTenant(def.getTenant());
		try {
			return getTenantZkAccessor().get(ZkPurpose.RUNNING_TASK, getKey(def), RunningTask.class);
		}
		finally {
			Impersonation.unImpersonate();
		}



	}

	/**
	 * put task specific data
	 *
	 * @param taskDef
	 * @param key
	 * @param data
	 * @throws ClusterException
	 */
	public void put(TaskDef taskDef, String key, Object data) throws Exception {
		Impersonation.impersonateTenant(taskDef.getTenant());
		try {
			getTenantZkAccessor().put(ZkPurpose.TASK_DATA, getKey(taskDef) + "_" + key, data);
		}
		finally {
			Impersonation.unImpersonate();
		}

	}

	/**
	 * get task specific data
	 *
	 * @param taskDef
	 * @param key
	 * @param type
	 * @return
	 * @throws ClusterException
	 */
	public <T> T get(TaskDef taskDef, String key, Class<T> type) throws Exception {
		Impersonation.impersonateTenant(taskDef.getTenant());
		try {
			return getTenantZkAccessor().get(ZkPurpose.TASK_DATA, getKey(taskDef) + "_" + key, type);
		}
		finally {
			Impersonation.unImpersonate();
		}
	}


	/**
	 * update running task in the service cluster
	 *
	 * @param def the task def whose running task object needs to be updated
	 * @param runningTask the updated running task data
	 * @throws ClusterException if anything goes wrong
	 */
	public void setRunningTask(TaskDef def, RunningTask runningTask) throws Exception {
		Impersonation.impersonateTenant(def.getTenant());
		try {
			getTenantZkAccessor().put(ZkPurpose.RUNNING_TASK, getKey(def), runningTask);
		}
		finally {
			Impersonation.unImpersonate();
		}
	}

	public Map<TaskDef, TaskStatus> getTaskStatus() throws Exception {

		Map<TaskDef, TaskStatus> ret = new HashMap<TaskDef, TaskStatus>();

		//map of task containers used to get status
		Map<String, List<TaskContainer>> clients = getContainers().stream().collect(groupingBy(TaskContainer::getHost));

		//get all running task info
		for (RunningTask runningTask : getRunningTasks()) {

			try {

				//if no guid yet, continue
				if (runningTask.getGuid() == null) {
					logger.warn("task guid null for " + runningTask.getTenant().getTenantID()  + "/" + runningTask.getTaskType() + ", skipping");
					continue;
				}

				else if (runningTask.getContainer() == null) {
					logger.warn("container null for " + runningTask.getTenant().getTenantID() + "/" + runningTask.getTaskType() +", skipping");
					continue;
				}

				List<TaskContainer> containers = clients.get(runningTask.getContainer());

				if (containers == null || containers.size() == 0) {
					logger.error("could not find container for running task, container=" + runningTask.getContainer());
				}

				else {

					//get the status, update the map
					logger.debug("asking " + runningTask.getContainer() + " for status of task " + runningTask.getGuid());
					TaskStatus taskStatus = containers.get(0).getStatus(runningTask.getGuid());
					ret.put(taskStatus.getTaskDef(), taskStatus);

				}

			} catch (Exception e) {
				//in this case no task status will be added to the map
				logger.warn("error getting task status", e);
			}

		}

		return ret;

	}

	/**
	 * called when a task is going to start
	 *
	 * @param taskDef
	 */
	void onStarting(TaskContainer container, TaskDef taskDef) throws Exception {

		//populate zk with information
		RunningTask runningTask = new RunningTask(taskDef.getName(), taskDef.getTenant(), container, taskDef.getType());
		setRunningTask(taskDef, runningTask);

	}

	/**
	 * called when a task has completed
	 *
	 * @param mockTaskDef the task that completed
	 */
	void onComplete(TaskResult taskResult) throws Exception {

		SecurityUtil.setThreadSystemAccess();

		//get the task def from the result
		TaskDef def = taskResult.getTaskDef();

		//get current running task status
		RunningTask task = getTenantZkAccessor().get(ZkPurpose.RUNNING_TASK, getKey(def), RunningTask.class);

		//if there was an error
		if (taskResult.getState() == State.FAILED) {

			LOGGER.warn("error executing task type=" + def.getType() + ", tenant=" + def.getTenant().getTenantID() + " name=" + def.getName(), taskResult.getThrown());

			//log to platform
			platformMgr.handleException(taskResult.getThrown(), NodeRole.REST);

			//set state to error, will tell service to rerun
			task.setState(taskResult.getState());

			//update the cluster state
			getTenantZkAccessor().put(ZkPurpose.RUNNING_TASK, getKey(def), task);

			//TODO: log task error completion to database

		}

		//if the task fully completed
		else if (taskResult.getState() == State.SUCCESSFUL) {

			//remove the entry from tracking, we've completed successfully
			getTenantZkAccessor().delete(ZkPurpose.RUNNING_TASK, getKey(def));
			getTenantZkAccessor().delete(ZkPurpose.TASK_DATA, getKey(def));

			//TODO: log task successful completion to database

		}


	}

	/**
	 * get the cluster key for the given task def
	 *
	 * @param mockTaskDef
	 * @return
	 */
	String getKey(TaskDef def) {
		return  def.getType().getTypeName() + "/" + def.getName();
	}


	/**
	 * @return A list of task containers
	 * @throws Exception
	 */
	public List<TaskContainer> getContainers() throws Exception {
		List<TaskContainer> containers = new ArrayList<TaskContainer>();
		for (PlatformNode node : getParticipants()) {
			containers.add(new RemoteTaskContainer(node, platformMgr));
		}
		return containers;
	}

	/**
	 * @return Provides a controller
	 */
	public TaskController newController(LocalTaskContainer container) throws Exception {
		return new TaskController(container, docs, timeSource);
	}

	@Override
	protected Class<? extends Member> getMemberType() {
		return TaskContainer.class;
	}

	private TimeSource timeSource;

	private Provider<DocumentHandler> docs;

}
