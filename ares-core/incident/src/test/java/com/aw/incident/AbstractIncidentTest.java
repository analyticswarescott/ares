package com.aw.incident;

import com.aw.common.rest.AdminMgr;
import com.aw.common.rest.security.DefaultSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import org.junit.Before;

public class AbstractIncidentTest {

	@Before
	public void before() throws Exception {
		ThreadLocalStore.set(new DefaultSecurityContext(AdminMgr.SYSTEM_USER));
	}

}
