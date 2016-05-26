package com.aw.common.cluster.db;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.recipes.shared.VersionedValue;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a shared counter for op sequences
 */
public class OpSequenceListener implements SharedCountListener {

	public static final Logger logger = LoggerFactory.getLogger(OpSequenceListener.class);


	private  SharedCount m_sc;
	private String m_key;

	private int currVal = 0;


	public int getCurrentValue() {
		VersionedValue<Integer> cv = m_sc.getVersionedValue();
		return cv.getValue();
	}

	public synchronized int incrementValue() throws Exception{

		VersionedValue<Integer> cv = m_sc.getVersionedValue();
		int newCount = cv.getValue() + 1;

		//TODO: how to decide timeout?
		long l = System.currentTimeMillis();
		while (true) {
		  boolean b =	m_sc.trySetCount(cv, newCount);
			if (b) {
				currVal = newCount;
				/*if (m_key.endsWith("1")) {
					System.out.println(Thread.currentThread().getName() + " OK:  set counter for " + m_key + " to " + newCount);
				}*/
				return newCount;
			}
			else { //someone else has hit it since we last read, so we need to increment
				cv = m_sc.getVersionedValue();
				newCount = cv.getValue() + 1;
				/*if (m_key.endsWith("1")) {
					System.out.println(Thread.currentThread().getName() + " failed to set counter for " + m_key);
				}*/
				if (System.currentTimeMillis() - l > 30000) {
					throw new Exception(Thread.currentThread().getName() +  " timout setting shared counter");
				}
			}
		}

	}

	public OpSequenceListener(String tenantID, String key, SharedCount sc) { //TODO: add a callback for lost connections?
		m_key = key;
		m_sc = sc;
		setTenantID(tenantID);

	}


	@Override
	public void countHasChanged(SharedCountReader sharedCountReader, int i) throws Exception {
		//logger.warn(" count changed for " + m_key + " to value " + i);
		currVal = i;
	}

	@Override
	public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {

	}

	public void close() throws Exception{
		m_sc.close();
	}

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	private String tenantID;
}
