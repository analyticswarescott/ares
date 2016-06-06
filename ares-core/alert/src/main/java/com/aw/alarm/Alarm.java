package com.aw.alarm;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import com.aw.document.DocumentType;
import com.aw.unity.Data;
import com.aw.unity.dg.CommonField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An alarm represents something that occured of interest, but may not represent an incident yet.
 *
 * TODO: Implement this; this class is not completed yet, it is only a placeholder.
 *
 *
 *
 */
public interface Alarm {

	/**
	 * @return the time this alarm occurred
	 */
	public Instant getTime();

	/**
	 * @return the type of rule that generated this alarm
	 */
	public DocumentType getRuleType();

	/**
	 * The data representation of the alarm. A single alarm may represent multiple data objects. A requirement of this method is that
	 * every call will return the same Data instances with the same guids. In other words guids cannot be generated each time
	 * asData() is called.
	 *
	 * @return get alarm as a collection of unity data
	 */
	public Collection<Data> asData();

	/**
	 * @return the guid of the alarm
	 */
	@JsonProperty(CommonField.ARES_GUID)
	public UUID getGuid();

}
