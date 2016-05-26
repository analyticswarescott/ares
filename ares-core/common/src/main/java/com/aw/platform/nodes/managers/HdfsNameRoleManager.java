package com.aw.platform.nodes.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.system.structure.Hive;
import com.aw.common.xml.Property;
import com.aw.common.zookeeper.DefaultZkAccessor;
import com.aw.common.zookeeper.ZkAccessor;
import com.aw.document.DocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.platform.PlatformUtils;
import com.aw.platform.exceptions.NodeOperationException;
import com.aw.platform.monitoring.NodeRoleStatus;
import com.aw.platform.nodes.NodeUtil;
import com.aw.platform.nodes.RoleOSCommand;
import com.aw.platform.roles.HdfsName;

/**
 * Role manager for HDFS Node
 */
public class HdfsNameRoleManager extends HDFSBaseRoleManager {

    public static final String CORE_SITE_XML = "core-site.xml";
    public static final String HDFS_SITE_XML = "hdfs-site.xml";
    public static final String SLAVES = "slaves";

    @Inject
    public HdfsNameRoleManager(PlatformMgr platformMgr, Provider<DocumentHandler> docs) {
    	super(platformMgr, docs);
	}

    @Override
    protected void configureHDFSSiteForRole(List<Property> propertyList) {

    }

    @Override
    public NodeRole getRole() {
        return NodeRole.HDFS_NAME;
    }

    @Override
    public List<RoleOSCommand> getStartCommands() throws Exception {
		ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();

		String dir;

		//create command index since the number of commands will vary
		int cmdidx = 0;

		//check for namenode formatting e.g. the existence of /opt/aw/data/hadoop/name/current/VERSION
		boolean isFormatted = false;
		File f = new File(EnvironmentSettings.getDgData() + "/hadoop/name/current/VERSION");
		logger.info("checking for HDFS VERSION file at " + f.getAbsolutePath());
		isFormatted = f.exists();


		if (!isFormatted) {
			if (m_node.getSetting(HdfsName.STANDBY) != null) {

				logger.warn("formatting as stand-by NameNode");
				//** script 1 -- format name node if formatting not detected
				String script = "./hdfs";
				dir = getHome() + "/bin";
				List<String> args = new ArrayList<String>();

				args.add("namenode");
				args.add("-bootstrapStandBy");

				RoleOSCommand command = new RoleOSCommand(dir, script, args);
				ret.add(cmdidx, command);
				cmdidx++;

			}
			else {
				logger.warn("formatting as primary NameNode");
				//** script 1 -- format name node if formatting not detected
				String script = "./hdfs";
				dir = getHome() + "/bin";
				List<String> args = new ArrayList<String>();

				args.add("namenode");
				args.add("-format");
				args.add("-clusterId");
				args.add("dgHDFSCluster");
				// args.add("-nonInterActive"); //this should not be needed

				RoleOSCommand command = new RoleOSCommand(dir, script, args);
				ret.add(cmdidx, command);
				cmdidx++;
			}


		} else {
			logger.warn(" VERSION file exists.  NameNode will not be formatted");
		}

		//******* script 2 == namenode daemon start
		String script2 = "./hadoop-daemon.sh";
		List<String> args2 = new ArrayList<String>();
		dir = getHome() + "/sbin";

		args2.add("--config");
		//args2.add("$HADOOP_CONF_DIR");
		args2.add(getHome() + "/etc/hadoop");

		args2.add("--script");
		args2.add("hdfs");

		args2.add("start");
		args2.add("namenode");

		RoleOSCommand command2 = new RoleOSCommand(dir, script2, args2);
		ret.add(cmdidx, command2);
		cmdidx++;


		final int numHadoopNameNodes = PlatformUtils.getHadoopNameNodes(platformMgr.getPlatform()).size();
		if (numHadoopNameNodes == 1) {
			//do not start secondarynamenode if 2 namenodes are defines
			//******* script 3 == secondary namenode daemon start -- this is NOT HA, just a backup fsimge updater
			String script3 = "./hadoop-daemon.sh";
			List<String> args3 = new ArrayList<String>();
			dir = getHome() + "/sbin";

			args3.add("start");
			args3.add("secondarynamenode");

			RoleOSCommand command3 = new RoleOSCommand(dir, script3, args3);
			ret.add(cmdidx, command3);
			cmdidx++;
		}
		//start HA mode if there are 2 namenodes
		else if (numHadoopNameNodes == 2) {
			//script 4 -- initialize zookeeper
			//
			//
			// TODO: does this need to be only done the first time?
			ZkAccessor zk = null;
			try {
				 zk = new DefaultZkAccessor(platformMgr.get(), Hive.SYSTEM);
				if (!zk.exists("/hadoop-ha")) {
					String script4 = "./hdfs";
					List<String> args4 = new ArrayList<String>();
					dir = getHome() + "/bin";

					args4.add("zkfc");
					args4.add("-formatZK");

					RoleOSCommand command4 = new RoleOSCommand(dir, script4, args4);
					ret.add(cmdidx, command4);
					cmdidx++;
				}
			}
			finally {
				if (zk!=null) {zk.cleanup();}; //close curator client after one-time use of accessor
			}

			//TODO: is there a timing issue between the adding of the node and the start of ZKFC?
			Thread.sleep(500);

			//**** script 5 ZKFC monitor to initiate failover if node health goes bad
			//[hdfs]$ $HADOOP_PREFIX/sbin/hadoop-daemon.sh --script $HADOOP_PREFIX/bin/hdfs start zkfc
			String script5 = "./hadoop-daemon.sh";
			List<String> args5 = new ArrayList<String>();
			dir = getHome() + "/sbin";

			args5.add("start");
			args5.add("zkfc");

			RoleOSCommand command5 = new RoleOSCommand(dir, script5, args5);
			ret.add(cmdidx, command5);
			cmdidx++;
		}



        return ret;

    }


    @Override
    public List<RoleOSCommand> getStopCommands() throws Exception {
        ArrayList<RoleOSCommand> ret = new ArrayList<RoleOSCommand>();
        String dir = getHome() + "/sbin";

		//create command index since the number of commands will vary
		int cmdidx = 0;


		final int numHadoopNameNodes = PlatformUtils.getHadoopNameNodes(platformMgr.getPlatform()).size();
		if (numHadoopNameNodes == 1) {
			//******* script 2 == secondary namenode daemon STOP -- if not in HA mode
			String script2 = "./hadoop-daemon.sh";
			List<String> args2 = new ArrayList<String>();

			args2.add("stop");
			args2.add("secondarynamenode");

			RoleOSCommand command2 = new RoleOSCommand(dir, script2, args2);
			ret.add(cmdidx, command2);
			cmdidx++;
		} else if (numHadoopNameNodes == 2) {
			//**** stop ZKFC if HA
			String script3 = "./hadoop-daemon.sh";
			List<String> args3 = new ArrayList<String>();
			dir = getHome() + "/sbin";

			args3.add("stop");
			args3.add("zkfc");

			RoleOSCommand command3 = new RoleOSCommand(dir, script3, args3);
			ret.add(cmdidx, command3);
			cmdidx++;
		}


		//******* script 1 == namenode daemon STOP (run last)
		String script = "./hadoop-daemon.sh";
		List<String> args = new ArrayList<String>();

		args.add("stop");
		args.add("namenode");

		RoleOSCommand command = new RoleOSCommand(dir, script,args);
		ret.add(cmdidx, command);
		cmdidx++;


		return ret;



    }

    @Override
    public NodeRoleStatus getStatus() throws NodeOperationException {
		//hdfs.server.namenode.NameNode

        NodeRoleStatus ret = super.getStatus();

/*        RestClient r = new RestClient(NodeRole.HDFS_NAME, HdfsName.WEB_UI_PORT);
		r.setSpecificNode(m_node); //check THIS node for a web UI

        HttpResponse resp = null;
        try {
            resp = r.get("");
        } catch (Exception e) {
            e.printStackTrace();
            ret.setState(RoleStatus.RoleState.STOPPED);
            return ret;
        }





        if (resp.getStatusLine().getStatusCode() == 200) {
            ret.setState(RoleStatus.RoleState.RUNNING);
        }
        else {
            ret.setState(RoleStatus.RoleState.ERROR);
            ret.setStatusMessage(" HDFS Web UI call returned status " + resp.getStatusLine().getStatusCode());
        }*/


		//TODO: bug SLC-830 -- reverting to process check until HTTP issues can be resolved

		NodeUtil.statusFromProcessCount(getRole(), "namenode.NameNode", 1, ret);



        return ret;
    }


}
