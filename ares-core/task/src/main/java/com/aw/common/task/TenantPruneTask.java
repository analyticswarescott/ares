package com.aw.common.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.inject.Provider;

import com.aw.common.util.es.ESKnownIndices;
import org.apache.log4j.Logger;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.task.TaskStatus.State;
import com.aw.common.task.exceptions.TaskException;
import com.aw.common.task.exceptions.TaskInitializationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.TimeSource;
import com.aw.common.util.es.ESClient;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;

public class TenantPruneTask extends AbstractTask {

	private static final Logger LOGGER = Logger.getLogger(TenantPruneTask.class);
	private Provider<Platform> platform;
	private PlatformMgr platformMgr;
	private TimeSource time;
	private State state = State.PENDING;

	@Override
	public void initialize(TaskContext ctx) throws TaskInitializationException {

		try {

			this.platform = ctx.getPlatformMgr();
			this.platformMgr = ctx.getPlatformMgr();

			//get all tenants
			this.tenants = ctx.getDocMgr().getSysDocHandler().getBodiesAsObjects(DocumentType.TENANT, Tenant.class);

			this.time = ctx.getTimeSource();

		} catch (Exception e) {
			throw new TaskInitializationException("error setting up pruning task", e);
		}

	}

	@Override
	public TaskStatus getStatus() throws TaskException {

		TaskStatus ret = new TaskStatus();

		//set some basic status information
		ret.setProgress(0);
		ret.setState(state);
		ret.setStatusMessage("pruning tenants: " + tenants);

		return ret;

	}

	@Override
	public void execute() throws Exception {

		state = State.RUNNING;

		Exception exception = null;

		try {

			for (Tenant tenant : tenants) {

				try {

					//just prune elasticsearch for now
					pruneElasticsearch(tenant);

				} catch (Exception e) {
					exception = e;
					this.platformMgr.handleError(EnvironmentSettings.getHost(), new Exception("error pruning tenant " + tenant.getTenantID(), e), NodeRole.REST);
				}

			}

		} finally {
			state = (exception == null ? State.SUCCESSFUL : State.FAILED);
		}

		if (exception != null) {
			throw exception;
		}

	}

	protected void pruneElasticsearch(Tenant tenant) throws Exception {

		ESClient client = newESClient(platform.get());
		for (ESKnownIndices index : ESKnownIndices.values()) {

			List<String> indices = client.getAllIndices(tenant, index);
			prune(tenant, index, indices, client);

		}

	}

	protected ESClient newESClient(Platform platform) {
		return new ESClient(platform);
	}

	/**
	 * prune the list of indices belonging to the given index to the given date
	 *
	 * @param index
	 * @param indices
	 */
	private void prune(Tenant tenant, ESKnownIndices index, List<String> indices, ESClient client) throws Exception {

		Duration retention = tenant.getRetention(index);
		Instant cutoff = time.now().minus(retention);

		LOGGER.info("pruning data for " + tenant.getTenantID() + ", index=" + index + " retention=" + retention + " cutoff=" + cutoff);

		for (String curIndex : indices) {

			if (index.getEarliest(tenant, curIndex).isBefore(cutoff) &&
				index.getLatest(tenant, curIndex).isBefore(cutoff)) {

				LOGGER.info("database index before cutoff of " + cutoff + " for tenant " + tenant.getTenantID() + ": " + curIndex);
				client.deleteIndex(index, curIndex);

			}

		}

	}

	@Override
	public void stop() {

		//nothing really to do here yet

	}

	@Override
	public void shuttingDown() {

		//nothing really to do here yet

	}

	//the tenants for which we will be pruning
	private Collection<Tenant> tenants;

}
