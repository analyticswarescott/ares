package com.aw.platform.nodes.managers;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.XMLUtils;
import com.aw.common.xml.Configuration;
import com.aw.common.xml.Property;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.nodes.AbstractRoleManager;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.HdfsName;

/**
 * Base class to centralize config and ENV settings for HDFS Role managers
 */
public abstract class HDFSBaseRoleManager extends AbstractRoleManager {

	public static final String CORE_SITE_XML = "core-site.xml";
	public static final String HDFS_SITE_XML = "hdfs-site.xml";
	public static final String HADOOP_ENV = "hadoop-env.sh";

	public static final String CORE_SITE_XML_TEMPLATE = "hdfs_core_site";
	public static final String HDFS_SITE_XML_TEMPLATE = "hdfs_hdfs_site";
	public static final String HADOOP_ENV_TEMPLATE = "hdfs_hadoop_env";

	@Inject
	public HDFSBaseRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
		super(platformMgr, docs);
	}

	public Map<String, String> getEnv() {
		HashMap<String, String> ret = new HashMap<String, String>();

		ret.put("HADOOP_HOME", getHome());
		ret.put("HADOOP_CONF_DIR", getHome() + "/etc/hadoop");
		ret.put("HADOOP_PREFIX", getHome());
		ret.put("HADOOP_LIBEXEC_DIR", getHome() + "/libexec");

		return ret;
	}

	@Override
	public void configure() throws Exception {

		configureCoreSiteXML();
		configureHDFSSiteXML();
		configureHadoopENV();



	}

	private void configureHadoopENV() throws Exception{
		String conf = m_roleConfig.getConfigTemplateContent(HADOOP_ENV_TEMPLATE);
		//TODO: no changes based on platform at present -- using template only
		m_roleConfig.saveConfig(getHome() + File.separatorChar + "etc" + File.separatorChar
				+ "hadoop" + File.separatorChar + HADOOP_ENV,
			conf);

	}

	/**
	 * Add or modify any properties that only apply to a specific role
	 * @param propertyList
	 */
	protected abstract void configureHDFSSiteForRole( List<Property> propertyList);


	private void configureCoreSiteXML() throws Exception{
		String hdfs = m_roleConfig.getConfigTemplateContent(CORE_SITE_XML_TEMPLATE);
		Configuration conf = XMLUtils.unmarshal(new StringReader(hdfs), Configuration.class);
		List<Property> propertyList = conf.getProperty();

		final int numHadoopNameNodes = PlatformUtils.getHadoopNameNodes(platformMgr.getPlatform()).size();
		for (Property p : propertyList) {
			if (p.getName().equals("fs.defaultFS")) {
				if (numHadoopNameNodes > 1) {
					p.setValue("hdfs://" + m_node.getSetting(HdfsName.HA_CLUSTER_NAME));
				}
				else {
					p.setValue("hdfs://" + m_node.getHost()
						+ ":" +
						m_node.getSetting(HdfsName.PORT));
				}
			}

		}

		if (numHadoopNameNodes == 2) {
			configureHAinCoreSite(propertyList);
		}

		StringWriter sw = new StringWriter();
		XMLUtils.marshal(sw, conf);
		m_roleConfig.saveConfig(getHome() + File.separatorChar + "etc" + File.separatorChar + "hadoop" + File.separatorChar + CORE_SITE_XML,
			sw.toString());

	}

	private void configureHDFSSiteXML() throws Exception {

		//TODO: this will need to change if HA (2 name nodes) is detected
		//namenode RPC address will need to become name-service based

		String hdfs = m_roleConfig.getConfigTemplateContent(HDFS_SITE_XML_TEMPLATE);
		Configuration conf = XMLUtils.unmarshal(new StringReader(hdfs), Configuration.class);
		List<Property> propertyList = conf.getProperty();

//set other non-HA properties as appropriate for node type
		for (Property p : propertyList) {

			//set replication based on
			if (p.getName().equals("dfs.replication")) {
				//check desired replication in platform -- if not set, default to 3

				//default replication target is 3 unless set in name node global settings
				int desiredReplication = 3;

				int checkSetting = platformMgr.getPlatform().getNode(NodeRole.HDFS_NAME)
					.getSettingInt(HdfsName.DFS_REPLICATION);
				if (checkSetting != -1) {
					desiredReplication = checkSetting;
				}

				//check number of configured data nodes, if < desired, set replication = # nodes
				int availableNodes = platformMgr.getPlatform().getNodes(NodeRole.HDFS_DATA).size();
				if (availableNodes < desiredReplication) {
					p.setValue(Integer.toString(availableNodes));
					logger.warn(" dfs.replication set to available node count of " + availableNodes
						+ " rather than target of " + desiredReplication);
				} else {
					p.setValue(Integer.toString(desiredReplication));
				}

			}

		}


		//now allow sub-role to add any config
		if (PlatformUtils.getHadoopNameNodes(platformMgr.getPlatform()).size() == 2) {
			configureHDFSSiteForRole(propertyList);
			configureHAinHdfsSite(propertyList);
		}

		StringWriter sw = new StringWriter();
		XMLUtils.marshal(sw, conf);
		m_roleConfig.saveConfig(getHome() + File.separatorChar + "etc" + File.separatorChar + "hadoop" + File.separatorChar + HDFS_SITE_XML,
			sw.toString());

	}




	private void configureHAinCoreSite(List<Property> propertyList) throws Exception{
		for (Property p : propertyList) {
			if (p.getName().equals("ha.zookeeper.quorum")) {
				p.setValue(PlatformUtils.getZKQuorum(platformMgr.getPlatform()));
			}
		}
	}

	private void configureHAinHdfsSite(List<Property> propertyList) throws Exception{



		//get cluster/nameservice name

		String clusterName = PlatformUtils.getHadoopClusterName(platformMgr.getPlatform());

		//modify default properties

		for (Property p : propertyList) {

			if (p.getName().equals("dfs.nameservices")) {
				p.setValue(clusterName);
			}

			if (p.getName().startsWith("dfs.ha.namenodes.")) {

				p.setName("dfs.ha.namenodes." + clusterName );
				p.setValue(PlatformUtils.getHadoopNameNodeList(platformMgr.getPlatform()));
			}




		}

		//setup shared edits prop as it can only exist in HA
		Property pSE = new Property();
		pSE.setName("dfs.namenode.shared.edits.dir");
		pSE.setValue(PlatformUtils.getHadoopJournalNodeList(platformMgr.getPlatform()));
		propertyList.add(pSE);


		//add formulated properties rpc-address and http-address
		for (PlatformNode nn : PlatformUtils.getHadoopNameNodes(platformMgr.getPlatform())) {
			Property p = new Property();
			p.setName("dfs.namenode.rpc-address." + clusterName
				+ "." + nn.getHost());

			p.setValue(nn.getHost()
				+ ":" + nn.getSettingInt(HdfsName.PORT));
			p.setDescription(" added by DG configuration");
			propertyList.add(p);

			Property p2 = new Property();
			p2.setName("dfs.namenode.http-address." + clusterName
				+ "." + nn.getHost());
			p2.setValue(nn.getHost()
				+ ":" + nn.getSettingInt(HdfsName.WEB_UI_PORT));
			p2.setDescription(" added by DG configuration");
			propertyList.add(p2);


		}


       /* <property>
        <name>dfs.namenode.rpc-address.mycluster.nn1</name>
        <value>machine1.example.com:8020</value>
        </property>
        <property>
        <name>dfs.namenode.rpc-address.mycluster.nn2</name>
        <value>machine2.example.com:8020</value>
        </property>*/


        /*<property>
        <name>dfs.namenode.http-address.mycluster.nn1</name>
        <value>machine1.example.com:50070</value>
        </property>
        <property>
        <name>dfs.namenode.http-address.mycluster.nn2</name>
        <value>machine2.example.com:50070</value>
        </property>*/




	}



	@Override
	public NodeRole getRole() {
		return null;
	}

	@Override
	public String getHome() {
		return EnvironmentSettings.getDgHome() + File.separatorChar + "roles" + File.separatorChar + "hadoop";
	}

	private void configureSlaves() throws Exception {

		//TODO: this is a no-op until we need to support multiple data nodes
	}




	@Override
	public List<RoleOSCommand> getStartCommands() throws Exception {
		ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

		return ret;

	}


	@Override
	public List<RoleOSCommand> getStopCommands() throws Exception {
		ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

		return ret;
	}

}
