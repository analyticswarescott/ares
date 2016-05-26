package com.aw.common.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: rename this class to DGFileUtils
public class AWFileUtils {
    public static final Logger logger = LoggerFactory.getLogger(AWFileUtils.class);

    public static JSONArray getFileToJSONArray(String dir, String filename) throws Exception {
        //
        File f = new File(dir + File.separatorChar + filename);
        String s = FileUtils.readFileToString(f);
        JSONArray a = new JSONArray(s);

        return a;
    }

    public static JSONObject getFileToJSONObject(File f) throws Exception {
        JSONObject o;

        try {
            String s = FileUtils.readFileToString(f);
            o = new JSONObject(s);
        } catch (FileNotFoundException fnf) {
            //TODO: better solution for file not found

            logger.warn(fnf.getLocalizedMessage());
            return null;
        } catch (Exception e) {
        	throw new Exception("Error processing json file " + f, e);
        }

        return o;
    }

    public static JSONObject getFileToJSONObject(String dir, String filename) throws Exception {
        File f = new File(dir + File.separatorChar + filename);
        return getFileToJSONObject(f);
    }

    public static void writeJSONToFile(JSONObject json, File file) throws Exception {
        FileUtils.writeStringToFile(file, json.toString());
    }

}
