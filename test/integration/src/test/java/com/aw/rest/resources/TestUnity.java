package com.aw.rest.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

// xxx todo - this might be a unit test
public class TestUnity {

    public static String port = "8080";

	public static void main(String[] args) throws Exception {


        //




        //do login and get token

        HttpResponse response = doLogin("849370b1-e97e-4988-a4c1-d1ef44a3e742",
                "8971c55d-29f5-4a33-a22c-5edebbaf0abc",  "dude", "test", UUID.randomUUID().toString());

        JSONObject entity = new JSONObject(IOUtils.toString(new InputStreamReader(response.getEntity().getContent())));
        String accessToken = (String) entity.get("accessToken");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet uri = get(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/reporting/trays");
        uri.addHeader("accessToken", accessToken);

        response = httpClient.execute(uri);
        int statusCode = response.getStatusLine().getStatusCode();



        //now run a query as this user
		//String s = rest.getString("/metadata/tables");
		//System.out.println(s);

		//JSONObject query = AWFileUtils.getFileToJSONObject("/Users/scott/dev/src/dg2/dg2/conf/defaults/content/def_query",
		//		"EVENT_DETAIL.json");

       // JSONObject doc = (JSONObject) query.get(DocAttribs.DOC);


        //TODO: create actual Unity functional test
        JSONObject doc = new JSONObject();

        JSONArray parmarray = new JSONArray();

        JSONObject q = new JSONObject();
        q.put("pname" , "query");
        q.put("pvalue", doc.toString());
        parmarray.put(q);

        JSONObject pc = new JSONObject();
        pc.put("parms", parmarray);


		//String s2 = rest.post("/DataSetJSON", pc.toJSONString());


        httpClient = HttpClientBuilder.create().build();
        HttpPost p = post(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/Unity/DataSetJSON", pc);
        p.addHeader("accessToken", accessToken);

        response = httpClient.execute(p);
        statusCode = response.getStatusLine().getStatusCode();


		InputStream ins = response.getEntity().getContent();

        JSONObject o = new JSONObject(IOUtils.toString(new InputStreamReader(ins)));

        //System.out.println(o.toJSONString());
       // AWFileUtils.writeJSONToFile(o, new File("/Users/scott/dev/src/dg2/dg2/tmp/dataset.json"));

	}



    public static HttpResponse doLogin(String tenantID, String userID, String userName, String password, String domainID) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost post = post(com.aw.util.Statics.VERSIONED_REST_PREFIX + "/Account/Login");

        // add header
        JSONObject login = new JSONObject();
        login.put("tenantKey", tenantID);
        login.put("userName", userName);
        login.put("userID", userID);
        login.put("password", password);
        login.put("domainId", domainID);

        StringEntity params = new StringEntity(login.toString());
        post.addHeader("content-type", "application/json");
        post.setEntity(params);

        HttpResponse response = httpClient.execute(post);
        return response;
    }

    public static HttpGet get(String URL) {
        return new HttpGet("http://localhost:8080"  + URL);
    }


    public static HttpPost post(String URL) {
        return new HttpPost("http://localhost:" + port + URL);
    }


    public static HttpPost post(String URL, JSONObject doc) throws Exception {
        HttpPost httpPut = new HttpPost("http://localhost:" + port + URL);
        httpPut.addHeader("Content-type", "application/json");
        httpPut.setEntity(new StringEntity(doc.toString()));
        return httpPut;
    }

}
