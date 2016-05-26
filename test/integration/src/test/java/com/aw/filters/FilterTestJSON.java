package com.aw.filters;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.util.AWFileUtils;
import com.aw.unity.Data;
import com.aw.unity.query.Filter;

public class FilterTestJSON extends BaseIntegrationTest {

    String filterDir;

	@Before
	public void setUp() throws Exception {
        filterDir = this.getClass().getClassLoader().getResource("com.aw/filters").getFile();
    }

    @Override
    protected boolean usesElasticsearch() {
    	return false;
    }

    @Test
    public void validateFilterLogic() throws Exception {

        JSONArray testMatrix = AWFileUtils.getFileToJSONArray(filterDir, "test_matrix.json");

        for (int x=0; x<testMatrix.length(); x++) {
        	Object test = testMatrix.get(x);
            JSONObject testJ = (JSONObject) test;
            doFilter(testJ.get("filter_file").toString(), testJ.get("data_file").toString(), (JSONArray) testJ.get("expected_results"));

        }

    }


    private void doFilter(String filterFile, String dataFile, JSONArray expectedResults) throws Exception {

        JSONObject filt = AWFileUtils.getFileToJSONObject(filterDir, filterFile);
        JSONArray data = AWFileUtils.getFileToJSONArray(filterDir, dataFile);

        //TODO: need pure unit test of filters? This test now requires security
        setThreadSystemAccess();

        //string to force compile errors downstream
        Filter filter = TestDependencies.getUnity().get().newFilter(filt.toString());

        System.out.println("Canonical Filter:  " + filter.printCanonical());

        JSONArray actualResults = new JSONArray();
        for (int x=0; x<data.length(); x++) {
        	Object row = data.get(x);
            JSONObject rowJ = (JSONObject) row;
            Data curData = TestDependencies.getUnity().get().newData(rowJ.toString());
            boolean b =  filter.match(curData);
            System.out.println(rowJ.toString());
            System.out.println("Match:  " + b);

            actualResults.put(b);
        }



        Assert.assertEquals("Expected results", expectedResults.toString(), actualResults.toString());

    }
}
