package com.aw;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.aw.common.util.es.ESKnownIndices;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.DefaultSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.EnvironmentSettings.Setting;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpStatusUtils;
import com.aw.common.util.JSONUtils;
import com.aw.compute.streams.processor.GenericESProcessor;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.document.DocumentType;
import com.aw.platform.DefaultPlatform;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.roles.Elasticsearch;
import com.aw.platform.roles.Rest;
import com.aw.unity.Data;
import com.aw.unity.UnityInstance;
import com.aw.unity.json.JSONDataType;
import com.aw.util.Statics;
import com.aw.utils.tests.KafkaZookeeperServiceWrapper;
import com.google.common.base.Preconditions;

import io.netty.handler.codec.http.HttpMethod;
import org.mockito.Mockito;

@SuppressWarnings("all")
public class BaseFunctionalTest {
    public static final Logger logger = Logger.getLogger(BaseFunctionalTest.class);

	private static final String DEFAULT_DRIVER = "driver30sec";
	private static final String DRIVER_EXTRA_JAVA_OPTIONS = "extra_java_options";
	private static final String DRIVER_BATCH_INTERVAL = "batch_inteval_seconds";
	private static final String DRIVER_WORK_POLL_INTERVAL = "work_poll_interval";


	// Default REST port
    protected static final int PORT = 9099;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // public


	public void setExtraSysProps() {

	}

	@Before
	public final void beforeFunctional() throws Exception {

		setExtraSysProps();

	/*	String confDirectoryPath = getConfDirectory();
		File confDirectory = new File(confDirectoryPath);
		if ( !confDirectory.exists() || !confDirectory.isDirectory() ) {
			throw new RuntimeException("Missing or invalid CONF_DIRECTORY environment variable. Please supply this to proceed.");
		}*/

		// Set defaults for the integration tests
		final Map<String, String> env = new HashMap<>();

		env.put( "MANUAL_SERVICES_START", "false"  );
		env.put( "UNITY_ES_HOST", "localhost"  );
		env.put( "SYS_SERVER_URL", "localhost:2181"  );
		env.put( "ZOO_ADDR", "localhost:2181"  );

		env.put("DISABLE_AUTH_VALIDATION", "false");
		env.put( "TEST_MODE", "true"  );

		File f = new File(".");
		String path = f.getAbsolutePath();



/*		File ffc = new File(path).getParentFile();

		System.out.println(" BASE CONF PATH is " + ffc.getAbsolutePath());


		String confPath = ffc.getAbsolutePath() + File.separatorChar + "conf";
		env.put( "CONF_DIRECTORY", confPath);*/

		//TODO: move this to wrapper
		env.put( "DG_SPARK_HOME", path + File.separatorChar + "spark_test");

		File ff = new File(path).getParentFile().getParentFile().getParentFile();
		String libPath = ff.getAbsolutePath() + File.separatorChar + "ares-core" + File.separatorChar + "compute" + File.separatorChar + "target" + File.separatorChar + "lib";
		env.put("SPARK_LIB_HOME", libPath);

		setEnv(env);

	}

	/**
	 * Allows setting a custom environment variable
     */
	protected final void customizeEnvironment(String property, String value) {

		Preconditions.checkArgument(property != null, "Property is required");
		Preconditions.checkArgument(value != null, "Value is required");

		final Map<String, String> env = new HashMap<>();

		for (Map.Entry entry : System.getProperties().entrySet()) {
			env.put((String) entry.getKey(), (String) entry.getValue());
		}
		env.put(property, value);

		setEnv(env);
	}

	@After
    public void after() throws Exception {

    	if (testDir != null) {
            // destroy testDir
            testDir.deleteOnExit();
    	}

    	TestDependencies.getDocMgr().get().reset();

        //clean docs
        File docs = new File (EnvironmentSettings.fetch(Setting.CONFIG_DB_ROOT));
        docs.deleteOnExit();

        TestDependencies.getPlatformMgr().get().reset();

    }

    public static void setThreadSystemAccess() {
        //TODO: how and where to establish system access for internal ops
        setThreadSecurity(Tenant.SYSTEM_TENANT_ID, Tenant.SYSTEM_TENANT_UID, Tenant.SYSTEM_TENANT_USER_NAME);
    }

    protected static void setThreadSecurity(String tid, String userID, String userName) {

        //TODO: how and where to establish system access for internal ops
        AuthenticatedUser u = new AuthenticatedUser();
        u.setTenant(tid);
        u.setName(userName);
        u.setId(userID);
        u.setLocale(Tenant.SYSTEM_TENANT_LOCALE);

        DefaultSecurityContext sec = new DefaultSecurityContext(u);
        ThreadLocalStore.set(sec);

    }

    /**
     * Create an instance of the aw service
     *
     */
    public BaseFunctionalTest() {

        Path test;
        try {

        	test = Files.createTempDirectory("test");
            testDir = test.toFile();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private File getRootDir() {
        File rootDir = new File( new File("").getAbsolutePath() );

		String s = rootDir.getAbsolutePath();
        if ( s.endsWith("integration") ) {
            rootDir = new File( rootDir.getParentFile().getParentFile().getAbsolutePath() );
        }
		else
		if ( s.endsWith( "testbase" ) ) {
			rootDir = new File( rootDir.getParentFile().getParentFile().getAbsolutePath() );
		}

        return rootDir;
    }

    @SuppressWarnings("all")
	private void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public  Document getDefaultPlatformDocAsNewName(String newName) throws Exception {
    	Document ret = new TestDocumentHandler().getDocument(DocumentType.PLATFORM, Platform.DEFAULT);
    	DefaultPlatform platform = ret.getBodyAsObject((DocumentHandler)null);

    	//set the test rest port
    	platform.getNode(NodeRole.REST).getSettings(NodeRole.REST).put(Rest.PORT, PORT);

    	//set the test cluster name for elasticsearch
    	platform.getNode(NodeRole.ELASTICSEARCH).getSettings(NodeRole.ELASTICSEARCH).put(Elasticsearch.CLUSTER_NAME, "elasticsearch");

        ret.setName(newName);
    	ret.setBody(new JSONObject(JSONUtils.objectToString(platform)));
    	return ret;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // platform services

    protected void startRest() throws Exception {
        startNodeService(); //always start node service first
        startRest(getDefaultPlatformDocAsNewName(Platform.LOCAL));
    }


    protected void startRest(Document platformDoc) throws Exception {

    	if (restService != null) {
    		stopRest();
    	}

        if ( restService == null ) {
            restService = ReportingServiceWrapper.create(PORT);
        }

        //initialize rest cluster here so we can perform some basic document operations
		TestDependencies.getRestCluster().get().init();

		//set it up for our class loader
        BaseFunctionalTest.setThreadSystemAccess();
        if ( platformDoc != null ) {

			TestDependencies.getDocMgr().get().getSysDocHandler().updateDocument(platformDoc);

			//update the driver doc with our test settings
			updateDriverDoc();

		}

		if ( !restService.isRunning() ) {
            restService.start();
        }

        //set it up for our class loader
        BaseFunctionalTest.setThreadSystemAccess();
        if ( platformDoc != null ) {

            //post DEFAULT platform doc to the wrapper, as it has loaded LOCAL by default
            DocumentHandler restie = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, TestDependencies.getPlatform().get());
            restie.updateDocument(platformDoc);

            if (TestDependencies.getPlatformMgr() != null) {
		        TestDependencies.getPlatformMgr().get().setPlatform(null);
            }

        }

    }

    protected void updateDriverDoc() throws Exception{

    	Document driver = TestDependencies.getDocMgr().get().getSysDocHandler().getDocument(DocumentType.STREAM_DRIVER, DEFAULT_DRIVER);

    	if (driver == null) {
    		throw new Exception("required driver document " + DEFAULT_DRIVER + " not found");
    	}

/*		File rootDir = getRootDir();
		File platformDir = rootDir.getParentFile();
		File libDir = new File(platformDir, "lib");
		File jacocoAgentJar = new File(libDir, "jacocoagent.jar");
		File jacocoExecFile = new File(platformDir,
			"test" + File.separatorChar +
			"testbase" + File.separatorChar +
			"target" + File.separatorChar +
			"jacoco-spark-" + getClass().getSimpleName() + "" + s_counter.incrementAndGet() + ".exec");*/

    	//add the extra java options
    	JSONObject body = driver.getBody();

/*		StringBuilder coverageOptions = new StringBuilder();
		coverageOptions.append("-javaagent:")
			.append(jacocoAgentJar.getAbsolutePath())
			.append("=destfile=")
			.append(jacocoExecFile.getAbsolutePath())
			.append(",includes=com.aw.*");*/

		//poll more frequently for testing
		body.put(DRIVER_BATCH_INTERVAL, "1");
        body.put(DRIVER_WORK_POLL_INTERVAL, "1");

		//body.put(DRIVER_EXTRA_JAVA_OPTIONS, coverageOptions.toString());

    	//update the driver configuration to include our test java options for code coverage
		TestDependencies.getDocMgr().get().getSysDocHandler().updateDocument(driver);

    }

    protected void stopRest() throws Exception {
    	nodeService.stop();

    	if ( restService != null && restService.isRunning() ) {
            restService.stop().get();
        }

    	restService = null;;
    }

    protected void startKafka() {
    	setThreadSystemAccess();
        startKafka(null);
    }

    protected void startKafka(String topic) {
        if ( kafkaService == null ) {
            kafkaService = KafkaZookeeperServiceWrapper.create(topic);
        }

        if ( !kafkaService.isRunning() ) {
            kafkaService.start();
        }
    }

    protected void stopKafka() {
        if ( kafkaService != null && kafkaService.isRunning() ) {
            kafkaService.stop();
        }
    }

    protected void startElasticSearch(String index) throws Exception {

    	logger.info("starting elasticsearch");

        if ( elasticSearchService == null ) {
            elasticSearchService = ElasticSearchServiceWrapper.create();
        }

        if ( !elasticSearchService.isRunning() ) {

        	Boolean isUp = elasticSearchService.start().get();
            if ( !isUp ) {
                Assert.fail("Failed to start elastic search.");
            }

        }

        //delete any existing indices here
        elasticSearchService.deleteIndex("*");

        if ( elasticSearchService.isRunning() && index != null ) {
            elasticSearchService.createIndex(index);
        }

    	logger.info("elasticsearch started");

    }

    protected boolean startHadoop() throws Exception {

    	setThreadSystemAccess();
    	if (hadoopService == null) {
    		hadoopService = new MiniHadoopServiceWrapper(9000);
    		return hadoopService.start();
    	} else {
    		return hadoopService.isRunning();
    	}

    }

    protected void stopHadoop() {

    	if (hadoopService != null) {
    		hadoopService.stop();
    		hadoopService = null;
    	}

    }

    protected void startSpark() throws Exception {

    	setThreadSystemAccess();

    	logger.info("starting spark");

    	//make sure spark home is set
    	System.setProperty(Setting.ARES_SPARK_HOME.name(), "./spark_test");

    	if ( sparkService == null ) {
            sparkService = SparkServiceWrapper.create();
        }

        if ( !sparkService.isRunning() ) {
            Boolean isUp = sparkService.start().get();
            if ( !isUp ) {
                Assert.fail("Failed to start spark.");
            } else {
            	logger.info("spark started");
            }
        }

    }

    protected void stopSpark() throws Exception {

    	if (sparkService != null) {

    		if (sparkService.isRunning()) {
    			sparkService.stop();
    		}

    		sparkService = null;

    	}

    }

    protected void startNodeService() throws Exception {

    	setThreadSystemAccess();
		String esPath = System.getProperty("es.path.home");

        nodeService = new NodeServiceWrapper(esPath + "/../node_service",  9100);
        nodeService.start();

    }

    protected void stopNodeService() throws Exception {

    	nodeService.stop();

    }

    protected void startElasticSearch() throws Exception {
    	setThreadSystemAccess();
        startElasticSearch(null);
    }

    protected void stopElasticsearch() throws Exception {
    	boolean stop = ( elasticSearchService != null && elasticSearchService.isRunning() );
    	logger.info("Stopping elasticsearch? " + stop);
        if (stop) {

	        // drop all indexes
	        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(10000).setConnectTimeout(3000).setSocketTimeout(30000).build();
	        HttpDelete httpDelete = new HttpDelete( elasticSearchService.queryBase() + "/*/");
	        httpDelete.setConfig(config);
	        HttpResponse response = httpClient.execute(httpDelete);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (!HttpStatusUtils.isSuccessful(statusCode)) {
            	throw new Exception("delete indexes failed: " + statusCode + " (" + IOUtils.toString(response.getEntity().getContent()) + ")");
            }

        	elasticSearchService.stop();
        }
    }

	protected void startPostgres() throws  Exception {
		logger.info("starting embedded posgresql");

		if ( postgresqlService == null ) {
			postgresqlService = new PostgresqlServiceWrapper();
		}

		if ( !postgresqlService.isRunning() ) {

			Boolean isUp = postgresqlService.start();
			if ( !isUp ) {
				Assert.fail("Failed to start postgresql search.");
			}

		}

		logger.info("postgresql started");
	}

	protected void stopPostgres() throws Exception {
		if (postgresqlService != null) {
			postgresqlService.stop();
			postgresqlService = null;
		}

	}
    protected void feedKafkaAsync(String testData) throws Exception {
        feedKafkaAsync(null, null, testData);
    }

    protected void feedKafkaAsync(String topic, String key, String testData) throws Exception {
        if ( kafkaService == null || !kafkaService.isRunning() ) {
            throw new Exception("Kafka is not started. Please start it first before trying to feed it test data.");
        }
        if ( topic == null && key == null ) {
            kafkaService.feedTopic(testData);
        } else {
            kafkaService.feedTopic(topic, key, testData);
        }
    }

    protected JSONArray getAllEs(String index) throws Exception {
        if ( elasticSearchService == null && !elasticSearchService.isRunning() ) {
            throw new Exception("Elastic search service is not started. Please start it first before trying to get data from it.");
        }

        return elasticSearchService.dump(index);
    }


    public static void provisionTenant(String tenantID) throws Exception {

        final ClientConfig config = new ClientConfig();
        final Client client = ClientBuilder.newClient(config);
        final WebTarget service = client.target(UriBuilder.fromUri("http://localhost:9099").build());

        final Tenant newTenant = new Tenant(tenantID);

		final Invocation.Builder requestBuilder = service.path(Statics.VERSIONED_REST_PREFIX + "/ares/tenants").request(MediaType.APPLICATION_JSON);
		String serviceSharedSecret = EnvironmentSettings.getServiceSharedSecret();
        if ( serviceSharedSecret != null ) {
			requestBuilder.header("serviceSharedSecret", serviceSharedSecret);
        }
        String postData = newTenant.getAsJSON().toString();

        try {
            requestBuilder.post(Entity.entity(postData, MediaType.APPLICATION_JSON_TYPE), String.class);
        } catch (WebApplicationException e) {
     	   if (System.getProperty(BaseIntegrationTest.SHOW_CHILD_LOGS) != null) {
     		   e.printStackTrace();
     		   System.err.println("entity: " + IOUtils.toString(((InputStream)e.getResponse().getEntity())));
     	   }
        	throw e;
        }

    }

    public static void unProvisionTenant(String tenantID) throws Exception {

        final ClientConfig config = new ClientConfig();
        final Client client = ClientBuilder.newClient(config);
        final WebTarget service = client.target(UriBuilder.fromUri("http://localhost:9099").build());

		final Invocation.Builder requestBuilder = service.path(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/aw/tenants/" + tenantID).request(MediaType.APPLICATION_JSON);
        String serviceSharedSecret = EnvironmentSettings.getServiceSharedSecret();
        if ( serviceSharedSecret != null ) {
            requestBuilder.header("serviceSharedSecret", serviceSharedSecret);
        }

        requestBuilder.delete(String.class);

    }


    protected String addUser(String user, String tenantID) throws Exception {
        return addUser(user, user, tenantID);
    }

    protected String addUser(String user, String password, String tenantID) throws Exception {
        // add header
        JSONObject login = new JSONObject();
        login.put("tenantKey", tenantID);
        login.put("userName", user);
        login.put("password", password);
        login.put("domainId", tenantID);

        String path = com.aw.util.Statics.VERSIONED_REST_PREFIX + "/Account/Login";
        HttpResponse post = restService.post(path, login, null, null);
        JSONObject entity = getJSONEntity(HttpMethod.POST, path, post, JSONObject.class);
        if (entity == null) {
            throw new Exception("Could not add user " + user + ": " + post.toString());
        }
        String accessToken = (String) entity.get("accessToken");

        userTokenMap.put(user, accessToken);
        return accessToken;
    }

    protected <T> T authPost( String user, String path, Object content, Class<T> returnType) throws Exception {
        String userToken = getUserToken(user);

        HttpResponse post = reportingService().post(path, content, userToken, null);
        return getJSONEntity(HttpMethod.POST, path, post, returnType);
    }

    protected <T> T authDelete( String user, String path, Class<T> returnType) throws Exception {
        String userToken = getUserToken(user);

        HttpResponse post = reportingService().delete(path, userToken, null);
        return getJSONEntity(HttpMethod.DELETE, path, post, returnType);
    }

    protected <T> T authPut( String user, String path, Object content, Class<T> returnType) throws Exception {
        String userToken = getUserToken(user);

        HttpResponse put = reportingService().put(path, content, userToken, null);
        return getJSONEntity(HttpMethod.PUT, path, put, returnType);
    }

    protected <T> T authPatch( String user, String path, JSONObject content, Class<T> returnType) throws Exception {
        String userToken = getUserToken(user);

        HttpResponse put = reportingService().patch(path, content, userToken, null);
        return getJSONEntity(HttpMethod.PATCH, path, put, returnType);
    }

    protected <T> T authGet( String user, String path, Class<T> returnType) throws Exception {
        String userToken = getUserToken(user);
        HttpResponse get = reportingService().get(path, userToken, null);
        return getJSONEntity(HttpMethod.GET, path, get, returnType);
    }
    protected <T> T unAuthPost( String path, JSONObject content, Class<T> returnType) throws Exception {
        HttpResponse post = reportingService().post(path, content, null, null);
        return getJSONEntity(HttpMethod.POST, path, post, returnType);
    }

    protected <T> T unAuthPut( String path, JSONObject content, Class<T> returnType) throws Exception {
        HttpResponse put = reportingService().put(path, content, null, null);
        return getJSONEntity(HttpMethod.PUT, path, put, returnType);
    }

    protected <T> T unAuthPatch(String path, JSONObject content, Class<T> returnType) throws Exception {
        HttpResponse put = reportingService().patch(path, content, null, null);
        return getJSONEntity(HttpMethod.PATCH, path, put, returnType);
    }

    protected <T> T unAuthGet( String path, Class<T> returnType) throws Exception {
        HttpResponse get = reportingService().get(path, null, null);
        return getJSONEntity(HttpMethod.GET, path, get, returnType);
    }

    protected String getUserToken(String user) throws Exception {
        String userToken = userTokenMap.get(user);
        if (userToken == null ) {
            throw new Exception("User " + user + " not logged in, log them in first.");
        }
        return userToken;
    }

    protected ReportingServiceWrapper reportingService() throws Exception {
        if ( restService == null || !restService.isRunning() ) {
            throw new Exception("Reporting service not running. Please start it first.");
        }

        return restService;
    }

    /**
     * Returns a JSONObject or JSONArray depending on the data from the server, a string if
     * it's not valid json of any kind, or the response object if there was an error.
     *
     * @param method The HTTP method - used for meaningful error messages
     * @param path The context path for the rest call - used for meaningful error messages
     * @param response The raw response
     * @return The appropriate type of object as described in the method docs
     */
    protected <T> T getJSONEntity( HttpMethod method, String path,  HttpResponse response, Class<T> returnType ) throws Exception {

    	//don't touch the response object if it's needed by the caller
    	if (returnType == HttpResponse.class) {
    		return (T)response;
    	}

    	//get response string
        String str = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
    	Object ret = response;

    	//check invalid request
    	if (response.getStatusLine().getStatusCode() > 299) {

    		if (!returnType.isAssignableFrom(response.getClass())) {
        		throw new RuntimeException(method + " " + path + ": Unexpected http response: " +
        				toString(response.getStatusLine()) + " " +
                        "(content=" + str + ")");
    		}

    		return (T)response;

    	}

    	else if (returnType.isAssignableFrom(JSONObject.class)) {

    		//try a json object
            ret = new JSONObject(str);

    	}

    	else if (returnType.isAssignableFrom(JSONArray.class)) {

    		//try an array
			ret = new JSONArray(str);

    	}

    	else if (returnType == String.class) {

    		//basic string
    		ret = str;

    	}

    	//not an error but not what we expected
    	else if (!returnType.isAssignableFrom(ret.getClass())) {
    		throw new RuntimeException(method + " " + path + ": Unexpected return data, expected " + returnType.getSimpleName() + ", but got: " + ret.getClass() + " status=" + toString(response.getStatusLine()));
    	}

    	//else we can cast it safely so do that here
		return (T)ret;

    }

    //i don't like their toString
    private String toString(StatusLine status) {
		final Response.Status responseStatus = Response.Status.fromStatusCode(status.getStatusCode());
		return status.getStatusCode() + " " + responseStatus.getReasonPhrase();
    }

	protected void createIndex(String tenantID, ESKnownIndices index, String mappings) throws Exception {

		//don't analyzer image name
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //set up the index mapping to set types properly
        HttpPost post = new HttpPost(elasticSearchService.queryBase() + "/" + index.buildIndexFor(Tenant.forId(tenantID), Instant.now()));
        post.addHeader("content-type", "application/json");
        post.setEntity(new StringEntity(mappings));
        HttpResponse response = httpClient.execute(post);

    }

	/**
     * Bulk insert of multiple json lines - the json list passed in must be multiple full json object documents, no newlines
     *
     * Fields available: un (username), in (image name i.e. filename), esu (date)
     * @param tenantID
     * @param jsonList
     * @throws Exception
     */
    protected void populateEventDataBulk(String tenantID, String dataType, List<String> jsonList) throws Exception {

    	//add dg_guids
    	for (int x=0; x<jsonList.size(); x++) {
    		String json = jsonList.get(x);

    		JSONObject jsonObject = new JSONObject(json);
    		Data data = TestDependencies.getUnity().get().newData(json);
    		if (data.getGuid() == null) {
    			UUID guid = UUID.randomUUID();
    			jsonObject.put(((JSONDataType)data.getType()).getPath(data.getType().getIDField())[0], guid);
    		}

    		//update with the guid
    		jsonList.set(x, jsonObject.toString());
    	}

		//don't analyzer image name
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //populate the database
        GenericESProcessor processor = Mockito.spy(new GenericESProcessor());
        processor.init(new StreamDef("index_name", ESKnownIndices.EVENTS_ES.name().toLowerCase()));

        //wire test dependencies for this processor
        Mockito.doReturn(TestDependencies.getPlatform().get()).when(processor).getDependency(Platform.class);
        Mockito.doReturn(TestDependencies.getUnity().get()).when(processor).getDependency(UnityInstance.class);

        //bulk insert the data
        processor.process("1", jsonList);

        //it can take a second to see the results
        int tries = 0;

        //quit after 5 tries
        while (tries < 5) {

            //HttpGet get = new HttpGet("http://127.0.0.1:9200/" + tenantID + "_dlp/_search?q=jlehmann");
            HttpGet get = new HttpGet(elasticSearchService.queryBase() + "/"+ ESKnownIndices.EVENTS_ES.buildIndexFor(Tenant.forId(tenantID), Instant.now())+"/_stats");
            HttpResponse response = httpClient.execute(get);
            JSONObject results = new JSONObject(IOUtils.toString(response.getEntity().getContent()));

            //wait until we have something
            if (results.getJSONObject("_all").getJSONObject("primaries").getJSONObject("docs").getInt("count") >= jsonList.size()) {
            	break;
            }

            Thread.sleep(1000L);

            //keep track of how many times we've had to do this
            tries++;

        }

        if (tries == 5) {
        	throw new Exception("test data not visible in elasticsearch after 5 seconds - aborting");
        }

    }

    protected void populateMachineDLPIndexes(String tenantID) throws Exception {
        for ( int i = 0; i < 5; i++ ) {
            createMachineDLPDatapoint(tenantID, "chrome.exe", "aron");
            createMachineDLPDatapoint(tenantID, "chrome.exe", "allen");
            createMachineDLPDatapoint(tenantID, "firefox.exe", "aron");
        }

        // wait for things to index
        Thread.sleep(3000);
    }

    protected String getConfDirectory() {
        // not set; assume its root

		return EnvironmentSettings.getConfDirectory();

       // File rooDir = getRootDir();
        //return rooDir.getAbsolutePath() + File.separatorChar + "conf";
    }

	protected String getDataDirectory() {
		// not set; assume its root
		File rooDir = getRootDir();
		return rooDir.getAbsolutePath() + File.separatorChar + "data";
	}

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private
    protected File testDir;
    protected ReportingServiceWrapper restService;
    protected KafkaZookeeperServiceWrapper kafkaService;
    protected ElasticSearchServiceWrapper elasticSearchService;
    protected SparkServiceWrapper sparkService;
	protected PostgresqlServiceWrapper postgresqlService;
    protected MiniHadoopServiceWrapper hadoopService;
    protected NodeServiceWrapper nodeService;
    protected Map<String, String> userTokenMap = new HashMap<>();

    //counter for generating a unique coverage file name
    private static AtomicInteger s_counter = new AtomicInteger(0);

    private void createMachineDLPDatapoint(String tenantID, String processName, String username) throws Exception {

        JSONObject document = new JSONObject();
        document.put("in", processName);
        document.put("event_count", 1);
        document.put("un", username);

        createMachineDLPDatapoint(tenantID, document.toString());

    }

    private void createMachineDLPDatapoint(String tenantID, String json) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(elasticSearchService.queryBase() + "/" + tenantID + "_dlp/machine_event/");

        StringEntity params = new StringEntity(json);
        post.addHeader("content-type", "application/json");
        post.setEntity(params);

        CloseableHttpResponse execute = httpClient.execute(post);
        if ( execute.getStatusLine().getStatusCode() != 201 ) {
            throw new RuntimeException("Failed to create datapoint: " + execute.getStatusLine().getStatusCode());
        }
    }

	protected int getRestPort() {
		return this.PORT;
	}
}
