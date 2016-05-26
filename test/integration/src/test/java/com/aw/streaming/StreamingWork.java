package com.aw.streaming;

import org.codehaus.jettison.json.JSONObject;

import com.aw.BaseIntegrationTest;
import com.aw.platform.Platform;
import com.aw.platform.restcluster.LocalRestMember;

public class StreamingWork {


    public static boolean launchGlobalEvents(Platform platform, LocalRestMember member) throws  Exception {


		int numRetries = 0;
        while (numRetries < 30) {
            if (member.getController().isProcessorRegistered("global_Events")){break;}
			numRetries++;
            Thread.sleep(1000);
        }

        System.out.println("Processor registration complete.  Re-starting REST ");




        //state should be re-established, so continue

        //add bundles to tenant bundle topic
        DataFeedUtils.fireGlobalEvents(platform, "1", "test_event1.json");


        //read it ourselves to be sure we insert everything
        JSONObject bundles = new JSONObject(DataFeedUtils.readFile("bundle_1.json"));
        JSONObject bundles2 = new JSONObject(DataFeedUtils.readFile("bundle_2.json"));

        System.out.println(" wait for indexes to populate....  ");
        //wait for ES records to show up -- this should also test ES provisioning since index should exist

        return true;

    }

/*    public static boolean awaitDLPResult() throws Exception{

        boolean b1 = DataFeedUtils.awaitESResultBoolean("1_dlp", "user_cd_burn", 44, 60);
        boolean b2 = DataFeedUtils.awaitESResultBoolean("1_dlp", "user_net_transfer_download", 23, 60);

        boolean b3 = DataFeedUtils.awaitESResultBoolean("2_dlp", "user_cd_burn", 44, 60);
        boolean b4 = DataFeedUtils.awaitESResultBoolean("2_dlp", "user_net_transfer_download", 23, 60);

        if (b1 && b2 && b3 && b4) {
            return true;
        }
        return false;
    }*/


}
