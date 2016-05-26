package com.aw.tools.scale;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aw.common.tenant.Tenant;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;
import com.aw.tools.generators.BundleGenerator;
import com.aw.tools.generators.EDRGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Definition for launching a group of like test tenants
 */
public class TestTenantGroup implements Runnable {



	@JsonIgnore
	private Map<Integer, PlatformClient> m_clients = new HashMap<>();


	private String groupName = "TenantGroup1"; //to allow namespaced-descriptive generated tenant IDs like small-1 or large-3


	public String getEdrSourcePath() {
		return edrSourcePath;
	}

	public void setEdrSourcePath(String edrSourcePath) {
		this.edrSourcePath = edrSourcePath;
	}

	private String edrSourcePath;

	public int getFilesPerScan() {
		return filesPerScan;
	}

	public void setFilesPerScan(int filesPerScan) {
		this.filesPerScan = filesPerScan;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getNumTenants() {
		return numTenants;
	}

	public void setNumTenants(int numTenants) {
		this.numTenants = numTenants;
	}

	public int getNumMachines() {
		return numMachines;
	}

	public void setNumMachines(int numMachines) {
		this.numMachines = numMachines;
	}

	public int getNumDays() {
		return numDays;
	}

	public void setNumDays(int numDays) {
		this.numDays = numDays;
	}

	public int getAvgBundleSize() {
		return avgBundleSize;
	}

	public void setAvgBundleSize(int avgBundleSize) {
		this.avgBundleSize = avgBundleSize;
	}

	public long getBundlesPerDay() {
		return bundlesPerDay;
	}

	public void setBundlesPerDay(long bundlesPerDay) {
		this.bundlesPerDay = bundlesPerDay;
	}

	public int getNumScans() {
		return numScans;
	}

	public void setNumScans(int numScans) {
		this.numScans = numScans;
	}

	private int numTenants = 1;
	private int numMachines = 1;
	private int numDays = 1;

	private int avgBundleSize = 1;
	private long bundlesPerDay = 1;

	private int numScans = 1;
	private int filesPerScan = 1;

	public int getBundleThreadsPerTenant() {
		return bundleThreadsPerTenant;
	}

	public void setBundleThreadsPerTenant(int bundleThreadsPerTenant) {
		this.bundleThreadsPerTenant = bundleThreadsPerTenant;
	}

	public int getEdrThreadsPerTenant() {
		return edrThreadsPerTenant;
	}

	public void setEdrThreadsPerTenant(int edrThreadsPerTenant) {
		this.edrThreadsPerTenant = edrThreadsPerTenant;
	}

	private int bundleThreadsPerTenant = 1;
	private int edrThreadsPerTenant = 1;

	@JsonIgnore
	ExecutorService tenantBundleThreads;
	@JsonIgnore
	ExecutorService tenantEDRThreads;

	public void run() {

		tenantBundleThreads = Executors.newFixedThreadPool(numTenants * bundleThreadsPerTenant);
		tenantEDRThreads = Executors.newFixedThreadPool(numTenants * edrThreadsPerTenant);


		for (int i=1; i <= numTenants; i++) {

			//create the tenant
			String tenantID = getGroupName() + "_" + i;
			try {
				createTenant(tenantID);
			} catch (Exception e) {
				throw new RuntimeException("tenant creation error", e);
			}

			for (int dlp = 1; dlp <= getBundleThreadsPerTenant(); dlp ++) {
				//TODO: launch the generators
				bundleGenWrapper bgr = new bundleGenWrapper();
				bgr.tenantID = tenantID;
				bgr.threadNo = dlp;
				tenantBundleThreads.submit(bgr);
			}

			for (int edr = 1; edr <= getEdrThreadsPerTenant(); edr ++) {
				edrGenWrapper egr = new edrGenWrapper();
				egr.tenantID = tenantID;
				egr.threadNo = edr;
				tenantEDRThreads.submit(egr);
			}
		}

		tenantBundleThreads.shutdown();
		tenantEDRThreads.shutdown();

	}


	private void createTenant(String tenantID) throws Exception{
		Tenant t = Tenant.forId(tenantID);
		System.out.println(t.toString());

		PlatformClient pc = new PlatformClient(PlatformMgr.getCachedPlatform());

		if (pc.tenantExists(t)) {
			System.out.println(" tenant already exists with ID " + t.getTenantID());
		}
		else {
			pc.provision(t);
		}
	}

	class bundleGenWrapper implements Runnable {

		private String tenantID;

		public int getThreadNo() {
			return threadNo;
		}

		public void setThreadNo(int threadNo) {
			this.threadNo = threadNo;
		}

		public String getTenantID() {
			return tenantID;
		}

		public void setTenantID(String tenantID) {
			this.tenantID = tenantID;
		}

		private int threadNo;


		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		private String server;

		@Override
		public void run() {

			System.out.println(" would send bundles " + tenantID + "-" + threadNo + " to server " + server);

			BundleGenerator bg = new BundleGenerator();
			bg.setAverageBundleSize(getAvgBundleSize());
			bg.setDateRange(getNumDays());
			bg.setMachineCount(getNumMachines());
			bg.setEventsPerDay(getBundlesPerDay());
			bg.setFormat(BundleGenerator.Format.JSON);
			bg.setTenantID(tenantID);

			try {
				bg.execute();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}


		}
	}

	class edrGenWrapper implements Runnable {

		private String tenantID;

		public int getThreadNo() {
			return threadNo;
		}

		public void setThreadNo(int threadNo) {
			this.threadNo = threadNo;
		}

		public String getTenantID() {
			return tenantID;
		}

		public void setTenantID(String tenantID) {
			this.tenantID = tenantID;
		}

		private int threadNo;


		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		private String server;

		@Override
		public void run() {

			System.out.println(" would send EDR " + tenantID + "-" + threadNo + " to server " + server);

			EDRGenerator eg = new EDRGenerator(PlatformMgr.getCachedPlatform());
			eg.setAverageStaticFiles(getFilesPerScan());
			eg.setMachineCount(getNumMachines());
			eg.setRange(getNumDays());
			eg.setScans(getNumScans());
			eg.setTenantID(getTenantID());
			eg.setSource(getEdrSourcePath());

			try {
				eg.execute();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}


		}
	}

}
