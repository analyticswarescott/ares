package com.aw;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchServiceWrapper {

	public static final String ES_HOME = "es.path.home";

    public static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceWrapper.class);

    private final File homeDir;
    public final int port;
    private boolean closed;
    private boolean running;
    private static ElasticSearchServiceWrapper wrapper;

    private ElasticSearchServiceWrapper(File homeDir, int port) {
        this.homeDir = homeDir;
        this.port = port;
        closed = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {

        try {

            //don't continue until elasticsearch is really dead
            while (m_elasticProcess.isAlive()) {

            	m_elasticProcess.destroyForcibly();
            	Thread.sleep(100L);

            }

            this.closed = true;
            running = false;
            this.homeDir.delete();

        } catch (Exception e) {
        	throw new RuntimeException("Error closing elasticsearch", e);
        }

    }

    public Future<Boolean> start() {

        new Thread(() -> {
            running = true;

            //we must have the elasticsearch directory created as part of the test environment
            Object esHome = System.getProperties().get("es.path.home");
            if (esHome == null) {
            	throw new RuntimeException("No elasticsearch home set (es.path.home)");
            }

            try {

            	ProcessBuilder builder = new ProcessBuilder("java", "-Des.path.home=" + esHome, "-Des.security.manager.enabled=false", "-cp", getClasspath(), "org.elasticsearch.bootstrap.Elasticsearch", "start");
            	if (System.getProperty(BaseIntegrationTest.SHOW_CHILD_LOGS) != null) {
            		builder = builder.inheritIO();
            	} else {
            		File log = File.createTempFile("es_out", ".log");
            		log.deleteOnExit(); //delete it when we exit
            		builder.redirectOutput(Redirect.to(log));
            	}
            	m_elasticProcess = builder.start();

            } catch (Exception e) {
            	throw new RuntimeException(e);
            }

        }).start();

        CompletableFuture<Boolean> callback = isElasticSearchUp();

        return callback;
    }

    private String getClasspath() {

		String esPath = System.getProperty("es.path.home");


    	File[] files = new File(esPath + "/lib").listFiles();
    	StringBuilder paths = new StringBuilder();
    	for (File f : files) {
    		if (paths.length() > 0) {
    			paths.append(File.pathSeparatorChar);
    		}
    		paths.append(f.getAbsolutePath());
    	}
    	return paths.toString();
    }

    public CompletableFuture<Boolean> isElasticSearchUp() {
        return isElasticSearchUp(30 * 1000); // 30 seconds
    }

    public static CompletableFuture<Boolean> isElasticSearchUp(long timeout) {
        CompletableFuture<Boolean> callback = new CompletableFuture<>();
        new Thread(() -> {
            // poll
            long now = System.currentTimeMillis();
            boolean isUp = false;

            while (!isUp && System.currentTimeMillis() - now < timeout) {

                HttpGet httpGet = new HttpGet("http://localhost:9200/_cat/indices?v");
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                try {
                    CloseableHttpResponse response = httpClient.execute(httpGet);
                    isUp = response.getStatusLine().getStatusCode() == 200;
                    if ( isUp ) {
                        break;
                    }
                } catch (IOException e) {
                    //
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { }
            }

            callback.complete(isUp);
        }).start();
        return callback;
    }


    public static ElasticSearchServiceWrapper create()  {
        File homeDir = null;
        try {
            homeDir = File.createTempFile("elasticsearch", "elasticsearch");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if ( wrapper != null ) {
            if ( !wrapper.closed ) {
                return wrapper;
            }
        }
        if ( homeDir.exists() ) {
            homeDir.delete();
        }

        homeDir.mkdir();
        wrapper = new ElasticSearchServiceWrapper(homeDir, 9200);
        return wrapper;
    }

    public String queryBase() {
        return "http://localhost:" + this.port;
    }

    public JSONArray dump() throws Exception {
        return dump("");
    }

    public JSONArray dump(String path) throws Exception {
        if ( !path.startsWith("/") ) {
            path = "/" + path;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(queryBase() + path + "/_search/?size=1000000000&pretty=1");

        CloseableHttpResponse response = httpClient.execute(get);
        JSONObject jsonEntity = (JSONObject) getJSONEntity(response);
        if ( jsonEntity == null ) {
            return null;
        }

        JSONObject hits = (JSONObject) jsonEntity.get("hits");

        return (JSONArray) hits.get("hits");
    }

    public void createIndex( String index ) throws Exception {
        if ( !index.startsWith("/") ) {
            index = "/" + index;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(queryBase() + index );
        CloseableHttpResponse response = httpClient.execute(put);
        if ( response.getStatusLine().getStatusCode() != 200 ) {
            throw new RuntimeException("Failed to create index at " + index + ", got "
                    + response.getStatusLine().getStatusCode());
        }
    }

    public void deleteIndex( String index ) throws Exception {
        if ( !index.startsWith("/") ) {
            index = "/" + index;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete delete = new HttpDelete(queryBase() + index );
        CloseableHttpResponse response = httpClient.execute(delete);
        if ( response.getStatusLine().getStatusCode() != 200 ) {
            throw new RuntimeException("Failed to delete index at " + index + ", got "
                    + response.getStatusLine().getStatusCode());
        }
    }

    public boolean indexExists( String path ) throws Exception {
        if ( !path.startsWith("/") ) {
            path = "/" + path;
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(queryBase() + path);

        CloseableHttpResponse response = httpClient.execute(get);
        return response.getStatusLine().getStatusCode() == 200;
    }

    public JSONObject load(String path, JSONObject document) throws Exception {
        if ( !path.startsWith("/") ) {
            path = "/" + path;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(queryBase() + path);

        StringEntity params = new StringEntity(document.toString());
        post.addHeader("content-type", "application/json");
        post.setEntity(params);

        CloseableHttpResponse response = httpClient.execute(post);
        if ( !(response.getStatusLine().getStatusCode() >= 200 &&  response.getStatusLine().getStatusCode() < 300) ) {
            throw new RuntimeException("Failed to upload document to " + path + ", got "
                + response.getStatusLine().getStatusCode());
        }

        // wait for few seconds to allow for indexing
        Thread.sleep(2000);

        return (JSONObject) getJSONEntity(response);
    }

    private Object getJSONEntity( HttpResponse response ) {
        try {
            String str = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return new JSONObject(str);
        } catch (Exception e) {
            return null;
        }
    }

    // todo - write a test for this
//    public static void main(String s[]) throws Exception {
//        ElasticSearchServiceWrapper elasticSearchServiceWrapper = ElasticSearchServiceWrapper.create();
//        Future<Boolean> start = elasticSearchServiceWrapper.start();
//        start.get();
//
//        System.out.println(elasticSearchServiceWrapper.indexExists("tenant0"));
//        System.out.println(elasticSearchServiceWrapper.indexExists("tenant0"));
//        elasticSearchServiceWrapper.createIndex("tenant0");
//        System.out.println(elasticSearchServiceWrapper.indexExists("tenant0"));
//
//        {
//            JSONObject object = new JSONObject();
//            object.put("name", "aron");
//            object.put("age", "39");
//            elasticSearchServiceWrapper.load("tenant0/friends", object );
//        }
//        {
//            JSONObject object = new JSONObject();
//            object.put("name", "aron2");
//            object.put("age", "39");
//            elasticSearchServiceWrapper.load("tenant0/friends", object );
//        }
//
//        JSONArray dump = elasticSearchServiceWrapper.dump("tenant0/friends");
//        System.out.println(dump.toString());
//
//        elasticSearchServiceWrapper.stop();
//    }


    Process m_elasticProcess = null;

}
