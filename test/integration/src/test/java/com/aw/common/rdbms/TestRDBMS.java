package com.aw.common.rdbms;

import com.aw.BaseIntegrationTest;
import com.aw.common.system.TenantSettings;
import com.aw.common.tenant.Tenant;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

public class TestRDBMS extends BaseIntegrationTest {

    @Test
    public void testDBFencing() throws Exception {

        Tenant t1 = new Tenant("1");

        Tenant t2 = new Tenant("2");
        t2.getEnvOverrides().put(TenantSettings.Setting.JDBC_URL, "jdbc:derby:" + getConfDirectory() + File.separatorChar + "custom;create=true");

        provisionTenant(t1.getTenantID());
        provisionTenant(t2.getTenantID());

        String username = UUID.randomUUID().toString();
        String userAccessToken = fetchAccessToken("1", username, "test");

        // get user prefs
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username);
        get.addHeader("accessToken", userAccessToken);
        HttpResponse response = httpClient.execute(get);
        Assert.assertEquals("Expected successful GET user meta request", 200, response.getStatusLine().getStatusCode());

        username = UUID.randomUUID().toString();
        userAccessToken = fetchAccessToken("2", username, "test");

        // get user prefs
        httpClient = HttpClientBuilder.create().build();
        get = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username);
        get.addHeader("accessToken", userAccessToken);
        response = httpClient.execute(get);
        Assert.assertEquals("Expected successful GET user meta request", 200, response.getStatusLine().getStatusCode());

    }

}
