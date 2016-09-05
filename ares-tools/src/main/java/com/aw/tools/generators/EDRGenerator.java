/*
package com.aw.tools.generators;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.entity.ByteArrayEntity;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.DGCommandLine;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;
import com.aw.platform.PlatformMgr;

@SuppressWarnings("static-access")
public class EDRGenerator extends AbstractEventGenerator {

	private static Option DATE_RANGE = OptionBuilder.hasArg()
			.withDescription("number of days the data should span, ending today")
			.withArgName("days")
			.isRequired(true)
			.create("range");

	private static Option SCANS = OptionBuilder.hasArg()
			.withDescription("Number of scans to generate")
			.withArgName("count")
			.isRequired(true)
			.create("scans");

	private static Option AVERAGE_STATIC_FILES = OptionBuilder.hasArg()
			.withDescription("Number of static files in the scan")
			.withArgName("size")
			.isRequired(true)
			.create("avg_static_files");

	private static Option MACHINE_COUNT = OptionBuilder.hasArg()
			.withDescription("Number of machines scanned")
			.withArgName("count")
			.isRequired(true)
			.create("machines");

	private static Option TENANT_ID = OptionBuilder.hasArg()
			.withDescription("The tenant id for which bundles will be generated")
			.withArgName("tenant id")
			.isRequired(true)
			.create("tenant");

	private static Option SERVER = OptionBuilder.hasArg()
			.withDescription("")
			.withArgName("host:port")
			.isRequired(true)
			.create("server");

	private static Option SOURCE = OptionBuilder.hasArg()
			.withDescription("Source zip for edr generation")
			.withArgName("path")
			.isRequired(true)
			.create("source");

	public EDRGenerator(Platform platform) {
		super(platform);
	}

	public EDRGenerator(CommandLine cli, Platform platform) {
		super(platform);
		setRange(Integer.parseInt(cli.getOptionValue(DATE_RANGE.getOpt())));
		setScans(Integer.parseInt(cli.getOptionValue(SCANS.getOpt())));
		setAverageStaticFiles(Integer.parseInt(cli.getOptionValue(AVERAGE_STATIC_FILES.getOpt())));
		setMachineCount(Integer.parseInt(cli.getOptionValue(MACHINE_COUNT.getOpt())));
		setTenantID(cli.getOptionValue(TENANT_ID.getOpt()));
		/////setServer(cli.getOptionValue(SERVER.getOpt()));
		setSource(cli.getOptionValue(SOURCE.getOpt()));

	}

	*/
/**
	 * Command line options for this tool
	 *//*

	public static class Cli extends DGCommandLine {

		*/
/**
		 * serial version UID
		 *//*

		private static final long serialVersionUID = 1L;

		public Cli() {
			super(DATE_RANGE, SCANS, AVERAGE_STATIC_FILES, MACHINE_COUNT, TENANT_ID, SERVER, SOURCE);
		}

	}

	public void execute() throws Exception {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime localStart = now.minusDays(30).with(LocalTime.MIN);
		Instant start = localStart.toInstant(ZoneOffset.UTC);
		Instant end = Instant.now();
		long period = (end.toEpochMilli() - start.toEpochMilli()) / m_scans;

		LocalDateTime cur = localStart;

		//generate the number of requested scans
		for (int x=0; x<m_scans; x++) {

			ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(m_source));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			FileOutputStream fout = new FileOutputStream("c:/temp/generated_edr.zip");
//			ZipArchiveOutputStream out = new ZipArchiveOutputStream(fout);
			ZipArchiveOutputStream out = new ZipArchiveOutputStream(new BufferedOutputStream(baos));

			System.out.println("scan time: " + cur);

			//our total count of files
			int totalCount = randomIntWithinVariance(m_averageStaticFiles, .5);
			int curCount = 0;

			//reference random files once in each scan
			Set<String> fileReferences = new HashSet<String>();

			ArchiveEntry entry = in.getNextEntry();
			while (entry != null) {

				//if not a staticdata json, just copy
				if (!entry.getName().contains("staticdata")) {

					ArchiveEntry newEntry = out.createArchiveEntry(new ArchiveFile(entry.getSize(), cur.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()), entry.getName());
					out.putArchiveEntry(newEntry);

					IOUtils.copy(in, out);

					out.closeArchiveEntry();

				}

				//else populate some static data if we haven't reached the file count limit yet
				else if (curCount < totalCount) {

					ArchiveEntry newEntry = out.createArchiveEntry(new ArchiveFile(entry.getSize(), cur.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()), entry.getName());
					out.putArchiveEntry(newEntry);

					JSONObject json = buildStaticData(fileReferences, totalCount - curCount, new JSONObject(IOUtils.toString(in)));
					out.write(json.toString().getBytes());
					curCount += json.getJSONArray("OnDiskExecutables").length();

					out.closeArchiveEntry();

				}

				entry = in.getNextEntry();

			}

			cur = cur.plus(period, ChronoUnit.MILLIS);

			out.flush();
			out.close();

			byte[] bytes = baos.toByteArray();

			getClient().postPayload(getTenantID(), PlatformClient.PayloadType.SCAN, randomMachineId(m_machineCount), UUID.randomUUID().toString(), new ByteArrayEntity(bytes));
		}

	}

	*/
/**
	 * Populate static data up to count files
	 *
	 * @param count The maximum number of files to generate with the current json file
	 * @param data The input json array of executables
	 * @return The output json array of executables
	 * @throws Exception
	 *//*

	private JSONObject buildStaticData(Set<String> referencedFiles, int count, JSONObject data) throws Exception {

		JSONArray executables = data.getJSONArray("OnDiskExecutables");

		JSONArray newList = new JSONArray();

		for (int x=0; x<count && x<executables.length(); x++) {

			JSONObject executable = executables.getJSONObject(x);

			//set up a file that correlates to one of our random files every once in a while
			String filename = randomFile();
			String path = executable.getString("FilePath");
			if (!referencedFiles.contains(filename)) {
				referencedFiles.add(filename);
				executable.put("FilePath", filename);
				executable.getJSONObject("HashCodes").put("MD5", getHash(filename));
				path = filename;
			}

			//enrich as necessary - for now let's stick a file extension in there
			executable.put("dg_file_ext", path.substring(path.lastIndexOf(".") + 1));

			newList.put(executable);

		}

		data.put("OnDiskExecutables", newList);

		return data;

	}

	public int getRange() { return m_range; }
	public void setRange(int range) { m_range = range; }
	private int m_range;

	public int getScans() { return m_scans; }
	public void setScans(int scans) { m_scans = scans; }
	private int m_scans;

	public int getAverageStaticFiles() { return m_averageStaticFiles; }
	public void setAverageStaticFiles(int averageStaticFiles) { m_averageStaticFiles = averageStaticFiles; }
	private int m_averageStaticFiles;

	public int getMachineCount() { return m_machineCount; }
	public void setMachineCount(int machineCount) { m_machineCount = machineCount; }
	private int m_machineCount;


	public String getSource() { return m_source; }
	public void setSource(String source) { m_source = source; }
	private String m_source;

	public static void main(String[] args) {

		Cli cli = new Cli();

		try {

			CommandLine commandLine = cli.parse(args);
			new EDRGenerator(commandLine, PlatformMgr.getCachedPlatform()).execute();

		} catch (ParseException e) {

			//on parameter error, output usage
			System.err.println(e.getMessage());
			cli.printUsage();

		} catch (Exception e) {

			//other exception types, print the full stack trace
			e.printStackTrace();

		}

	}

	//used to get basic file information into the output edr zip
	protected class ArchiveFile extends File {

		private static final long serialVersionUID = 1L;

		public ArchiveFile(long length, long time) {
			super("path");
			m_length = length;
			m_time = time;
		}

		@Override
		public long length() { return m_length; }
		private long m_length = 0L;

		@Override
		public long lastModified() { return m_time; }
		private long m_time = 0L;

	}

}
*/
