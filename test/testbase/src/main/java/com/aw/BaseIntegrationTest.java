package com.aw;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.aw.common.hadoop.structure.TargetPath;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.cli.LocalFileClient;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpStatusUtils;
import com.aw.document.Document;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.util.Statics;

public abstract class BaseIntegrationTest extends BaseFunctionalTest {

	//if system property set, will show child process logs (spark, elasticsearch, etc)
	public static final String SHOW_CHILD_LOGS = "showChildLogs";

	//location for cached platform for integration tests
	private static final File RELATIVE_PATH_TO_PLATFORM = new File("./conf/platform.json");

	 @Rule public TestName name = new TestName();

    @Before
    public final void beforeIntegration() throws Exception {

        //so I can see what the heck is being tested
    	System.out.println("\n\n=== " + getClass().getSimpleName() + "#" + name.getMethodName() + " ===\n\n");

		//delete all local data
		File data = new File(EnvironmentSettings.getDgData());
		FileUtils.deleteDirectory(data);

    	new File(EnvironmentSettings.getDgData()).mkdirs();

    	System.setProperty(EnvironmentSettings.Setting.FIRST_NODE.name().toUpperCase(), "true");



		String rpp = RELATIVE_PATH_TO_PLATFORM.getAbsolutePath();
		System.setProperty(EnvironmentSettings.Setting.PLATFORM_PATH.name(), rpp);
		//System.setProperty(EnvironmentSettings.Setting.PLATFORM_PATH.name(), getConfDirectory());


		//remove cached platform if exists
		File platformCache = new File (EnvironmentSettings.getPlatformPath());
		if (platformCache.exists()) {
			FileUtils.forceDelete(platformCache);
		}

		//call the setup script's method to create a cached platform
		File platformCacheSource = new File(getConfDirectory() + File.separatorChar + "defaults/platform/local.json");

		//File platformCacheSource =

		System.out.println(" P Cache Source: " + platformCache.getAbsolutePath().toString() );

		LocalFileClient.initPlatformCache(platformCacheSource, PORT);

		if (!platformCache.exists()) {
			throw new Exception("Could not find platform json to cache at  " + platformCacheSource);
		}

    	setThreadSystemAccess();

        //clean db directory - TODO: clean this up when we clean up integration test constructors!!

		//clean config
		File f = new File(EnvironmentSettings.getDgData()+ File.separatorChar + "config");
		FileUtils.deleteDirectory(f);
		if (f.exists()) {
			logger.warn(" error removing data/config directory during platform startup");
		}

    	//reset documents
    	removeDocuments();

        initDocuments();

    	setupPlatform();

    }

    protected void removeDocuments() throws Exception {

    	setThreadSystemAccess();
    	TestDependencies.getDocMgr().get().initDocuments();

    	if (TestDependencies.getDocMgr().get().isInitialized()) {

    		//ignore consistency issues
    		TestDependencies.getDocMgr().get().getSysDocHandler().setBootstrapping(true);

    		for (Document tenantDoc : TestDependencies.getDocMgr().get().getSysDocHandler().getAllTenants()) {
    			Tenant tenant = new Tenant(tenantDoc.getBody());
    			if (!tenant.getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
    				TestDependencies.getDocMgr().get().removeDocHandler(tenant.getTenantID());
    			}
    		}

    	}

    	//last remove system tenant
		TestDependencies.getDocMgr().get().removeDocHandler(Tenant.SYSTEM_TENANT_ID);

    	//reset initialized flag
    	TestDependencies.getDocMgr().get().reset();

    }

    protected void initDocuments() throws Exception {

    	//reinitialize document db
    	TestDependencies.getDocMgr().get().initDocuments();

    }

    /**
     * Don't delete documents, but reset internal document manager state
     *
     * @throws Exception
     */
    protected void resetDocuments() throws Exception {

    	TestDependencies.resetDocs();

    }

    protected void populateIndexes(String tenantID) throws Exception {
        for ( int i = 0; i < 5; i++ ) {
            createDatapoint(tenantID, "chrome.exe", "aron");
            createDatapoint(tenantID, "chrome.exe", "allen");
            createDatapoint(tenantID, "firefox.exe", "aron");
        }

        // wait for things to index
        Thread.sleep(3000);
    }

    protected void createDatapoint(String tenantID, String processName, String username) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(elasticQueryBase() + "/" + tenantID + "_dlp/machine_event/");

        JSONObject document = new JSONObject();
        document.put("in_", processName);
        document.put("event_count", 1);
        document.put("un", username);

        StringEntity params = new StringEntity(document.toString());
        post.addHeader("content-type", "application/json");
        post.setEntity(params);

        CloseableHttpResponse execute = httpClient.execute(post);
        if ( execute.getStatusLine().getStatusCode() != 201 ) {
            throw new RuntimeException("Failed to create datapoint: " + execute.getStatusLine().getStatusCode());
        }
    }

    @After
    public void after() throws Exception {

        teardownPlatform();

        super.after();

    }

    /**
     * GETs the path until a success code or the timeout is reached
     *
     * @param client The http client to use for the request
     * @param request The request to execute
     * @return If a successful return code was received within the timeout
     * @throws Exception If anything goes wrong
     */
    public static boolean waitForSuccess(HttpClient client, HttpUriRequest request) throws Exception {

        int tries = 0;
        Exception lastE = null;
        while (tries < 30) {
        	try {
            	HttpResponse r = client.execute(request);
				System.out.println(request.toString() + " yields " + r.toString());
            	if (org.eclipse.jetty.http.HttpStatus.isSuccess(r.getStatusLine().getStatusCode())) {
            		break;
            	}
        	} catch (Exception e) {
        		lastE = e;
        	}
        	tries++;
			System.out.println(" service start wait for " + request.getURI().toString());
        	Thread.sleep(1000L);
        }

        return tries < 30;

    }

    protected void teardownPlatform() throws Exception {

    	boolean shutdownErrors = shutdown(() -> {

    		//make sure we're not running a task
    		TestDependencies.getTaskContainer().get().shuttingDown();

        	stopRest();
            restService = null;

            return false;

    	});

    	shutdownErrors |= shutdown(() -> { TestDependencies.getRestCluster().get().getOpSequencer().close(); return false; });

    	shutdownErrors |= shutdown(() ->  { stopHadoop(); return false; });

		shutdownErrors |= shutdown(() -> { stopKafka(); return false; });

		shutdownErrors |= shutdown(() -> { stopElasticsearch(); return false; });

		shutdownErrors |= shutdown(() -> {
	        if (usesSpark()) {
	        	stopSpark();
	        }
	        return false;
		});

		shutdownErrors |= shutdown(() -> {
	        //delete local cache directory
	        TargetPath targetPath = new TargetPath(new Path(MiniHadoopServiceWrapper.LOCAL_CACHE_DIR), TestDependencies.getPlatform().get());
	        if (!targetPath.delete()) {
	        	throw new Exception("Could not delete hdfs path " + targetPath.getPath());
	        }
	        return false;
		});

		shutdownErrors |= shutdown(() -> {
			//delete the spark work directory
			if (usesSpark()) {
				File f = new File(EnvironmentSettings.getDgSparkHome() + File.separatorChar + "work");
				FileUtils.deleteDirectory(f);
				if (f.exists()) {
					logger.warn(" error removing Spark work directory during platform teardown");
				}
			}
			return false;
		});

		shutdownErrors |= shutdown(() -> {
			//delete all local data
			File data = new File(EnvironmentSettings.getDgData());
			FileUtils.deleteDirectory(data);
			return false;
		});

		assertFalse("there were errors shutting down, see output for details", shutdownErrors);
    }

    /**
     * shutdown, return if there were exceptions
     *
     * @param c the callable to execute to shut something down
     * @return whether there were exceptions during shutdown
     */
    protected boolean shutdown(Callable<Boolean> c) {

    	boolean ret = false;

    	try {
    		ret = c.call();
    	} catch (Exception e) {
    		e.printStackTrace();
    		ret = true;
    	}

    	return ret;

    }

    /**
     * Override if you use elasticsearch in your test
     * @return Whether this test uses elasticsearch
     */
    protected boolean usesElasticsearch() {
    	return true;
    }

    protected boolean usesSpark() {
    	return false;
    }

    protected boolean startsSpark() {
    	return false;
    }

    protected boolean usesKafka() {
    	return true;
    }

    protected boolean startsRest() {
        return true;
    }

    /**
     * @return Whether a real hadoop server should be used
     */
    protected boolean usesHadoop() {
    	return false;
    }

	protected boolean usesPostgres() {return false;}

    protected void setupPlatform() throws Exception {

    	//start things in parallel - create an executor for each startup thread and a completion service
    	Executor executor =  Executors.newFixedThreadPool(NodeRole.values().length + 1);
        CompletionService<Boolean> services = new ExecutorCompletionService<>(executor);

        //setup docs first
        setupDocs();

        //set up the process flags here
        System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, String.valueOf(usesSpark()));

        if ( usesElasticsearch()) {
        	System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, String.valueOf(usesElasticsearch()));
        }

        //enable spark if needed
        if (usesSpark()) {
            System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, String.valueOf(true));
        }

        else {
            System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, String.valueOf(false));
        }

		if(usesPostgres()) {
			System.setProperty(Statics.PROP_POSTGRES_ENABLED, String.valueOf(true));
		} else {
			System.setProperty(Statics.PROP_POSTGRES_ENABLED, String.valueOf(false));
		}
        //startup in parallel
        services.submit(() -> { startHadoop(); return true; });
        services.submit(() -> { if (usesKafka()) startKafka(); return true; });
        services.submit(() -> { if (usesElasticsearch()) startElasticSearch(); return true; });
        services.submit(() -> { if (startsSpark()) startSpark(); return true; });
		services.submit(() -> { if(usesPostgres()) startPostgres(); return true; });

        int servicesExpected = 4;
        int servicesStarted = 0;
        long start = System.currentTimeMillis();

        //wait until all are done
        while (servicesStarted < servicesExpected) {

        	//wait up to 30 seconds for everything to start
        	assertTrue(System.currentTimeMillis() - start < 30000L);

        	//get the result
        	services.take().get();
        	servicesStarted++;

        }

        //clear platform and let REST load it
        if (TestDependencies.getPlatformMgr() != null) {
        	TestDependencies.getPlatformMgr().get().setPlatform(null);
        }

        if ( startsRest() ) {
            startRest();
        }

    }

    protected boolean setupDocs() throws Exception {

    	//set platform in non-jetty class loader
    	setThreadSystemAccess();
        Document doc = getDefaultPlatformDocAsNewName(Platform.LOCAL);

        //save it to the platform_path location
        FileUtils.write(RELATIVE_PATH_TO_PLATFORM, doc.toJSON());

        if (TestDependencies.getPlatformMgr() != null) {
        	TestDependencies.getPlatformMgr().get().setPlatform(null);
        }

        //delete local cache directory
        TargetPath targetPath = new TargetPath(new Path(MiniHadoopServiceWrapper.LOCAL_CACHE_DIR), TestDependencies.getPlatform().get());
        if (!targetPath.delete()) {
        	throw new Exception("Could not delete hdfs path " + targetPath.getPath());
        }

        return true;

    }

    public String elasticQueryBase() {
        return "http://localhost:" + ElasticSearchServiceWrapper.create().port;
    }

    protected HttpGet get(String URL) {
        return new HttpGet("http://localhost:" + getRestPort() + URL);
    }

    protected CloseableHttpResponse get(String url, String accessToken) throws Exception {
        HttpGet get = get(url);
        get.addHeader("accessToken", accessToken);
        return client().execute(get);
    }

    protected HttpPost post(String URL) {
        return new HttpPost("http://localhost:" + getRestPort() + URL);
    }

    protected HttpPost post(String URL, JSONObject hudMeta) throws Exception {
    	return post(URL, hudMeta.toString());
    }

    protected HttpPost post(String URL, String payload) throws Exception {
        HttpPost httpPost = new HttpPost("http://localhost:" + getRestPort() + URL);
        httpPost.addHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(payload));
        return httpPost;
    }

    protected HttpPut put(String URL, JSONObject payload) throws Exception {
    	return put(URL, payload.toString());
    }

    protected HttpPut put(String URL, String payload) throws Exception {
        HttpPut httpPut = new HttpPut("http://localhost:" + getRestPort() + URL);
        httpPut.addHeader("Content-type", "application/json");
        httpPut.setEntity(new StringEntity(payload));
        return httpPut;
    }

    protected CloseableHttpResponse put(String url, JSONObject payload, String accessToken) throws Exception {
        HttpPut put = put(url, payload);
        put.addHeader("accessToken", accessToken);
        return client().execute(put);
    }

    protected CloseableHttpResponse post(String url, JSONObject payload, String accessToken) throws Exception {
        HttpPost post = post(url, payload);
        post.addHeader("accessToken", accessToken);
        return client().execute(post);
    }

    protected HttpPatch patch(String URL, JSONObject payload) throws Exception {
        HttpPatch httpPatch = new HttpPatch("http://localhost:" + getRestPort() + URL);
        httpPatch.addHeader("Content-type", "application/json");
        httpPatch.setEntity(new StringEntity(payload.toString()));
        return httpPatch;
    }

    protected String fetchAccessToken(String tenantID, String userName, String password) throws Exception {
        HttpResponse response = doLogin(tenantID, userName, password, UUID.randomUUID().toString() );
        JSONObject entity = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
        org.junit.Assert.assertNotNull(entity);
        return (String) entity.get("accessToken");
    }

    protected HttpResponse doLogin(String tenantID, String userName, String password, String domainID) throws Exception {
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

    protected Object getJSONEntity( HttpResponse response ) throws Exception {

    	String data = IOUtils.toString(response.getEntity().getContent());
    	if (!HttpStatusUtils.isSuccessful(response.getStatusLine().getStatusCode())) {
    		throw new Exception("Unexpected http error: " + response.getStatusLine().getStatusCode() + " : " + data);
    	}
    	try {

        	return new JSONObject(data);

        } catch (Exception e) {

        	//try array
        	return new JSONArray(data);

        }
    }

    protected CloseableHttpClient client() {
        return HttpClientBuilder.create().build();
    }

    protected boolean contains(JSONArray ary, String str) throws Exception {
    	for (int x=0; x<ary.length(); x++) {
    		if (ary.getString(x).equals(str)) {
    			return true;
    		}
    	}
    	return false;
    }



}
