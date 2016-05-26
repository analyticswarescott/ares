package com.aw.tools;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class ToolsUnitTest {

	/**
	 * Get the path for the given filename for this unit test class.
	 *
	 * @param filename The filename needed
	 * @return The path to this file
	 */
	protected File getFile(String filename) throws FileNotFoundException {

		//check where we are
		File parentDir = new File("src/main/resources");

		//return the path to this file
		File ret = new File(parentDir, filename);
		if (!ret.exists()) {
			throw new FileNotFoundException("Test resource " + ret.getAbsolutePath() + " doesn't exist");
		}
		return ret;

	}

}
