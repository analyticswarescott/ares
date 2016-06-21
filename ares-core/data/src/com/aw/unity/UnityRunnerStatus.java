package com.aw.unity;

import com.aw.unity.UnityRunner.State;

/**
 * The status of a unity runner
 *
 *
 */
public class UnityRunnerStatus {

	public UnityRunnerStatus() {
	}

	public UnityRunnerStatus(UnityRunner runner) {
		m_state = runner.getState();
		m_message = runner.getErrorMessage();
	}

	public UnityRunnerStatus(State state, String message) {
		m_state = state;
		m_message = message;
	}

	public State getState() { return m_state; }
	public void setState(State state) { m_state = state; }
	private State m_state;

	public String getMessage() { return m_message; }
	public void setMessage(String message) { m_message = message; }
	private String m_message;

}
