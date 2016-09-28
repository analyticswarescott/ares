package com.aw;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.aw.util.RestServer;
import com.aw.util.Statics;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Assert;

import com.aw.common.rest.AdminMgr;
import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.PlatformMgr;

public abstract class RestServiceWrapper {

    public static final Logger logger = Logger.getLogger(RestServiceWrapper.class);

    private final int port;
    private RestServer server;
    private boolean running;

    /**
     * Create an instance of the aw service
     *
     * @param RestServicePort
     */
    public RestServiceWrapper(
    		String path,
            int port
    ) {
    	this(path, port, TestDependencies.class.getName());
    }
    public RestServiceWrapper(
    		String path,
            int port,
            String di
    ) {

    	//set up test dependency injector to give us access to common dependencies
    	System.setProperty(EnvironmentSettings.Setting.DEPENDENCY_INJECTOR.name(), di);
		System.setProperty("TEST_MODE", "true");



    	//set up the service properties - path and port
        if (port < 1) {
            logger.warn("No reportingServicePort provided, defaulting to 8080");
            port = 8080;
        }
        this.port = port;

        if ( path == null ) {
			for (File f : getRootDir().listFiles()) {
				if (f.isDirectory() && f.getAbsolutePath().endsWith("-apps")) {
					//TODO: improve naming convention
					path = f.getAbsolutePath();
				}
			}
        }

		if (path == null) {//assume testing setup

			throw new RuntimeException(" webapp project ending in -apps not found as required");
		}

        logger.info("starting service, path=" + path + " port=" + port);

        //start up the service

        server = newServer(path, this.port);
        try {
        	server.init(PlatformMgr.getCachedPlatform());
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

    protected abstract RestServer newServer(String basePath, int port);

    /**
     * @return The path to GET to see if the server is up - once a success return code (2xx) is returned from the GET, the server will be considered up
     */
    protected abstract String getCheckPath();

    public Future<Boolean> stop() throws Exception {

    	if (server == null) {
            throw new RuntimeException("Cannot stop, service not initialized!");
        }

        CompletableFuture<Boolean> callback = new CompletableFuture<>();
        server.stop();
        running = false;
        callback.complete(true);
        return callback;

    }

    public boolean isRunning() throws Exception {
        return running;
    }

    public void start()  throws Exception {

        logger.warn("Starting service.");


        server.addLifeCycleListener(new ServiceLifeCycleListener(new CompletableFuture<>()))
              .start();

		System.out.println("about to check running");
        //wait until we have a server running
        running = BaseIntegrationTest.waitForSuccess(client(), get(getCheckPath()));

        if (!running) {
        	Assert.fail("rest server failed to start (port=" + port + ")");
        }

    }

    public int getPort() {
        return this.port;
    }

    public HttpResponse patch(String url, JSONObject payload, String accessToken, Map<String, String> headers) throws Exception {
        HttpPatch patch = patch(url, payload);
        apply(accessToken, headers, patch);
        return client().execute(patch);
    }

    public HttpResponse get(String url, String accessToken, Map<String, String> headers) throws Exception {
        HttpGet get = get(url);
        apply(accessToken, headers, get);
        return client().execute(get);
    }

    public HttpResponse put(String url, Object payload, String accessToken, Map<String, String> headers) throws Exception {
        HttpPut put = put(url, payload);
        apply(accessToken, headers, put);
        return client().execute(put);
    }

    public HttpResponse post(String url, Object payload, String accessToken, Map<String, String> headers) throws Exception {
        HttpPost post = post(url, payload);
        apply(accessToken, headers, post);
        return client().execute(post);
    }

    public HttpResponse delete(String url, String accessToken, Map<String, String> headers) throws Exception {
        HttpDelete delete = delete(url);
        apply(accessToken, headers, delete);
        return client().execute(delete);
    }

    public String fetchAccessToken(String tenantID, String userName, String password) throws Exception {
        HttpResponse response = doLogin(tenantID, userName, password, UUID.randomUUID().toString() );
        JSONObject entity = (JSONObject) getJSONEntity(response);
        Assert.assertNotNull(entity);
        return (String) entity.get("accessToken");
    }

    public HttpResponse doLogin(String tenantID, String userName, String password, String domainID) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost post = post(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/Account/Login");

        // add header
        JSONObject login = new JSONObject();
        login.put("tenantKey", tenantID);
        login.put("userName", userName);
        login.put("password", password);
        login.put("domainId", domainID);

        StringEntity params = new StringEntity(login.toString());
        post.addHeader("content-type", "application/json");
        post.setEntity(params);

        HttpResponse response = httpClient.execute(post);
        return response;
    }

    public CloseableHttpClient client() {
        return HttpClientBuilder.create().build();
    }

    private File getRootDir() {
        File rootDir = new File( new File("").getAbsolutePath() );

        if ( rootDir.getAbsolutePath().endsWith( "integration" ) ) {
            rootDir = new File( rootDir.getParentFile().getParentFile().getAbsolutePath() );
        }

        return rootDir;
    }

    private class ServiceLifeCycleListener implements LifeCycle.Listener {

        private CompletableFuture<Boolean> callback;

        ServiceLifeCycleListener(CompletableFuture<Boolean> callback) {
            this.callback = callback;
        }

        @Override
        public void lifeCycleStarting(LifeCycle lifeCycle) {
        }

        @Override
        public void lifeCycleStarted(LifeCycle lifeCycle) {
            this.callback.complete(true);
        }

        @Override
        public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            this.callback.complete(false);
        }

        @Override
        public void lifeCycleStopping(LifeCycle lifeCycle) {
        }

        @Override
        public void lifeCycleStopped(LifeCycle lifeCycle) {
            this.callback.complete(true);
        }
    }

    private HttpPost post(String URL, Object payload) throws Exception {
        HttpPost httpPost = new HttpPost("http://localhost:" + this.port + URL);
        httpPost.addHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(payload.toString()));
        return httpPost;
    }

    private HttpPut put(String URL, Object payload) throws Exception {
        HttpPut httpPut = new HttpPut("http://localhost:" + this.port + URL);
        httpPut.addHeader("Content-type", "application/json");
        httpPut.setEntity(new StringEntity(payload.toString()));
        return httpPut;
    }

    private HttpPatch patch(String URL, Object payload) throws Exception {
        HttpPatch httpPatch = new HttpPatch("http://localhost:" + this.port + URL);
        httpPatch.addHeader("Content-type", "application/json");
        httpPatch.setEntity(new StringEntity(payload.toString()));
        return httpPatch;
    }

    private HttpDelete delete(String URL) throws Exception {
        HttpDelete httpDelete = new HttpDelete("http://localhost:" + this.port + URL);
        httpDelete.addHeader("Content-type", "application/json");
        return httpDelete;
    }

    private void apply(String accessToken, Map<String, String> headers, org.apache.http.HttpMessage verb) {
        if ( accessToken != null ) {
            verb.addHeader("accessToken", accessToken);
        }
        if ( headers != null ) {
            headers.keySet().stream().forEach( (key) -> {
                verb.addHeader(key, headers.get(key));
            });
        }
    }

    public HttpGet get(String URL) {
        return new HttpGet("http://localhost:" + this.port + URL);
    }

    public HttpPost post(String URL) {
        return new HttpPost("http://localhost:" + this.port + URL);
    }

    private Object getJSONEntity( HttpResponse response ) {
        try {
            String str = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return new JSONObject(str);
        } catch (Exception e) {
            return null;
        }
    }

}
