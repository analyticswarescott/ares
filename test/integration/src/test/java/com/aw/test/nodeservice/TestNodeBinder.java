package com.aw.test.nodeservice;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import com.aw.TestDependencies;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.RoleManager;
import com.aw.platform.monitoring.os.SysStatReader;
import com.aw.platform.nodes.NodeManager;
import com.aw.platform.nodes.managers.*;
import com.aw.rest.resources.NodeServiceTest.TestRoleManager;

public class TestNodeBinder extends TestDependencies {

	private NodeManager nodeManager;

	public static NodeManager getNodeManager() { return binder.nodeManager; }

	public TestNodeBinder() {
		super();

		synchronized (TestNodeBinder.class) {
			binder = this;
		}

	}

	@Override
	protected void configure() {
		super.configure();

		try {

	    	nodeManager = new NodeManager(platformMgr, docs, new TestSysStatReader(),
					mockRoleManager(RestRoleManager.class),
					mockRoleManager(HdfsDataRoleManager.class),
					mockRoleManager(HdfsNameRoleManager.class),
					mockRoleManager(HdfsJournalRoleManager.class),
					mockRoleManager(ElasticSearchRoleManager.class),
					mockRoleManager(KafkaRoleManager.class),
					mockRoleManager(ZookeeperRoleManager.class),
					mockRoleManager(SparkMasterRoleManager.class),
					mockRoleManager(SparkWorkerRoleManager.class),
					mockRoleManager(ConfigDBMasterRoleManager.class),
					mockRoleManager(ConfigDBWorkerRoleManager.class),
					mockRoleManager(DefaultRoleManager.class)) {
				@Override
				public RoleManager getRoleManager(NodeRole role) {
					return new TestRoleManager(platformMgr.get(), role, docs);
				}
			};

			bind(nodeManager).to(NodeManager.class);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e); //test exception, just bubble it up
		}

	}

	private <T extends RoleManager> T mockRoleManager(Class<T> type) throws Exception {
		T ret = mock(type);
		NodeRole role = NodeRole.fromManager(type);
		doReturn(role).when(ret).getRole();
		return ret;
	}

	private static TestNodeBinder binder;

	private static class TestSysStatReader extends SysStatReader {

		@Override
		public String getRawData(Platform p, Instant t) throws Exception {
			return TEST_DELIMITED;
		}

		@Override
		public String getRawNetworkData(Platform p, Instant t) throws Exception {
			return TEST_XML_NETWORK;
		}

	}

	public static String TEST_DELIMITED = "# hostname;interval;timestamp;CPU;%usr;%nice;%sys;%iowait;%steal;%irq;%soft;%guest;%gnice;%idle[...];proc/s;cswch/s;pswpin/s;pswpout/s;pgpgin/s;pgpgout/s;fault/s;majflt/s;pgfree/s;pgscank/s;pgscand/s;pgsteal/s;%vmeff;tps;rtps;wtps;bread/s;bwrtn/s;frmpg/s;bufpg/s;campg/s;kbmemfree;kbmemused;%memused;kbbuffers;kbcached;kbcommit;%commit;kbactive;kbinact;kbdirty;kbswpfree;kbswpused;%swpused;kbswpcad;%swpcad;dentunusd;file-nr;inode-nr;pty-nr;runq-sz;plist-sz;ldavg-1;ldavg-5;ldavg-15;blocked\n" +
			"DEV-DG8-03.verdasys.com;60;2016-03-29 08:59:01 UTC;-1;0.00;0.00;0.01;0.00;0.00;0.00;0.00;0.00;0.01;99.98;0.12;40.62;0.00;0.00;0.00;0.85;56.80;0.00;25.50;0.00;0.00;0.00;0.00;0.17;0.00;0.17;0.00;1.70;0.92;0.00;0.03;15471568;746516;4.60;0;277212;402728;1.65;205828;113428;4;8130664;61332;0.75;6200;10.11;61043;1824;28664;0;0;316;0.00;0.01;0.05;0\n" +
			"DEV-DG8-03.verdasys.com;60;2016-03-29 09:00:01 UTC;-1;0.00;0.00;0.00;0.00;0.00;0.00;0.00;0.00;0.00;99.99;0.12;75.10;0.00;0.00;0.00;0.45;56.86;0.00;24.92;0.00;0.00;0.00;0.00;0.13;0.00;0.13;0.00;0.90;2.25;0.00;0.03;15472108;745976;4.60;0;277220;402720;1.65;205848;113428;4;8130664;61332;0.75;6200;10.11;61049;1824;28664;0;0;316;0.00;0.01;0.05;0";

		public static String TEST_XML_NETWORK = "<sysstat>\n" +
			"\t<sysdata-version>2.15</sysdata-version>\n" +
			"\t<host nodename=\"DEV-DG8-03.verdasys.com\">\n" +
			"\t\t<sysname>Linux</sysname>\n" +
			"\t\t<release>3.10.0-229.el7.x86_64</release>\n" +
			"\t\t<machine>x86_64</machine>\n" +
			"\t\t<number-of-cpus>16</number-of-cpus>\n" +
			"\t\t<file-date>2016-03-27</file-date>\n" +
			"\t\t<statistics>\n" +
			"\t\t\t<timestamp date=\"2016-03-27\" time=\"10:00:01\" utc=\"1\" interval=\"60\">\n" +
			"\t\t\t\t<network per=\"second\">\n" +
			"\t\t\t\t\t<net-dev iface=\"lo\" rxpck=\"0.00\" txpck=\"0.00\" rxkB=\"0.00\" txkB=\"0.00\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-dev iface=\"em1\" rxpck=\"0.12\" txpck=\"0.08\" rxkB=\"0.01\" txkB=\"0.01\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.03\"/>\n" +
			"\t\t\t\t\t<net-dev iface=\"em2\" rxpck=\"0.00\" txpck=\"0.00\" rxkB=\"0.00\" txkB=\"0.00\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"lo\" rxerr=\"0.00\" txerr=\"0.00\" coll=\"0.00\" rxdrop=\"0.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"em1\" rxerr=\"0.00\" txerr=\"0.00\" coll=\"0.00\" rxdrop=\"0.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"em2\" rxerr=\"0.00\" txerr=\"0.00\" coll=\"0.00\" rxdrop=\"0.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t</network>\n" +
			"\t\t\t</timestamp>\n" +
			"\t\t</statistics>\n" +
			"\t\t<restarts>\n" +
			"\t\t</restarts>\n" +
			"\t</host>\n" +
			"</sysstat>";
}
