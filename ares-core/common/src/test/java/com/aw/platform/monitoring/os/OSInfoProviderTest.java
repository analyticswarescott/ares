package com.aw.platform.monitoring.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;

import java.time.Instant;
import java.util.List;

import org.junit.Test;

import com.aw.common.AbstractKafkaZkUnitTest;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.document.TestDocumentHandler;
import com.aw.platform.Platform;
import com.aw.platform.nodes.RoleOSCommand;

public class OSInfoProviderTest extends AbstractKafkaZkUnitTest {

	public static String MOCK_RAW_DATA = "# hostname;interval;timestamp;CPU;%usr;%nice;%sys;%iowait;%steal;%irq;%soft;%guest;%gnice;%idle[...];proc/s;cswch/s;pswpin/s;pswpout/s;pgpgin/s;pgpgout/s;fault/s;majflt/s;pgfree/s;pgscank/s;pgscand/s;pgsteal/s;%vmeff;tps;rtps;wtps;bread/s;bwrtn/s;frmpg/s;bufpg/s;campg/s;kbmemfree;kbmemused;%memused;kbbuffers;kbcached;kbcommit;%commit;kbactive;kbinact;kbdirty;kbswpfree;kbswpused;%swpused;kbswpcad;%swpcad;dentunusd;file-nr;inode-nr;pty-nr;runq-sz;plist-sz;ldavg-1;ldavg-5;ldavg-15;blocked\n" +
			"DEV-DG8-03.verdasys.com;60;2016-03-29 08:59:01 UTC;-1;0.00;0.00;0.01;0.00;0.00;0.00;0.00;0.00;0.01;99.98;0.12;40.62;0.00;0.00;0.00;0.85;56.80;0.00;25.50;0.00;0.00;0.00;0.00;0.17;0.00;0.17;0.00;1.70;0.92;0.00;0.03;15471568;746516;4.60;0;277212;402728;1.65;205828;113428;4;8130664;61332;0.75;6200;10.11;61043;1824;28664;0;0;316;0.00;0.01;0.05;0\n" +
			"DEV-DG8-03.verdasys.com;60;2016-03-29 09:00:01 UTC;-1;0.00;0.00;0.00;0.00;0.00;0.00;0.00;0.00;0.00;99.99;0.12;75.10;0.00;0.00;0.00;0.45;56.86;0.00;24.92;0.00;0.00;0.00;0.00;0.13;0.00;0.13;0.00;0.90;2.25;0.00;0.03;15472108;745976;4.60;0;277220;402720;1.65;205848;113428;4;8130664;61332;0.75;6200;10.11;61049;1824;28664;0;0;316;0.00;0.01;0.05;0";

		public static String MOCK_NETWORK_DATA = "<sysstat>\n" +
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
			"\t\t\t\t\t<net-dev iface=\"lo\" rxpck=\"2.00\" txpck=\"3.00\" rxkB=\"0.00\" txkB=\"55.00\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-dev iface=\"em1\" rxpck=\"0.12\" txpck=\"3.08\" rxkB=\"0.01\" txkB=\"44.01\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.03\"/>\n" +
			"\t\t\t\t\t<net-dev iface=\"em2\" rxpck=\"4.00\" txpck=\"2.00\" rxkB=\"0.00\" txkB=\"33.00\" rxcmp=\"0.00\" txcmp=\"0.00\" rxmcst=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"lo\" rxerr=\"55.00\" txerr=\"11.00\" coll=\"0.00\" rxdrop=\"9.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"em1\" rxerr=\"44.00\" txerr=\"12.44\" coll=\"0.00\" rxdrop=\"8.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t\t<net-edev iface=\"em2\" rxerr=\"33.00\" txerr=\"4.55\" coll=\"0.00\" rxdrop=\"7.00\" txdrop=\"0.00\" txcarr=\"0.00\" rxfram=\"0.00\" rxfifo=\"0.00\" txfifo=\"0.00\"/>\n" +
			"\t\t\t\t</network>\n" +
			"\t\t\t</timestamp>\n" +
			"\t\t</statistics>\n" +
			"\t\t<restarts>\n" +
			"\t\t</restarts>\n" +
			"\t</host>\n" +
			"</sysstat>";

		@Test
	public void test() throws Exception {

		DocumentHandler docHandler = new TestDocumentHandler();
		Platform platform = docHandler.getDocument(DocumentType.PLATFORM, Platform.LOCAL).getBodyAsObject();

		SysStatReader ssi = new SysStatReader();

		Instant now = Instant.ofEpochMilli(1459775290424L);
		RoleOSCommand command = ssi.buildRawDataCommand(platform, now);

		assertTrue(command.getArgs().toString().endsWith(" --, -bBqrRSuvwW, -I, SUM, -I, XALL, -u, ALL]"));

		DefaultOSInfoProvider pp = new DefaultOSInfoProvider();

		ssi = mock(SysStatReader.class);
		doReturn(MOCK_RAW_DATA).when(ssi).getRawData(any(), any());
		doReturn(MOCK_NETWORK_DATA).when(ssi).getRawNetworkData(any(), any());

		List<OSPerfStats> stats = pp.getPerfStats(platform, ssi, Instant.now().minusSeconds(5)); //pass bogus timestamp for testing

		assertEquals(stats.size(), 2);

		OSPerfStats stat = stats.get(0);

			//System.out.println(JSONUtils.objectToString(stat));

		//assert a few vaules in the POJO to match the test data
		assertEquals(99.98, stat.getPctidle(), 0.001);
		assertEquals(4.6, stat.getPctmemused(), 0.001);
		assertNotNull(stat.getDiskfreemb());
		assertEquals(16218084, stat.getKbtotalmem());


	}

}
