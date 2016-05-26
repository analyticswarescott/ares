package com.aw.common.system.cli;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.Document;
import com.aw.platform.DefaultPlatform;
import com.aw.platform.DefaultPlatformNode;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.roles.Rest;

/**
 * Used to make direct requests to the api
 *
 * TODO: scott, should this file really be in common?
 */
@SuppressWarnings("static-access")
public class LocalFileClient {


	static Option FILE = OptionBuilder
			.withDescription("document file path")
			.hasArg()
			.withArgName("path")
			.create("file");

	static Option OPERATION = OptionBuilder
			.withDescription("get create update or sync")
			.hasArg()
			.withArgName("operation")
			.isRequired()
			.create("operation");


	static Option VERSION = OptionBuilder
			.withDescription("version stamp")
			.hasArg()
			.withArgName("version")
			.create("version");



	private static Options OPTIONS = new Options() { {
		addOption(OPERATION);
		addOption(VERSION);
	} };

	enum Operation {
		INIT_PLATFORM_CACHE,
		INIT_BUILDSTAMP;
	}

	//represents all of something, for example all tenants
	private static final String ALL = "all";

	public static void main(String[] args) {

		try {

			CommandLine cli = new BasicParser().parse(OPTIONS, args);

			for ( int i = 0; i < cli.getArgList().size() ; i++) {
				System.out.println(cli.getArgList().get(i));

			}

			//get the required options
			Operation op = Operation.valueOf(cli.getOptionValue(OPERATION.getOpt()).toUpperCase());

			String version = cli.hasOption(VERSION.getOpt()) ? cli.getOptionValue(VERSION.getOpt()) : null;


			//get the document handler
			//System.setProperty("DG_REPORTING_SERVICE_BASE_URL", "http://" + hostPort);
			//DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID);

			//perform the requested operation
			switch (op) {
				case INIT_PLATFORM_CACHE:
					File f = new File(EnvironmentSettings.getConfDirectory() + File.separatorChar + "defaults"
						+ File.separatorChar + "platform" + File.separatorChar + Platform.LOCAL + ".json");
					initPlatformCache(f, -1);

					break;
				case INIT_BUILDSTAMP:
					initBuildStamp(version);
					break;

			}


		} catch (ParseException e) {

			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("LocalFileClient", OPTIONS);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void initBuildStamp(String version) throws Exception{
		File out = new File(EnvironmentSettings.getDgHome()
			+ File.separatorChar + "buildstamp");

/*		if (out.exists()) {
			throw new Exception(" file " + out.getAbsolutePath() + " already exists");
		}*/

		System.out.println(" applying buildstamp (temporary development version)  "
			+ EnvironmentSettings.getHost() + " to " + out.getAbsolutePath());
		FileWriter w = new FileWriter(out, false); //overwrite
		w.write(version);
		w.close();
	}

	public static void initPlatformCache(File sourcePlatform) {
		initPlatformCache(sourcePlatform, -1);
	}

	public static void initPlatformCache(File sourcePlatform, int restPort) {

		if (!EnvironmentSettings.isFirstNode()) {
			System.out.println("FIRST_NODE is false - not writing platform cacehe");
			return;
		}

		try {
			String s = FileUtils.readFileToString(sourcePlatform);

			Document doc = new Document(s);
			DefaultPlatform p = doc.getBodyAsObject();
			DefaultPlatformNode localhost = (DefaultPlatformNode)p.getNodes().remove("localhost");
			p.getNodes().put(EnvironmentSettings.getHost(), localhost);

			//set port
			if (restPort > 0) {
				localhost.addSetting(NodeRole.REST, Rest.PORT, restPort);
			}

			doc.setBodyFromObject(p);

			File out = new File(EnvironmentSettings.getPlatformPath());

			/*if (out.exists()) {
				throw new Exception(" file " + out.getAbsolutePath() + " already exists");
			}*/

			System.out.println("* caching updated local platform with host name:  "
				+ EnvironmentSettings.getHost() + " to " + out.getAbsolutePath());

			FileWriter w = new FileWriter(out, false); //overwrite
			w.write(doc.toString());
			w.close();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

}
