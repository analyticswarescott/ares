/*

package com.dg.platform;

import com.dg.common.system.EnvironmentSettings;
import com.dg.document.Document;
import com.dg.document.DocumentHandler;
import com.dg.document.DocumentType;
import com.dg.document.TestDocumentHandler;
import com.dg.platform.nodes.NodeManager;
import com.dg.platform.nodes.RoleOSCommand;
import com.dg.platform.roles.*;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class RoleManagerCommandTest {


	private HashMap<String, String> expectedStarts = new HashMap<String, String>();
	private HashMap<String, String> expectedStops = new HashMap<String, String>();

	private void setExpectedStarts() {
		expectedStarts.put("KAFKA0", "/roles/kafka/bin/./kafka-server-start.sh -daemon ../config/server.properties");
		expectedStarts.put("REST0", "NONE");
		expectedStarts.put("ELASTICSEARCH0", "/roles/elastic/bin/./elasticsearch -d --pidfile ./espid.pid");
		expectedStarts.put("SPARK_WORKER0", "/roles/spark/sbin/./start-slave.sh localhost:7077 --webui-port 8082");
		expectedStarts.put("HDFS_JOURNAL0", "NONE");
		expectedStarts.put("ZOOKEEPER0", "/roles/zookeeper/bin/./zkServer.sh start");
		expectedStarts.put("HDFS_DATA0", "NONE");
		expectedStarts.put("HDFS_NAME0", "/roles/hadoop/bin/./hdfs namenode -format -nonInterActive");
		expectedStarts.put("HDFS_NAME1", "/roles/hadoop/sbin/./start-dfs.sh");
		expectedStarts.put("SPARK_MASTER0", "/spark/sbin/./start-master.sh --host localhost --port 7077 --webui-port 8888");
	}

	private void setExpectedStops() {
		expectedStops.put("KAFKA0", "/roles/kafka/bin/./kafka-server-stop.sh");
		expectedStops.put("REST0", "NONE");
		expectedStops.put("ELASTICSEARCH0", "/roles/elastic/bin/kill -9");
		expectedStops.put("SPARK_WORKER0", "/spark/sbin/./stop-slave.sh");
		expectedStops.put("HDFS_JOURNAL0", "NONE");
		expectedStops.put("ZOOKEEPER0", "/roles/zookeeper/bin/./zkServer.sh stop");
		expectedStops.put("HDFS_DATA0", "NONE");
		expectedStops.put("HDFS_NAME0", "/roles/hadoop/sbin/./stop-dfs.sh");
		expectedStops.put("SPARK_MASTER0", "/spark/sbin/./stop-master.sh");
	}



*/
/**
	 * 
	 *//*


	@Test
	public void testStartCommandGeneration() throws Exception {

		System.out.println("Start command testing -- ");

		setExpectedStarts();

		for (RoleManager rm : nm.getRoles().values()) {

			List<RoleOSCommand> startCommands = rm.getStartCommands();
			//HashMap<String, String> actualStarts = new HashMap<String, String>();
			if (startCommands != null) {
				int i = 0;
				for (RoleOSCommand c : startCommands) {
					//actualStarts.put(rm.getRole().toString() + i, c.toString().substring(EnvironmentSettings.getDgHome().length()));
					System.out.println(rm.getRole().toString()  + i +  " : " + c.toString().substring(EnvironmentSettings.getDgHome().length()));
					assertTrue(expectedStarts.containsKey(rm.getRole().toString() + i));
					assertEquals("expect proper command for role ", expectedStarts.get(rm.getRole().toString() + i),
							c.toString().substring(EnvironmentSettings.getDgHome().length()));

					i++;
				}
			}
			else {
				System.out.println(rm.getRole().toString() + "0 : NONE" );
				assertTrue(expectedStarts.containsKey(rm.getRole().toString() + "0"));
				assertEquals( "no command for role " , expectedStarts.get(rm.getRole().toString() + "0"), "NONE" );
			}

		}

		
	}

	@Test
	public void testStopCommandGeneration() throws Exception {


		System.out.println("Stop command testing -- ");

		setExpectedStops();

		for (RoleManager rm : nm.getRoles().values()) {

			List<RoleOSCommand> stopCommands = rm.getStopCommands();
			//HashMap<String, String> actualStarts = new HashMap<String, String>();
			if (stopCommands != null) {
				int i = 0;
				for (RoleOSCommand c : stopCommands) {
					//actualStarts.put(rm.getRole().toString() + i, c.toString().substring(EnvironmentSettings.getDgHome().length()));
					System.out.println(rm.getRole().toString() + i + " : " + c.toString().substring(EnvironmentSettings.getDgHome().length()));
					assertTrue(expectedStops.containsKey(rm.getRole().toString() + i));
					assertEquals("expect proper command for role ", expectedStops.get(rm.getRole().toString() + i),
							c.toString().substring(EnvironmentSettings.getDgHome().length()));

					i++;
				}
			}
			else {
				System.out.println(rm.getRole().toString() + "0 : NONE");
				assertTrue(expectedStops.containsKey(rm.getRole().toString() + "0"));
				assertEquals( "no command for role " , expectedStops.get(rm.getRole().toString() + "0"), "NONE" );
			}

		}





	}
	
}

*/
