package com.aw.common.spark;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.os.CommandResult;
import com.aw.common.util.os.SysCommandExecutor;
import com.aw.document.Document;
import com.aw.platform.Platform;
import com.aw.platform.PlatformUtils;

/**
 * Created by scott on 13/11/15.
 */
public class StartDriver {

    public static final Logger logger = LoggerFactory.getLogger(StartDriver.class);

    private static String locateComputeJar() {

        //get the parent dir
        String parentDir = System.getProperty("stream_lib_override");
        if (parentDir == null) {
            parentDir = EnvironmentSettings.getSparkLibHome();
        }

        //search for compute jar
        File[] computeJars = new File(parentDir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains("compute") && !name.toLowerCase().contains("sources");
			}
		});

        if (computeJars == null || computeJars.length != 1) {
            throw new InitializationException("could not locate compute jar for spark (" + Arrays.toString(computeJars) + ")");
        }

        else {
            return computeJars[0].getAbsolutePath();
        }

    }

    private static String getStreamClasspath() throws Exception{
        //TODO: resolve test vs. prod compute JAR build targeting

        String cpFile = "unset";
        if (System.getProperty("stream_lib_override") != null) {
            cpFile =  System.getProperty("stream_lib_override").toString() + File.separatorChar + "stream.classpath";
        }
        else {
            File f2 = new File(EnvironmentSettings.getDgHome() + File.separatorChar + "conf"
                    + File.separatorChar + "stream" +  File.separatorChar + "stream.classpath");
            cpFile = f2.getAbsolutePath();
        }

        String cp = FileUtils.readFileToString(new File(cpFile));
        return cp;

    }

    public static String submit(Platform platform, Document driverDefDoc, String driverNameAndTag) throws  Exception {

        DriverDef driverDef = driverDefDoc.getBodyAsObject();

        List<String> command = new ArrayList<String>();
        String script = EnvironmentSettings.getDgSparkHome() + "/bin/spark-submit";
        if (EnvironmentSettings.isWindows()) {
            script += ".cmd";
        }
        command.add(script);

        logger.info(" preparing spark-submit to " + script);

        command.add("--class");
        command.add("com.aw.compute.streams.drivers.DriverMain");
        command.add("--master");
        command.add(PlatformUtils.getSparkSubmitURL(platform));
        command.add("--deploy-mode");
        command.add("cluster");

        //supervision is optionally specified in the driver definition
        //default is true for fault tolerance, but false can be useful for debugging issues
        if (driverDef.isSupervised()) {
            command.add("--supervise");
        }


        command.add("--jars");

        String jarsRaw = getStreamClasspath().replace(':', ',') + ',';

        String computeJar = locateComputeJar();

        String jars = jarsRaw.replace("$SPARK_LIB_HOME", EnvironmentSettings.getSparkLibHome());
        jars = jars + "," + computeJar;

        command.add(jars);


        command.add("--name");
        command.add(driverNameAndTag);


        if (driverDef.getSparkExecutorCores() > 0){ // 0 woud allow all cores to be used
            command.add("--total-executor-cores");
            command.add(Integer.toString(driverDef.getSparkExecutorCores()));
       }


        command.add("--executor-memory");
        command.add(driverDef.getSparkExecutorMemory());

        command.add("--driver-class-path");
        //command.add(jars.replace(",",":"));
        command.add(getStreamClasspath().replace("$SPARK_LIB_HOME", EnvironmentSettings.getSparkLibHome()));


        //Add any defined options to the conf
        Map<String, String> sparkConfig = driverDef.getSparkConfigOptions();
        for (String key :  sparkConfig.keySet()) {
            command.add("--conf");
            command.add(key + "=" +  (String)  sparkConfig.get(key));
        }

        if (driverDef.getExtraJavaOptions() != null) {
            command.add("--conf");
            command.add("spark.executor.extraJavaOptions=" + driverDef.getExtraJavaOptions());
        }

        command.add(computeJar);
        command.add(driverDef.getDriverClass());
        command.add(driverDefDoc.getName());


// execute my command
        logger.debug("executing command :" + command.toString());
        logger.info("submitting driver: " + driverDef.getDriverClass());




        SysCommandExecutor commandExecutor = new SysCommandExecutor(command);
        CommandResult result = commandExecutor.executeCommand();

		if (result.result != 0 ) {
			String cmdString = "";

			for (String s : command) {
				cmdString = cmdString + s + " ";
			}
			logger.error(" command failed:  command was "  + cmdString);
		}

// get the output from the command
        StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
        StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

// print the output from the command
        //logger.debug("Driver submit  STDOUT");
        //logger.debug(stdout.toString());
        logger.debug("Driver submit STDERR");
        logger.debug(stderr.toString());

        String ret = getDriverID(stderr.toString());

        logger.debug(" Driver ID is " + ret);
        return  ret;

    }

    private static String getDriverID(String input) throws Exception{
        String driverID = null;

        String lookFor = "Driver successfully submitted as ";

        int dvs = input.indexOf(lookFor);
        if (dvs <= 0) {
            logger.debug(input);
            throw new Exception("spark-submit ERROR:  driver ID not detected; feedback from submission:  " + input );
        }

        int firstQuoteAfterDVS = input.indexOf("\"", dvs + lookFor.length());

        int absStart = dvs + lookFor.length();

        int driverIDLenth = firstQuoteAfterDVS -   absStart;
        driverID = input.substring(absStart, absStart + driverIDLenth);

        return driverID;
    }

    public static void kill(Platform platform, String driverID) throws Exception {


        List<String> command = new ArrayList<String>();
        String script = EnvironmentSettings.getDgSparkHome() + "/bin/spark-class";
        if (EnvironmentSettings.isWindows()) {
            script += ".cmd";
        }
        command.add(script);

        logger.info(" preparing spark-class to kill: " + script);

        command.add("org.apache.spark.deploy.Client");
        command.add("kill");
        command.add(PlatformUtils.getSparkMaster(platform));
        command.add(driverID);

        logger.info("executing command :" + command.toString());
        SysCommandExecutor commandExecutor = new SysCommandExecutor(command);
        CommandResult result = commandExecutor.executeCommand();

// get the output from the command
        StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
        StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

// print the output from the command
        logger.info("Driver kill  STDOUT");
        logger.info(stdout.toString());
        logger.info("Driver kill STDERR");
        logger.info(stderr.toString());

    }


}
