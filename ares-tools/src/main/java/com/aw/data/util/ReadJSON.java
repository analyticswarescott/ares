package com.aw.data.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;

public class ReadJSON {

    public static void main(String s[]) throws Exception {
        String path = System.getProperty("json.path");
        String operation = System.getProperty("operation");
        String delimiter = System.getProperty("delimiter");
        if (!StringUtils.isEmpty(delimiter) ) {
            JSONUtils.JSONElementDelimiter = delimiter;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        StringBuilder all = new StringBuilder();
        while ((line = in.readLine()) != null && line.length() != 0) {
            all.append(line);
        }

        try {
            JSONObject parsed = new JSONObject(all.toString());
            Object leafValue = JSONUtils.getLeafValue(parsed, path);

            if ( "keys".equals(operation) ) {
                if ( leafValue instanceof JSONObject ) {
                    Iterator keys = ((JSONObject) leafValue).keys();
                    List<String> keyList = new ArrayList<>();
                    while (keys.hasNext()) {
                        keyList.add((String) keys.next());
                    }
                    System.out.println( keyList.stream().collect(Collectors.joining(" ")) );
                }
            } else {
                // default to dumping the leafvalue
                System.out.println(leafValue);
            }

        } catch ( Exception e) {
            // silent fail
        }
    }

}
