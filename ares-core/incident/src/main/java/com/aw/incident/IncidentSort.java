package com.aw.incident;

/**
 * @author jhaight
 */
public enum IncidentSort {
	/**
	 * The newest created dates first
	 */
	NEWEST_FIRST,
	/**
	 * The oldest created dates first
	 */
	OLDEST_FIRST,
	/**
	 * The highest severity first, secondarily sorted by oldest dates first
	 */
	HIGH_SEVERITY_FIRST,
	/**
	 * The lowest severity first, secondarily sorted by newest dates first
	 */
	LOW_SEVERITY_FIRST,
	/**
	 * The newest modified dates first
	 */
	LAST_MODIFIED_FIRST
}
