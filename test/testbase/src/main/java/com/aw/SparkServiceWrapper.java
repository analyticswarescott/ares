package com.aw;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.HttpStatusUtils;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;

public class SparkServiceWrapper {

	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SparkServiceWrapper.class);

    //try not to clash with anything else
    private static int UI_PORT = 8081;
    private static int UI_PORT_SLAVE = 8082;
    private static int SPARK_PORT = 7077;

    Process m_masterProcess;
    Process m_workerProcess;

    public SparkServiceWrapper() {
    }

    public static SparkServiceWrapper create() {
        SparkServiceWrapper sparkContextWrapper = new SparkServiceWrapper();
        return sparkContextWrapper;
    }


   public Future<Boolean> start() throws Exception {

	   startMaster();
	   startWorker();
	   //startWorker();

		return new Future<Boolean>() {
			@Override
			public Boolean get() throws InterruptedException, ExecutionException {
				return true;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return true;
			}

			@Override
			public Boolean get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return true;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

		};
    }

   private void startMaster() throws Exception {

	   //start the master

	   //use local host name
	   String hostname = PlatformMgr.getCachedPlatform().getNode(NodeRole.SPARK_MASTER).getHost();

	   ProcessBuilder master = new ProcessBuilder("./spark_test/bin/" + getCommand(),
			                                     "org.apache.spark.deploy.master.Master",
			                                     "--ip", PlatformMgr.getCachedPlatform().getNode(NodeRole.SPARK_MASTER).getHost(),
			                                     "--port", String.valueOf(SPARK_PORT),
			                                     "--webui-port", String.valueOf(UI_PORT));

	   //set the platform path
	   master.environment().put(EnvironmentSettings.Setting.PLATFORM_PATH.name(), EnvironmentSettings.getPlatformPath());

	   if (System.getProperty(BaseIntegrationTest.SHOW_CHILD_LOGS) != null) {
		   master = master.inheritIO(); //we want the stdin / stdout
	   }

	   m_masterProcess = master.start();

	   //detect that it's up
       CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	   int status = 400;
	   int tries = 0;
	   while (tries < 20 && !HttpStatusUtils.isSuccessful(status)) {

		   Thread.sleep(1000L);

	       HttpGet get = new HttpGet("http://" + hostname + ":" + UI_PORT);
	       try {
			   CloseableHttpResponse response = httpClient.execute(get);
			   status = response.getStatusLine().getStatusCode();
			   response.close();
	       } catch (Exception e) {
	       }

		   tries++;

	   }
	   httpClient.close();

   }

   private void startWorker() throws Exception {

	   //use local host name
	   String hostname = PlatformMgr.getCachedPlatform().getNode(NodeRole.SPARK_MASTER).getHost();

	   //start the worker
	   ProcessBuilder worker = new ProcessBuilder("./spark_test/bin/" + getCommand(),
			                                      "org.apache.spark.deploy.worker.Worker",
			   									   "--webui-port", String.valueOf(UI_PORT_SLAVE),
			   PlatformMgr.getCachedPlatform().getNode(NodeRole.SPARK_MASTER).getHost() + ":" + SPARK_PORT);
	   worker.environment().put(EnvironmentSettings.Setting.PLATFORM_PATH.name(), EnvironmentSettings.getPlatformPath());

	   if (System.getProperty(BaseIntegrationTest.SHOW_CHILD_LOGS) != null) {
		   worker = worker.inheritIO(); //we want the stdin / stdout
	   }

	   m_workerProcess = worker.start();

	   //detect that it's up
       CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	   int status = 400;
	   int tries = 0;
	   while (tries < 20 && !HttpStatusUtils.isSuccessful(status)) {

		   Thread.sleep(1000L);

		   try {
		       HttpGet get = new HttpGet("http://" + hostname + ":" + UI_PORT_SLAVE);
			   CloseableHttpResponse response = httpClient.execute(get);
			   status = response.getStatusLine().getStatusCode();
			   response.close();
	       } catch (Exception e) {
	       }

		   tries++;
	   }
	   httpClient.close();

	   if (tries >= 20) {
		   throw new Exception("Failed to start spark, status=" + status);
	   }

	   m_running = true;

   }

   private String getCommand() {
	   String command = "spark-class";
	   if (EnvironmentSettings.isWindows()) {
		   command += ".cmd";
	   }
	   return command;
   }

   public void stop() {

/*	   try {
		   PlatformController.killDrivers();
	   } catch (Exception e) {
		   e.printStackTrace();
	   }*/

	   m_masterProcess.destroyForcibly();
	   try {
		   Thread.sleep(1000);
	   } catch (InterruptedException e) {
		   e.printStackTrace();
	   }

	   m_workerProcess.destroyForcibly();
	/*   try {
		   Thread.sleep(1000);
	   } catch (InterruptedException e) {
		   e.printStackTrace();
	   }*/

   }

   public boolean isRunning() { return m_running; }
   private boolean m_running = false;

}