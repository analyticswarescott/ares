package com.aw.platform.monitoring.os;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.util.JSONUtils;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.nodes.NodeManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;

/**
 * Gather OS performance and status info
 */
public class DefaultOSInfoProvider {

	public static final Logger logger = LoggerFactory.getLogger(DefaultOSInfoProvider.class);
	public DefaultOSInfoProvider() {

	}

	public List<OSPerfStats> getPerfStats(Platform p, SysStatInterface ssi, Instant timestamp) throws  Exception {

		List<OSPerfStats> ret = new ArrayList<>();

		List<JSONObject> base = getBaseData(p, ssi, timestamp);

		//now  supplemental data to each object
		for (JSONObject stat: base) {

			OSPerfStats perf = JSONUtils.objectFromString(stat.toString(), OSPerfStats.class);
			try {
				addNetworkData(p, ssi, perf);
			}
			catch(Exception ee) {
				logger.error(" network data SYSSTAT error " + ee.getMessage());
			}
			addDiskSpace(perf);
			addNodeRoles(p, perf);
			ret.add(perf);
		}



		return ret;
	}

	private void addNodeRoles(Platform p, OSPerfStats baseData) {
		PlatformNode me = p.getMe();
		baseData.setNodeRoleList(new ArrayList<>(me.getRoles()));

	}


	private void addDiskSpace(OSPerfStats baseData) throws Exception{

		//TODO: this may need refinement or replacement depending on disk environment

		File f = new File(EnvironmentSettings.getDgData());
		if (!f.exists()) {
			f = new File(".");
		}

		long freeL = f.getFreeSpace();
		long totalL = f.getTotalSpace();

		double free =  freeL;
		double total = totalL;

		double freePct = (free/total) * 100 ;

		DecimalFormat df = new DecimalFormat("#.#");

		baseData.setDiskfreepct(Float.parseFloat(df.format(freePct)));
		baseData.setDiskfreemb(totalL / 1024 / 1024);

		baseData.setNumprocs(NodeManager.getAvaliableProcessors());

	}


	private static String NET_ACTIVITY_BY_DEVICE = "net-dev";
	private static String NET_ERRORS_BY_DEVICE = "net-edev";
	private  void addNetworkData(Platform p, SysStatInterface ssi, OSPerfStats baseData) throws Exception{

		//get the network records for this collection time
		String networkXML = ssi.getRawNetworkData(p, baseData.getDgtime());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		java.io.StringReader xmlReader = new java.io.StringReader(networkXML);
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(xmlReader));

		//call processing functions to create aggregate and detail network stats
		processNetworkStats(doc, NET_ACTIVITY_BY_DEVICE, new String[]{"rxkB", "txkB"}, baseData,
			baseData.getNetUsageDetails());

		processNetworkStats(doc, NET_ERRORS_BY_DEVICE, new String[]{"rxerr", "txerr"}, baseData,
			baseData.getNetErrorDetails());


	}


	private void processNetworkStats(Document doc, String tagToIterate, String[] statAttribs, OSPerfStats baseData,
									 List<Map<String, Object>> detailMap) {

		Element e = (Element) doc.getElementsByTagName("network").item(0);

		if (e == null) {
			logger.error("WARNING: no network stats found for time: " +   baseData.getDgtime());
			return;
			//throw new RuntimeException(" no network stats found for time: " +   baseData.getDgtime());
		}
		NodeList nList = e.getElementsByTagName(tagToIterate);


		for (int i = 0; i<nList.getLength(); i++) {
			Map<String, Object> details = new HashMap<>();
			Node nd = nList.item(i);

			String iface = nd.getAttributes().getNamedItem("iface").getNodeValue();
			details.put("stat_iface", iface);

			//loop the attribs and add to map for interface
			for (String att : statAttribs) {
				float curr = Float.parseFloat(nd.getAttributes().getNamedItem(att).getNodeValue());
				details.put("stat_" + att, curr);
			}

			detailMap.add(details);

		}



	}


	private  List<JSONObject> getBaseData(Platform p, SysStatInterface ssi, Instant timestamp) throws  Exception{

		ArrayList<JSONObject> ret = new ArrayList<>();

		String base = ssi.getRawData(p, timestamp);

		//clean the headers up
		String cleaned = base.replace("%", "pct_");
		cleaned = cleaned.replace("/s", "");
		cleaned = cleaned.replace("[...]", "");
		cleaned = cleaned.replace("# ", "");
		cleaned = cleaned.replace("-", "");
		cleaned = cleaned.replace("_", "");


		StringReader sr = new StringReader(cleaned);

		CSVFormat f = CSVFormat.newFormat(";".charAt(0));
		f = f.withHeader();

		CSVParser csvParser = new CSVParser(sr, f);


		List<CSVRecord> recs = csvParser.getRecords();

		for (CSVRecord r : recs ) {
			JSONObject o = new JSONObject();
			for (String s : csvParser.getHeaderMap().keySet()) {
				o.put("stat_" + s, r.get(s));
			}

			ret.add(o);
		}


		return ret;



	}

}
