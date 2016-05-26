package com.aw.common.processor;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.TimeSource;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;
import com.aw.util.Statics;
import com.google.common.base.Preconditions;

/**
 * on 4/4/16.
 */
public class FileArchiveProcessor extends AbstractIterableProcessor implements SecurityAware {

	private static final long serialVersionUID = 1L;

	private static final String ROOT_PATH = "/";

	private TimeSource time;

	@Inject
	public FileArchiveProcessor(PlatformMgr platformMgr, TimeSource time) {
		this.platformMgr = platformMgr;
		this.time = time;
	}

	@Override
	public void process(String tenantId, Iterable<String> messages) throws ProcessingException {

		try {

			PlatformMgr platformMgr = getPlatformMgr();

			FileReader fileReader = platformMgr.getTenantFileReader();
			FileWriter fileWriter = platformMgr.getTenantFileWriter();

			//create the archive
			String filename = createArchiveFromIterables(tenantId, fileReader, fileWriter, messages);

			//log the creation of the archive
			platformMgr.handleLog("wrote archive for tenant " + getTenantID() + " : " + filename, NodeRole.SPARK_WORKER);

			//delete the files we created the archive from
			deleteFiles(messages, fileWriter, platformMgr);

			//send the archive to the archive topic for handling
			FileInputMetadata archive = new FileInputMetadata(HadoopPurpose.ARCHIVE, getTenantID(), null, ROOT_PATH, filename, null, false);
			platformMgr.sendMessage(Topic.ARCHIVED_FILES, archive);

		} catch (Exception e) {
			throw new ProcessingException("error processing archive files for tenant " + tenantId, e);
		}

	}

	void deleteFiles(Iterable<String> files, FileWriter writer, PlatformMgr platformMgr) throws Exception {

		for (String strFile : files) {

			FileInputMetadata metadata = JSONUtils.objectFromString(strFile, FileInputMetadata.class);
			if (!writer.delete(metadata.getPurpose(), new Path(metadata.getPath()), metadata.getFilename())) {

				//if we didn't get what we expected, just log it
				platformMgr.handleError("failed to delete " + metadata.getFilename(), NodeRole.SPARK_WORKER);

			}

		}

	}

	/**
	 * Write an archive to the tenant's purpose
	 *
	 * @param tenantId
	 * @param fileReader
	 * @param fileWriter
	 * @param messages
	 * @throws Exception
   */
	String createArchiveFromIterables(String tenantId, FileReader fileReader, FileWriter fileWriter, Iterable<String> messages) throws Exception {

		Preconditions.checkArgument(fileReader != null, "File Reader for tenant " + tenantId + " is null in platform manager");
		Preconditions.checkArgument(fileWriter != null, "File Writer for tenant " + tenantId + " is null in platform manager");

		Path path = new Path("/");

		//get the latest time we'll archive
		Instant startOfCurrent = time.now().atOffset(ZoneOffset.UTC).toLocalDateTime().truncatedTo(archiveTimeUnit).toInstant(ZoneOffset.UTC);
		String fileName = getFileName(time, archiveTimeUnit);

		//get the output stream for the new file
		try (OutputStream outputStream = fileWriter.getOutputStream(HadoopPurpose.ARCHIVE, path, fileName)) {

			//create a tar.gz stream
			try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(outputStream)))) {

				for (String message : messages) {

					//get the metadata object
					FileInputMetadata metadata = JSONUtils.objectFromString(message, FileInputMetadata.class);

					//get info on the file
					FileStatus status = fileReader.getFileInfo(metadata);

					Instant fileTime = Instant.ofEpochMilli(status.getModificationTime());
					if (fileTime.isAfter(startOfCurrent)) {

						//file is not from previous time unit, re-add to the topic to be processed next time
						platformMgr.sendMessage(Topic.READY_FOR_ARCHIVE, metadata);
						continue;

					}

					//get the input stream for the file
					FileWrapper fileWrapper = fileReader.read(metadata.getPurpose(), new Path(metadata.getPath()), metadata.getFilename());

					//get the base path for the file, used in the archiver
					Path basePath = fileWriter.getPathResolver().getPurposeRoot(new Path("/"), metadata.getPurpose());
					Path fullPath = basePath.suffix(metadata.getPath() + "/" + metadata.getFilename());

					//build the tar entry
					TarArchiveEntry entry = new TarArchiveEntry(fullPath.toString());
					entry.setSize(fileWrapper.getSize());

					//add the entry
					tar.putArchiveEntry(entry);
					try {
						IOUtils.copy(fileWrapper.getInputStream(), tar);
					} finally {
						//always close it
						tar.closeArchiveEntry();
					}

				}

			}

		}

        return fileName;

	}

	/**
	 * Creates a file name of pattern yyyy_MM_dd.tar.gz - builds a file for the most recent completed time unit based on the current time
	 * @return the file name
   */
	public static String getFileName(TimeSource time, ChronoUnit timeUnit) {
		Instant startOfLast = null;
		switch (timeUnit) {
			case DAYS:
				startOfLast = LocalDateTime.ofInstant(time.now(), Statics.UTC_ZONE_ID).minusDays(1).toInstant(ZoneOffset.UTC);
				break;
			case HOURS:
				startOfLast = LocalDateTime.ofInstant(time.now(), Statics.UTC_ZONE_ID).minusDays(1).toInstant(ZoneOffset.UTC);
				break;
			default: throw new UnsupportedOperationException("unsupported time unit for archiving: " + timeUnit);
		}
		LocalDateTime date = LocalDateTime.ofInstant(startOfLast, Statics.UTC_ZONE_ID);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
		return date.format(formatter) + ".tar.gz";
	}

	/**
	 * @return the platform manager
	 */
	protected PlatformMgr getPlatformMgr() { return this.platformMgr; }
	private PlatformMgr platformMgr;

	//for now just archive by day
	public ChronoUnit getArchiveTimeUnit() { return archiveTimeUnit; }
	private ChronoUnit archiveTimeUnit = ChronoUnit.DAYS;

}
