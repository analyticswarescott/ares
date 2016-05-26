package com.aw.common.util.os;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to execute a system command from a Java application.
 * See the documentation for the public methods of this class for more
 * information.
 *
 * Documentation for this class is available at this URL:
 *
 * http://devdaily.com/java/java-processbuilder-process-system-exec
 *
 *
 * Copyright 2010 alvin j. alexander, devdaily.com.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.

 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Please ee the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 *
 */
public class SysCommandExecutor
{
    private List<String> commandInformation;
    private String adminPassword;
    private ThreadedStreamHandler inputStreamHandler;
    private ThreadedStreamHandler errorStreamHandler;

    public Map<String, String> getAddl_Environment() {
        return addl_Environment;
    }

    public void setAddl_Environment(Map<String, String> addl_Environment) {
        this.addl_Environment = addl_Environment;
    }

    private  Map<String, String> addl_Environment = new HashMap<>();



    public SysCommandExecutor(final List<String> commandInformation)
    {
        if (commandInformation==null) throw new NullPointerException("The commandInformation is required.");
        this.commandInformation = commandInformation;
        this.adminPassword = null;
    }

    public CommandResult executeCommand() throws Exception{
       return executeCommand(null);
    }


	//for commands not expected to complete (e.g. self killing command for patch)
	public void execCommand(File dir) throws Exception{

		Process p = null;

			ProcessBuilder pb = new ProcessBuilder(commandInformation);
			Map<String, String> env = System.getenv();


			HashMap<String, String> newEnv = new HashMap<String, String>();
			newEnv.putAll(env);

			if (!newEnv.containsKey("JAVA_HOME")) {
				newEnv.put("JAVA_HOME", System.getProperty("java.home"));
			}



			//add any sepcified ENV adds
			newEnv.putAll(getAddl_Environment());


			// newEnv.put("PATH", EnvironmentSettings.getDgHome() +  "/core/jdk1.8.0_25/bin" + ":$PATH");

			if (dir != null) {
				pb.directory(dir);
			}

			pb.environment().putAll(newEnv);
			p = pb.start();


	}

    public CommandResult executeCommand(File dir)
            throws Exception
    {
        int exitValue = -99;

        Process p = null;
        long l = 0;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(commandInformation);
            Map<String, String> env = System.getenv();


            HashMap<String, String> newEnv = new HashMap<String, String>();
            newEnv.putAll(env);

            if (!newEnv.containsKey("JAVA_HOME")) {
                newEnv.put("JAVA_HOME", System.getProperty("java.home"));
            }



            //add any sepcified ENV adds
            newEnv.putAll(getAddl_Environment());


           // newEnv.put("PATH", EnvironmentSettings.getDgHome() +  "/core/jdk1.8.0_25/bin" + ":$PATH");

            if (dir != null) {
                pb.directory(dir);
            }

            pb.environment().putAll(newEnv);
            p = pb.start();

            l = unixLikeProcessId(p);



            // you need this if you're going to write something to the command's input stream
            // (such as when invoking the 'sudo' command, and it prompts you for a password).
            OutputStream stdOutput = p.getOutputStream();

            // i'm currently doing these on a separate line here in case i need to set them to null
            // to get the threads to stop.
            // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
            InputStream inputStream = p.getInputStream();
            InputStream errorStream = p.getErrorStream();

            // these need to run as java threads to get the standard output and error from the command.
            // the inputstream handler gets a reference to our stdOutput in case we need to write
            // something to it, such as with the sudo command
            inputStreamHandler = new ThreadedStreamHandler(inputStream, stdOutput, adminPassword);
            errorStreamHandler = new ThreadedStreamHandler(errorStream);

            // TODO the inputStreamHandler has a nasty side-effect of hanging if the given password is wrong; fix it
            inputStreamHandler.start();
            errorStreamHandler.start();

            // TODO a better way to do this?
            exitValue = p.waitFor();

            // TODO a better way to do this?
            inputStreamHandler.interrupt();
            errorStreamHandler.interrupt();
            inputStreamHandler.join();
            errorStreamHandler.join();
        }
        catch (IOException e)
        {
            // TODO deal with this here, or just throw it?
            throw e;
        }
        catch (InterruptedException e)
        {
            // generated by process.waitFor() call
            // TODO deal with this here, or just throw it?
            throw e;
        }
        finally
        {
            CommandResult cr = new CommandResult();
            cr.process =p;
            cr.pid = l;
            cr.result =exitValue;
            return cr;
        }
    }

    /**
     * Get the standard output (stdout) from the command you just exec'd.
     */
    public StringBuilder getStandardOutputFromCommand()
    {
		try {
			return inputStreamHandler.getOutputBuffer();
		}
		catch (Exception ex) {
			return new StringBuilder();
		}
    }

    /**
     * Get the standard error (stderr) from the command you just exec'd.
     */
    public StringBuilder getStandardErrorFromCommand()
    {
        return errorStreamHandler.getOutputBuffer();
    }


    private static Long unixLikeProcessId(Process process) {
        Class<?> clazz = process.getClass();
        try {
            if (clazz.getName().equals("java.lang.UNIXProcess")) {
                Field pidField = clazz.getDeclaredField("pid");
                pidField.setAccessible(true);
                Object value = pidField.get(process);
                if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                }
            }
        } catch (SecurityException sx) {
            sx.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static CommandResult executeSimple(List<String> command) throws Exception{

        SysCommandExecutor sc = new SysCommandExecutor(command);

        CommandResult cr = sc.executeCommand();
        cr.stdOut = sc.getStandardOutputFromCommand();
        cr.stdErr = sc.getStandardErrorFromCommand();
        return cr;

    }

}






