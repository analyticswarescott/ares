package com.aw.incident;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.aw.common.Tag;
import com.aw.common.auth.DefaultUser;
import com.aw.common.auth.User;
import com.aw.common.util.JSONUtils;
import com.aw.incident.Incident.Severity;
import com.aw.incident.Incident.State;

public class DefaultIncidentTest extends AbstractIncidentTest {

	@Test
	public void properties() throws Exception {

		DefaultIncident incident = new DefaultIncident();

		User user = new DefaultUser("jlehmann");

		//set some properties
		incident.setName("test name");
		incident.setAssignee(user);
		incident.setCloseComment(new DefaultComment(user, "test close comment"));
		incident.setCreationType(CreationType.MANUAL);
		incident.setCreator(user);
		incident.setModifiedTime(new Date());
		incident.setSeverity(Severity.HIGH);
		incident.setState(State.CREATED);
		incident.addTag(Tag.valueOf("test1"));
		incident.addTag(Tag.valueOf("test2"));

		String strIncident1 = JSONUtils.objectToString(incident);

		//build another incident
		Incident incident2 = JSONUtils.objectFromString(strIncident1, Incident.class, false, true);

		//make sure they're equal after loading back from json
		assertEquals("incident loaded back from json should have been the same", incident, incident2);
		assertEquals("incident equals should have equal hashcodes", incident.hashCode(), incident2.hashCode());
		assertEquals("tags missing", 2, incident2.getTags().size());
		assertTrue("tag missing", incident2.getTags().contains(Tag.valueOf("test1")));

		//make sure the converted json is equal after 2 toJsons
		String strIncident2 = JSONUtils.objectToString(incident);
		assertTrue("incident not equal to itself after turning back to json again", JSONUtils.jsonEquals(strIncident1, strIncident2));

		DefaultIncident incidentEmpty1 = new DefaultIncident();

		String str1 = JSONUtils.objectToString(incidentEmpty1);

		Incident incidentEmpty2 = JSONUtils.objectFromString(str1, Incident.class, false, true);

		assertEquals("incidents should be equal after json conversion", incidentEmpty1, incidentEmpty2);
		assertNotEquals("incident full and empty should not be equal", incident, incidentEmpty1);
		assertNotEquals("incident full and empty should not be equal", incidentEmpty1, incident);
	}

}
