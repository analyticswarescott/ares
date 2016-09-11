package com.aw.common.task;

import static java.util.stream.Collectors.groupingBy;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Provider;

import com.aw.common.system.scope.ResourceScope;
import com.aw.common.tenant.Tenant;
import org.apache.log4j.Logger;

import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.task.TaskSchedule.Type;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskControllerException;
import com.aw.common.task.exceptions.TaskNotFoundException;
import com.aw.common.util.TimeSource;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentListener;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.restcluster.PlatformController.PlatformState;

/**
 * A task controller decides where running tasks should execute
 *
 *
 *
 */
public class TaskController implements DocumentListener {

	private static final Logger LOGGER = Logger.getLogger(TaskController.class);

	private TimeSource time;

	public TaskController(LocalTaskContainer container, Provider<DocumentHandler> docs, TimeSource time) throws Exception {
		this();

		SecurityUtil.setThreadSystemAccess();

		this.container = container;
		this.docs = docs;
		this.time = time;

		//listen to all tenants including system
		this.docs.get().addListener(this); //listen for task source changes, system docs will fire on any changes for tenants
	}

	public TaskController() {

		//used for coordination
		lock = new ReentrantLock();

		//the empty task queue condition
		notEmpty = lock.newCondition();

		//the tasks changed condition
		tasksChanged = lock.newCondition();

	}

	/**
	 * Execute the task controller
	 */
	public void execute() throws TaskControllerException {

		try {

			SecurityUtil.setThreadSystemAccess();

			//while we are executing
			do {

				//execute tasks
				taskLoop();

			} while (running);

		} catch (InterruptedException e) {
			//swallow interrupted exceptions
		} catch (Exception e) {
			throw new TaskControllerException("error executing controller", e);
		}

	}

	void taskLoop() throws Exception {

		//only perform task execution logic if the platform is running
		if (container.getRestCluster().getState() == PlatformState.RUNNING) {
			runTasks();
		}

		else {
			LOGGER.warn("platform not running (state=" + container.getRestCluster().getState() + ", not running tasks");
		}

		//by default wake up every 10 seconds to refresh
		Instant wakeUpTime = Instant.now().plus(getMaxWaitTime());

		//wait until we need to execute, check tasks, or tasks have changed
		lock.lock();
		try {

			tasksChanged.awaitUntil(Date.from(wakeUpTime));

		} catch (InterruptedException e) {

			//stop leadership if we are interrupted
			setRunning(false);

		} finally {
			lock.unlock();
		}

	}

	void runTasks() throws Exception {

		//get all tasks that should run
		Set<TaskDef> tasks = container.getService().getTasks();

		//get information on running tasks
		Map<TaskDef, TaskStatus> status = container.getService().getTaskStatus();

		//remove tasks that are still running
		removeRunning(tasks, status);

		//handle failures
		handleFailed(status);

		//map by type
		Map<Type, List<TaskDef>> byType = tasks.stream().collect(groupingBy((t) -> t.getSchedule().getType()));

		//execute perpetual tasks not already running now
		execute(byType.get(Type.PERPETUAL));

		//execute scheduled tasks now
		List<TaskDef> scheduled = byType.get(Type.RECURRING);

		if (scheduled != null) {

			//sort scheduled to get next time to wake up
			Collections.sort(scheduled);

			for (TaskDef def : scheduled) {

				//if we need to execute this task now, do it
				if (!def.getNextExecutionTime(time).isAfter(time.now())) {
					execute(def);
				}

			}

		}

	}
	/**
	 * Look for failed tasks
	 *
	 * @param status
	 * @throws Exception
	 */
	void handleFailed(Map<TaskDef, TaskStatus> status) throws Exception {

		//look for failed tasks
		for (RunningTask runningTask : container.getService().getRunningTasks()) {

			//impersonate the tenant to get the task def and execute it
			Impersonation.impersonateTenant(runningTask.getTenant());

			try {

				//retry the task def
				Document taskDoc = docs.get().getDocument(DocumentType.TASK_DEF, runningTask.getTaskName());
				TaskDef taskDef = taskDoc.getBodyAsObject(TaskDef.class);

				if (taskDef == null) {
					throw new TaskNotFoundException("could not retry task " + runningTask.getTaskName() + " for tenant " + runningTask.getTenant().getTenantID() + " : task not found");
				}

				//if the task failed, or is supposed to be running but no container is executing it, rerun it
				boolean rerun = false;
				if (runningTask.getState() == State.FAILED) {
					container.getPlatformMgr().handleLog("found failed task " + runningTask.getTaskName() + " tenant=" + runningTask.getTenant().getTenantID() + ", restarting it", NodeRole.REST);
					rerun = true;
				}

				//if a container completely and spontaneously dies, restart tasks that it was running
				else if (!status.containsKey(taskDef)) {
					container.getPlatformMgr().handleLog("no container running task " + runningTask.getTaskName() + " tenant=" + runningTask.getTenant().getTenantID() + ", restarting it", NodeRole.REST);
					rerun = true;
				}

				//if the task failed or container died, rerun the task
				if (rerun) {
					execute(taskDef);
				}

			} finally {

				//unimpersonate when we're done
				Impersonation.unImpersonate();

			}

		}

	}

	void removeRunning(Set<TaskDef> tasks, Map<TaskDef, TaskStatus> status) {

		//remove tasks that are running (i.e. not failed)
		for (Map.Entry<TaskDef, TaskStatus> entry : status.entrySet()) {

			if (entry.getValue().getState() != State.FAILED) {

				tasks.remove(entry.getKey());

			}

		}


	}

	void execute(List<TaskDef> tasks) throws Exception {

		//nothing to execute if null
		if (tasks == null) {
			return;
		}

		for (TaskDef task : tasks) {

			execute(task);
		}

	}

	void execute(TaskDef taskDef) throws Exception {


		//LOGGER.error("@@@@@@@@@@@@@@@@@@@@@@@ task " + taskDef.getName() + " scope is "  + taskDef.getScope()); ;

		if (taskDef.getScope() == ResourceScope.SYSTEM && !taskDef.getTenant().getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			//LOGGER.error("DEBUG : skipping task " + taskDef.getName() + " for tenant " + taskDef.getTenant().getTenantID());
			return;
		}

		if (taskDef.getScope() == ResourceScope.TENANT && taskDef.getTenant().getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
			//LOGGER.error("DEBUG : skipping task " + taskDef.getName() + " for tenant " + taskDef.getTenant().getTenantID());
			return;
		}

		LOGGER.warn("==executing task " + taskDef.getType() + " for tenant " + taskDef.getTenant().getTenantID());

		//initialize task service to execute the given task def
		container.getService().onStarting(container, taskDef);

		//find a container for this task
		TaskContainer containerToExecute = findContainer(taskDef);

		while (containerToExecute == null) {

			lock.lock();
			try {

				LOGGER.warn("no containers available for task execution, waiting");
				tasksChanged.await(1000L, TimeUnit.MILLISECONDS);
				containerToExecute = findContainer(taskDef);

			} finally {
				lock.unlock();
			}

		}

		//now that we have a container, execute the task
		containerToExecute.executeTask(taskDef);

	}


	TaskContainer findContainer(TaskDef taskDef) throws Exception {

		//TODO: get node status and find least busy node once we have node performance information

		//for now just get a random container
		List<TaskContainer> containers = container.getService().getContainers();

		if (containers.size() == 0) {
			return null;
		}

		Collections.shuffle(containers);
		TaskContainer container = containers.iterator().next();

		return container;

	}

	@Override
	public void onDocumentCreated(Document document) throws Exception {
		onDocChange(document);
	}

	@Override
	public void onDocumentDeleted(Document document) throws Exception {
		onDocChange(document);
	}

	@Override
	public void onDocumentUpdated(Document document) throws Exception {
		onDocChange(document);
	}

	void onDocChange(Document document) {

		//ignore everything except task definitions
		if (document.getDocumentType() != DocumentType.TASK_DEF) {
			return;
		}

		//lock to signal task source change
		lock.lock();

		try {

			//refresh our task sources the next time they are requested
			taskDefs = null;

			//let anyone awaiting this event know
			tasksChanged.signalAll();

		} finally {

			//unlock when we're done
			lock.unlock();

		}

	}

	public boolean isRunning() { return running; }
	void setRunning(boolean running) {

		this.running = running;

		lock.lock();
		try {

			//kick the controller to detect a running state change
			notEmpty.signalAll();

		} finally {
			lock.unlock();
		}

	}
	private boolean running = true;

	Duration getMaxWaitTime() { return maxWaitTime; }
	void setMaxWaitTime(Duration maxWaitTime) { this.maxWaitTime = maxWaitTime; }
	private Duration maxWaitTime = Duration.of(10, ChronoUnit.SECONDS);

	private ReentrantLock lock = null;
	private Condition notEmpty = null;
	private Condition tasksChanged = null;

	/**
	 * the container executing this controller
	 */
	private LocalTaskContainer container = null;

	/**
	 * system documents
	 */
	private Provider<DocumentHandler> docs = null;

	/**
	 * current collection of task sources
	 */
	private Collection<TaskDef> taskDefs = null;

	/**
	 * Next time we need to check status of tasks
	 */
	private Instant nextStatusCheck;

}
