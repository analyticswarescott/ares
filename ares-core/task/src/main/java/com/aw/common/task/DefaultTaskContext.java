package com.aw.common.task;

import java.util.List;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.document.DocumentMgr;
import com.aw.platform.PlatformMgr;

/**
 * Provides executing tasks contextual information and configuration data
 *
 *
 *
 */
public class DefaultTaskContext implements TaskContext {

	public DefaultTaskContext(LocalTaskContainer container, TimeSource time, TaskDef taskDef, Tenant tenant, TaskService taskService, DocumentMgr docMgr) {
		this.container = container;
		this.time = time;
		this.platformMgr = container.getPlatformMgr();
		this.taskDef = taskDef;
		this.tenant = tenant;
		this.taskService = taskService;
		this.docMgr = docMgr;
	}

	@Override
	public <T> T getConfigScalar(String property, Class<T> type) throws ConfigurationException {
		return taskDef.getConfigScalar(property, type);
	}

	 @Override
	public <T> List<T> getConfigVector(String property, Class<T> type) throws ConfigurationException {
		 return taskDef.getConfigVector(property, type);
	}

	public LocalTaskContainer getContainer() { return this.container; }
	private LocalTaskContainer container;

	public TaskDef getTaskDef() { return this.taskDef; }
	private TaskDef taskDef;

	public TimeSource getTimeSource() { return this.time; }
	private TimeSource time;

	public PlatformMgr getPlatformMgr() { return this.platformMgr; }
	private PlatformMgr platformMgr;

	public Tenant getTenant() { return this.tenant; }
	private Tenant tenant;

	public TaskService getTaskService() { return this.taskService; }
	private TaskService taskService;

	public DocumentMgr getDocMgr() { return this.docMgr;  }
	public void setDocMgr(DocumentMgr docManager) { this.docMgr = docManager; }
	private DocumentMgr docMgr;

}
