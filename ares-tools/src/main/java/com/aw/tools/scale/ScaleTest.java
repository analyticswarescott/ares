package com.aw.tools.scale;

import com.aw.common.util.JSONUtils;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manage the process of firing a scale test at a platform
 */
public class ScaleTest {

/*	static Option PLATFORM = OptionBuilder
		.withDescription("document file path")
		.hasArg()
		.withArgName("platform")
		.create("platform");*/

	static Option TESTDEF = OptionBuilder
		.withDescription("document file path")
		.hasArg()
		.withArgName("testdef")
		.create("testdef");

	private static Options OPTIONS = new Options() { {
		//addOption(PLATFORM);
		addOption(TESTDEF);
	} };

	public static void main(String[] args) throws Exception {

		CommandLine cli = new BasicParser().parse(OPTIONS, args);

		for ( int i = 0; i < cli.getArgList().size() ; i++) {
			System.out.println(cli.getArgList().get(i));

		}
		//get the required options

		//String platformFile = cli.hasOption(PLATFORM.getOpt()) ? cli.getOptionValue(PLATFORM.getOpt()) : null;
		String testDefFile = cli.hasOption(TESTDEF.getOpt()) ? cli.getOptionValue(TESTDEF.getOpt()) : null;

		//System.out.println(p.toString());

/*		//test def -- TODO: this can be used to generate JSON from the default object while the def is volatile
		TestDef test1 = new TestDef();
		test1.setTestName("Emergency Broadcast System");
		TestTenantGroup tg1 = new TestTenantGroup();
		test1.getTenantGroups().add(tg1);
		String s = JSONUtils.objectToString(test1);
		System.out.println(s);*/

		//Load test def
		File f = new File(testDefFile);
		String def = FileUtils.readFileToString(f);

		TestDef def1 = JSONUtils.objectFromString(def, TestDef.class);

		ExecutorService es = Executors.newFixedThreadPool(1);

		Future fut =  es.submit(def1);

		//launch the tree of threads defined in the test def
		fut.get();

		es.shutdown();


	}

}
