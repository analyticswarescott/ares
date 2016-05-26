package com.aw.compute.streams.processor;

import org.codehaus.jettison.json.JSONObject;

public class JSONBundleContext implements BundleContext {

	public JSONBundleContext(JSONObject bundle) throws Exception {

		m_bundle = bundle;

		//get the basic properties
		m_tenantID = m_bundle.getString("tid");
		m_bundleID = m_bundle.getString("bid");
		m_machineID = m_bundle.getString("mid");

	}

	public JSONObject getBundle() { return m_bundle; }
	private JSONObject m_bundle;

	public String getTenantID() { return m_tenantID; }
	private String m_tenantID;

	public String getBundleID() { return m_bundleID; }
	private String m_bundleID;

	public String getMachineID() { return m_machineID; }
	private String m_machineID;

}
