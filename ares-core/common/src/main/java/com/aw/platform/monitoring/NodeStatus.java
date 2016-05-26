package com.aw.platform.monitoring;

import com.aw.platform.monitoring.os.OSPerfStats;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the State and last status activity of a Platform Node
 */
public class NodeStatus {

	/**
	 * The state of a particular node
	 */
	public enum State {
		STOPPING,
		STOPPED,
		STARTING,
		UPGRADING,
		RUNNING,
		UNKNOWN,
		ERROR;

		@JsonCreator
		public static State fromString(String str) { return valueOf(str.toUpperCase()); }
	}


	//Node current performance metrics for last collected interval

	@JsonProperty("perfStats")
	public OSPerfStats getPerfStats() {
		return perfStats;
	}
	public void setPerfStats(OSPerfStats perfStats) {
		this.perfStats = perfStats;
	}
	private OSPerfStats perfStats;


	//Node state and status

	public State getState() { return m_state; }
	public void setState(State state) { m_state = state; }
	private State m_state;

	/**
	 * @return Most recent status message
	 */
	public String getStatusMessage() { return m_statusMessage; }
	public void setStatusMessage(String statusMessage) { m_statusMessage = statusMessage; }
	private String m_statusMessage = "(none)";

	/**
	 * @return confirmed patch level (version number)
	 */
	public String getVersion() { return m_version; }
	public void setVersion(String version) { m_version = version; }
	private String m_version = "";

	public long getStatusTime() {return m_time;}
	public void setStatusTime(long time) {m_time = time;}
	private long m_time = System.currentTimeMillis();

}
