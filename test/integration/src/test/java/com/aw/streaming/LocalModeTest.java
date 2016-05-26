package com.aw.streaming;

import org.junit.Test;

//TODO: scott, can we remove this?
public class LocalModeTest extends StreamingIntegrationTest {

    public void testProvisionAndProcessDebug() throws Exception {


        //TODO: this is just to avoid having to run a failed test after killing a test -- improve
        try {
            elasticSearchService.deleteIndex("1_dlp");
        }
        catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }

        System.out.println("I am here to provide everything BUT spark so it can be debugged in local mode");
        System.out.println("Waiting for you to start all enabled drivers in local debug mode. ");

        //doTest();


    }




}
