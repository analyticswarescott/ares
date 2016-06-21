package com.aw.unity;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.DefaultSecurityContext;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.tenant.Tenant;

public class UnityUnitTest {

    protected void setThreadSecurity(String tid, String userID, String userName) {
        //TODO: how and where to establish system access for internal ops
        AuthenticatedUser u = new AuthenticatedUser();
        u.setTenant(tid);
        u.setName(userName);
        u.setId(userID);
        u.setLocale(Tenant.SYSTEM_TENANT_LOCALE);

        PlatformSecurityContext sec = new DefaultSecurityContext(u);
        ThreadLocalStore.set(sec);
    }

}
