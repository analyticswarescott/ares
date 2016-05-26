package com.aw.compute.streams.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aw.util.TwoKeyMap;

/**
 * A context for processing xml bundles
 *
 *
 *
 */
public class XMLBundleContext implements BundleContext {

	protected void cacheReferenceData(String tag, String keyAttribute, Map<String, Object> map) {

		Object keyValue = map.get(keyAttribute);

		//if we have the second key, put it in the map
		if (keyValue != null) {
			m_referenceData.put(tag, keyValue.toString().toUpperCase(), map);
		}

	}

	protected void cacheUad(Map<String, Object> map) {
		m_uads.add(map);
	}

	public List<Map<String, Object>> getUads() { return m_uads; }
	public void setUads(List<Map<String, Object>> uads) { m_uads = uads; }
	private List<Map<String, Object>> m_uads = new ArrayList<>();

	public TwoKeyMap<String, String, Map<String, Object>> getReferenceData() { return m_referenceData; }
	public void setReferenceData(TwoKeyMap<String, String, Map<String, Object>> referenceData) { m_referenceData = referenceData; }
	private TwoKeyMap<String, String, Map<String, Object>> m_referenceData = new TwoKeyMap<>();

	public String getTenantID() { return m_tenantID; }
	public void setTenantID(String id) { m_tenantID = id; }
	private String m_tenantID;

	public String getBundleID() { return m_bundleID; }
	public void setBundleID(String id) { m_bundleID = id; }
	private String m_bundleID;

	public String getMachineID() { return m_machineID; }
	public void setMachineID(String id) { m_machineID = id; }
	private String m_machineID;

}
