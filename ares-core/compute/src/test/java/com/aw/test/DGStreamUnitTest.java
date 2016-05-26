package com.aw.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.aw.util.Statics;

/**
 * Base class for testing stream processor related classes.
 *
 *
 *
 */
public class DGStreamUnitTest {

	/**
	 * @return The contents of the test file for this class, in UTF-8 format
	 */
	protected String getFileAsString(String filename) throws IOException {
		return getFileAsString(filename, Statics.UTF_8);
	}

	/**
	 * reads the test file in the provided charset
	 *
	 * @param filename the filename to read
	 * @param charset the character set to use, for example Statics.UTF_16
	 * @return the resulting string
	 * @throws IOException if anything goes wrong
	 */
	protected String getFileAsString(String filename, Charset charset) throws IOException {

		//read from the test json topic that should contain the data we need
		return IOUtils.toString(getClass().getResourceAsStream(filename), charset);

	}

	/**
	 * @return The contents of the test file for this class, located in {@value #TEST_PATH}/className/(file)
	 */
	protected InputStream getFileAsInputStream(String filename) throws IOException {

		//read from the test json topic that should contain the data we need
		return getClass().getResourceAsStream(filename);


	}

}

