package com.aw.common.xml;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import com.aw.common.util.XMLUtils;

public class ConfigurationTest {

	private static String EXPECTED =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
			"<configuration xmlns=\"http://www.digitalguardian.com/xsd/configuration/\">\n" +
			"    <property>\n" +
			"        <name>dfs.replication</name>\n" +
			"        <value>1</value>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.name.dir</name>\n" +
			"        <value>/opt/aw/data/hadoop/name</value>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.data.dir</name>\n" +
			"        <value>/opt/aw/data/hadoop/data</value>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.data.dir</name>\n" +
			"        <value>/opt/aw/data/hadoop/tmp</value>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>mapred.child.java.opts</name>\n" +
			"        <value>-Xmx1024m</value>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.namenode.rpc-bind-host</name>\n" +
			"        <value>0.0.0.0</value>\n" +
			"        <description>\n" +
			"            The actual address the RPC server will bind to. If this optional address is\n" +
			"            set, it overrides only the hostname portion of dfs.namenode.rpc-address.\n" +
			"            It can also be specified per name node or name service for HA/Federation.\n" +
			"            This is useful for making the name node listen on all interfaces by\n" +
			"            setting it to 0.0.0.0.\n" +
			"        </description>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.namenode.servicerpc-bind-host</name>\n" +
			"        <value>0.0.0.0</value>\n" +
			"        <description>\n" +
			"            The actual address the service RPC server will bind to. If this optional address is\n" +
			"            set, it overrides only the hostname portion of dfs.namenode.servicerpc-address.\n" +
			"            It can also be specified per name node or name service for HA/Federation.\n" +
			"            This is useful for making the name node listen on all interfaces by\n" +
			"            setting it to 0.0.0.0.\n" +
			"        </description>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.namenode.http-bind-host</name>\n" +
			"        <value>0.0.0.0</value>\n" +
			"        <description>\n" +
			"            The actual adress the HTTP server will bind to. If this optional address\n" +
			"            is set, it overrides only the hostname portion of dfs.namenode.http-address.\n" +
			"            It can also be specified per name node or name service for HA/Federation.\n" +
			"            This is useful for making the name node HTTP server listen on all\n" +
			"            interfaces by setting it to 0.0.0.0.\n" +
			"        </description>\n" +
			"    </property>\n" +
			"    <property>\n" +
			"        <name>dfs.namenode.https-bind-host</name>\n" +
			"        <value>0.0.0.0</value>\n" +
			"        <description>\n" +
			"            The actual adress the HTTPS server will bind to. If this optional address\n" +
			"            is set, it overrides only the hostname portion of dfs.namenode.https-address.\n" +
			"            It can also be specified per name node or name service for HA/Federation.\n" +
			"            This is useful for making the name node HTTPS server listen on all\n" +
			"            interfaces by setting it to 0.0.0.0.\n" +
			"        </description>\n" +
			"    </property>\n" +
			"</configuration>";

	@Test
	public void readWrite() throws Exception {

		Configuration cfg = XMLUtils.unmarshal(new FileReader("src/test/resources/com/aw/xml/configuration.xml"), Configuration.class);

        assertEquals("property count wrong", 9, cfg.getProperty().size());

		Property prop = cfg.getProperty().get(cfg.getProperty().size() - 1);
		assertEquals("property name wrong", "dfs.namenode.https-bind-host", prop.getName());
		assertEquals("property value wrong", "0.0.0.0", prop.getValue());
		assertEquals("property description wrong", "The actual adress the HTTPS server will bind to. If this optional address\n" +
				"            is set, it overrides only the hostname portion of dfs.namenode.https-address.\n" +
				"            It can also be specified per name node or name service for HA/Federation.\n" +
				"            This is useful for making the name node HTTPS server listen on all\n" +
				"            interfaces by setting it to 0.0.0.0.", prop.getDescription().trim());

		//test writing out
		Writer out = new StringWriter();
		XMLUtils.marshal(out, cfg);
		out.close();
		assertEquals("output xml wrong", EXPECTED.trim(), out.toString().trim());

	}

}
