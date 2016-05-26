package com.aw.common.cluster.db;

import com.aw.common.rest.security.TenantAware;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.common.zookeeper.structure.ZkPurpose;
import com.aw.platform.Platform;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Tenant-type level operation sequencer for DB write operations
 */
public class DefaultOpSequencer implements OpSequencer,  TenantAware {
	public static final Logger logger = LoggerFactory.getLogger(DefaultOpSequencer.class);

	private Map<String, OpSequenceListener> listeners = new HashMap<String, OpSequenceListener>();
	private ZkAccessor tenantZkAcccessor;

	protected OpSequenceListener getSequenceListener(String fullKey) {
		return listeners.get(fullKey);
	}


	public DefaultOpSequencer(Platform platform, ZkAccessor tenantZkAcccessor) {
		this.tenantZkAcccessor = tenantZkAcccessor;
	}

	public String getSequencePath(String key) {
		//TODO: make ZK base path public or always perform these ops from inside the ZKCluster class?  Maybe add to ClusterAware interface?
		return  tenantZkAcccessor.getAbsoluteZkPath(ZkPurpose.OP_SEQUENCE, key);
	}

	protected Map<String, OpSequenceListener> getListeners() {
		return listeners;
	}


	public int getSequenceValue(CuratorFramework client, String key) throws Exception {
		String fullKey = getSequencePath(key);
		OpSequenceListener l = getSequenceListener(fullKey);
		if (l == null) {
			l = getSequencer(client, key);
		}
		return l.getCurrentValue();
	}

	public int incrementSequence(CuratorFramework client, String key) throws Exception{
		String fullKey = getSequencePath(key);
		OpSequenceListener l = getSequenceListener(fullKey);

		if (l == null) {
			l = getSequencer(client, key);
		}

		return l.incrementValue();

	}

	public  OpSequenceListener getSequencer(CuratorFramework client, String key)  throws Exception{

		String fullKey = getSequencePath(key);

		if (listeners.containsKey(fullKey)) {
			logger.warn( " sequence already exists with key " + fullKey);
			return listeners.get(fullKey);
		}
		else {
			return add(client, fullKey);
		}
	}

	private synchronized OpSequenceListener add(CuratorFramework client, String fullKey) throws Exception {
		SharedCount sc = new SharedCount(client, fullKey, 0);
		sc.start();
		int foo = sc.getCount(); //intialize?
		OpSequenceListener l = new OpSequenceListener(getTenantID(), fullKey, sc);
		sc.addListener(l);
		listeners.put(fullKey, l);
		return l;
	}

	public void close() throws  Exception{
		for (OpSequenceListener l : listeners.values()) {
			l.close();
		}
	}

	public void closeCurrentTenant() throws  Exception {
		for (OpSequenceListener l : listeners.values()) {
			if (l.getTenantID().equals(getTenantID())) {
				l.close();
			}
		}
	}



}
