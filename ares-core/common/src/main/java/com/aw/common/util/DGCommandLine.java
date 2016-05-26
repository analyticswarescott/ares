package com.aw.common.util;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.aw.common.exceptions.InitializationException;

/**
 * Wrapper for command line interface to provide some common code.
 *
 *
 *
 */
public class DGCommandLine extends Options {

	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = 1L;

	public DGCommandLine(Option... options) {

		if (options == null || options.length == 0) {
			throw new InitializationException("no options provided");
		}

		//build the options

		for (Option option : options) {
			addOption(option);
		}

	}

	public CommandLine parse(String[] args) throws ParseException {
		return new BasicParser().parse(this, args);
	}

	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("<command>", this);
	}

}

