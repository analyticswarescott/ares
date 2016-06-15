package com.aw.compute.streams.processor;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.messaging.Topic;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.util.JSONUtils;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.StringTupleProcessor;
import com.aw.platform.PlatformMgr;

/**
 * Process files from HDFS
 *
 *
 *
 */
public interface HDFSFileProcessor extends StringTupleProcessor, Dependent {

	static final Logger logger = Logger.getLogger(HDFSFileProcessor.class);

	/**
	 * Process strings, by default this turns them into FileInputMetadata and calls processFile(FileInputMetadata)
	 */
	@Override
	default public void process(String string) throws StreamProcessingException {

		FileInputMetadata metadata = null;
		FileWrapper file = null;
		try {

			//parse the file info
	 		metadata = JSONUtils.objectFromString(string, FileInputMetadata.class);

	 		//combine the parts if it is in parts
 			if (metadata.getHasParts()) {
 				combineParts(getDependency(PlatformMgr.class), metadata);
 			}

 			//read the file
 			FileReader reader = getDependency(PlatformMgr.class).getTenantFileReader();
 			file = reader.read(metadata.getPurpose(), new Path(metadata.getPath()), metadata.getFilename());

 			processFile(metadata, file.getInputStream());

 			//add processed file to output topic, if set
 			getDependency(PlatformMgr.class).newClient().logMessage("sending to dest topic: " + getDestTopic());
 			if (getDestTopic() != null) {
 				getDependency(PlatformMgr.class).sendMessage(getDestTopic(), metadata);
 			}

 			//when completed, if there are parts, delete the parts at this point
 			if (metadata.getHasParts()) {
 				deleteParts(getDependency(PlatformMgr.class), metadata);
 			}

		} catch (Exception e) {

			//provide as much information as possible
			if (metadata != null) {
				throw new StreamProcessingException("while processing " + metadata.getPurpose() + " file,  name=" + metadata.getFilename(), e);
			}

			//if input is null, we couldn't parse the file metadata
			else {
				throw new StreamProcessingException("while processing file", e);
			}

		} finally {

			//close the input stream when we're done
			try {

				if (file != null && file.getInputStream() != null) {
					file.getInputStream().close();
				}

			} catch (Exception e) {
				throw new StreamProcessingException("error closing input stream from hdfs", e);
			}

		}

	}

	/**
	 * Process the file
	 *
	 * @param metadata The information on the file
	 * @throws Exception If anything goes wrong
	 */
	public void processFile(FileInputMetadata metadata, InputStream in) throws Exception;

	/**
	 * @param metadata
	 * @throws Exception
	 */
	public static void combineParts(PlatformMgr platformMgr, FileInputMetadata metadata) throws Exception {

		//we need the reader and the writer
		FileReader reader = platformMgr.getTenantFileReader();
		FileWriter writer = platformMgr.getTenantFileWriter();

		//the path of the complete file
		Path path = new Path(metadata.getPath());

		//get our output stream for the resulting file
		OutputStream out = writer.getOutputStream(metadata.getPurpose(), path, metadata.getFilename());

		//keep processing parts
		for (int x=0; ; x++) {

			String partFilename = FileWriter.toPartName(metadata.getFilename(), x);

			if (reader.exists(metadata.getPurpose(), path, partFilename)) {

				//get the input stream for the part
				FileWrapper wrapper = reader.read(metadata.getPurpose(), path, partFilename);

				//copy to output
				IOUtils.copy(wrapper.getInputStream(), out);

			}

			//stop the first time we don't have a part
			else {
				break;
			}

		}

		out.flush();
		out.close();

	}

	/**
	 * @return the topic to which files are written when they are done processing, or null if no output - this is null by default
	 */
	default public Topic getDestTopic() { return null; }

	/**
	 * Delete the parts after a file is successfully processed
	 *
	 * @param metaata
	 * @throws Exception
	 */
	public static void deleteParts(PlatformMgr platformMgr, FileInputMetadata metadata) throws Exception {

		//only delete parts if there are parts
		if (!metadata.getHasParts()) {
			return;
		}

		//get the writer which we will use for deletes
		FileWriter writer = platformMgr.getTenantFileWriter();

		//delete all of the files matching the prefix
		int partsDeleted = writer.deleteAll(metadata.getPurpose(), new Path(metadata.getPath()), FileWriter.toPartPrefix(metadata.getFilename()));

		logger.info("deleted " + partsDeleted + " parts of " + metadata.getFilename());

	}


}
