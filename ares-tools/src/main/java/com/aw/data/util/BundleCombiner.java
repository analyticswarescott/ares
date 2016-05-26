package com.aw.data.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.AWFileUtils;
import com.aw.common.util.JSONUtils;

public class BundleCombiner {

    private File inputDir;
    private File output;

    public BundleCombiner( File inputDir, File output) {

        this.inputDir = inputDir;
        this.output = output;
    }

    public void combine() throws Exception {
        JSONObject output = null;

        String primaryBid = "";
        List<String> bidsToReplace = new ArrayList<String>();

        for ( File inputFile : inputDir.listFiles() ) {
            JSONObject inputFileJSON = AWFileUtils.getFileToJSONObject(inputFile);
            if ( output == null ) {
                output = inputFileJSON;
                String bid = primaryBid = (String) inputFileJSON.get("bid");
            } else {
                JSONArray pi = (JSONArray) inputFileJSON.get("pi");
                {
                    JSONArray input = (JSONArray) inputFileJSON.get("pi");
                    JSONUtils.putAll(pi, input);
                }

                JSONArray mp = (JSONArray) inputFileJSON.get("mp");
                {
                    JSONArray input = (JSONArray) inputFileJSON.get("mp");
                    JSONUtils.putAll(mp, input);
                }

                JSONArray di = (JSONArray) inputFileJSON.get("di");
                {
                    JSONArray input = (JSONArray) inputFileJSON.get("di");
                    JSONUtils.putAll(di, input);
                }

                JSONArray ua = (JSONArray) inputFileJSON.get("ua");
                {
                    JSONArray input = (JSONArray) inputFileJSON.get("ua");
                    JSONUtils.putAll(ua, input);
                }

                JSONArray uad = (JSONArray) inputFileJSON.get("uad");
                {
                    JSONArray input = (JSONArray) inputFileJSON.get("uad");
                    JSONUtils.putAll(uad,  input);
                }
                bidsToReplace.add( (String) inputFileJSON.get("bid"));
            }
        }

        String outputJsonStr = output.toString();
        for ( String bid : bidsToReplace ) {
            outputJsonStr = outputJsonStr.replaceAll(bid, primaryBid);
        }

        output = new JSONObject(outputJsonStr);
        AWFileUtils.writeJSONToFile(output, this.output);
    }

    public static void main(String s[]) throws Exception {
        String sourceFolder = s[0];
        String targetFile = s[1];

        BundleCombiner bc = new BundleCombiner(new File(sourceFolder), new File(targetFile));
        bc.combine();
    }
}
