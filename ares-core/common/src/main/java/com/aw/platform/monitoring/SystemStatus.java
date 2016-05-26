package com.aw.platform.monitoring;

/**
 * System level status information
 *
 *
 *
 */
public class SystemStatus {

	public double getMemoryUsage() { return this.memoryUsage;  }
	public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
	private double memoryUsage;

	public double getCpuLoad() { return this.cpuLoad;  }
	public void setCpuLoad(double cpuLoad) { this.cpuLoad = cpuLoad; }
	private double cpuLoad;

}
