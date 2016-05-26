package com.aw.compute.streams.processor;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.system.FileInputMetadata;
import com.aw.util.Statics;

/**
 * Process bundles stored in HDFS. The Kafka message will point back to the the location in HDFS where
 * the bundle is stored.
 *
 *
 *
 */
public class HDFSJSONBundleProcessor extends JSONBundleProcessor implements HDFSFileProcessor {

	public static Logger logger = Logger.getLogger(HDFSJSONBundleProcessor.class);

	private static final long serialVersionUID = 1L;

	@Override
	public void processFile(FileInputMetadata metadata, InputStream in) throws Exception {

		//read the bundle from HDFS
		String strBundle = IOUtils.toString(in, Statics.CHARSET);

		processBundle(new JSONObject(strBundle));

	}

}