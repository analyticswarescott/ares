package com.aw.platform;

import com.aw.platform.nodes.managers.*;
import org.apache.log4j.Logger;

import com.aw.platform.monitoring.os.SysStatReader;
import com.aw.platform.nodes.NodeManager;
import com.aw.rest.inject.DGBinder;

/**
 * Bindings for node service
 *
 *
 *
 */
public class NodeBinder extends DGBinder {

	static final Logger LOGGER = Logger.getLogger(NodeBinder.class);

	@Override
	protected void configure() {
		super.configure();

		RestRoleManager rest = new RestRoleManager(platformMgr.get(), docs);
		HdfsDataRoleManager hdfsData = new HdfsDataRoleManager(platformMgr.get(), docs);
		HdfsNameRoleManager hdfsName = new HdfsNameRoleManager(platformMgr.get(), docs);
		HdfsJournalRoleManager hdfsJournal = new HdfsJournalRoleManager(platformMgr.get(), docs);
		ElasticSearchRoleManager es = new ElasticSearchRoleManager(platformMgr.get(), docs);
		SparkMasterRoleManager sMaster = new SparkMasterRoleManager(platformMgr.get(), docs);
		SparkWorkerRoleManager sWorker = new SparkWorkerRoleManager(platformMgr.get(), docs);
		ZookeeperRoleManager zoo = new ZookeeperRoleManager(platformMgr.get(), docs);
		KafkaRoleManager kafka = new KafkaRoleManager(platformMgr.get(), docs);
		ConfigDBMasterRoleManager configDb = new ConfigDBMasterRoleManager(platformMgr.get(), docs);
		ConfigDBWorkerRoleManager configDbWorker = new ConfigDBWorkerRoleManager(platformMgr.get(), docs);
		DefaultRoleManager defaultRole = new DefaultRoleManager(platformMgr.get(), docs);

		try {

			NodeManager nodeManager = new NodeManager(platformMgr, docs, new SysStatReader(), rest, hdfsData, hdfsName, hdfsJournal, es, kafka, zoo, sMaster, sWorker, configDb,configDbWorker , defaultRole);
			bind(nodeManager).to(NodeManager.class);

		} catch (Exception e) {
			LOGGER.fatal("could not initialize dependencies", e);
			throw new RuntimeException(e);
		}
	}
}
