package com.aw.tools.scale;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Settings for a test
 */
public class TestDef implements Runnable {



	public ArrayList<TestTenantGroup> getTenantGroups() {
		return tenantGroups;
	}

	public void setTenantGroups(ArrayList<TestTenantGroup> tenantGroups) {
		this.tenantGroups = tenantGroups;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	private String testName;
  private ArrayList<TestTenantGroup> tenantGroups = new ArrayList<>();



  @JsonIgnore
  ExecutorService groupRunner;


  @JsonIgnore
  public void run() {

	  groupRunner = Executors.newFixedThreadPool(tenantGroups.size());

	  for (TestTenantGroup ttg : tenantGroups) {;
		  groupRunner.submit(ttg);
	  }

	  groupRunner.shutdown();

  }


}
