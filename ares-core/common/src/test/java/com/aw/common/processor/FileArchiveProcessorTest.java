package com.aw.common.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.system.structure.Hive;
import com.aw.common.system.structure.PathResolverTenant;
import com.aw.common.util.TimeSource;
import com.aw.platform.PlatformMgr;

/**
 * on 4/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileArchiveProcessorTest {

	@Mock
	FileWriter writer;

	@Mock
	FileReader reader;

	@Mock
	PlatformMgr platformMgr;

	public void createArchiveFromIterables() throws Exception {

	}

	@Test
	public void process() throws Exception {

		SecurityUtil.setThreadSystemAccess();

		//set up platformMgr with our test writer/reader
		doReturn(writer).when(platformMgr).getTenantFileWriter();
		doReturn(reader).when(platformMgr).getTenantFileReader();

		FileArchiveProcessor fileArchiveProcessor = new FileArchiveProcessor(platformMgr, TimeSource.SYSTEM_TIME);

		long size = new File(getClass().getResource("bundle.json").getFile()).length();
		String bundleJson = IOUtils.toString(getClass().getResourceAsStream("bundle.json"));

		FileWrapper wrapper = mock(FileWrapper.class);
		doAnswer(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return getClass().getResourceAsStream("bundle.json");
			}
		}).when(wrapper).getInputStream();

		doReturn(size).when(wrapper).getSize();

		PathResolverTenant resolver = new PathResolverTenant(Hive.TENANT);
		doReturn(resolver).when(writer).getPathResolver();

		File output = File.createTempFile("test_" + getClass().getName() + "_", ".tar.gz");
		output.deleteOnExit();

		doReturn(new FileOutputStream(output)).when(writer).getOutputStream(any(HadoopPurpose.class), any(Path.class), any(String.class));

		FileStatus mockStatus = mock(FileStatus.class);
		doReturn(Instant.now().minus(Duration.ofHours(24)).toEpochMilli()).when(mockStatus).getModificationTime();
		doReturn(wrapper).when(reader).read(any(HadoopPurpose.class), any(Path.class), any(String.class));
		doReturn(mockStatus).when(reader).getFileInfo(any(FileInputMetadata.class));

		String tenantId = "0";
		Iterable<String> messages = buildFileInputMetadata(tenantId);

		fileArchiveProcessor.process(tenantId, messages);

		//verify we got the right input and output files

		//we should have read the 3 bundles
		verify(reader).read(HadoopPurpose.BUNDLE, new Path("/"), "bundle1.xml");
		verify(reader).read(HadoopPurpose.BUNDLE, new Path("/"), "bundle2.xml");
		verify(reader).read(HadoopPurpose.BUNDLE, new Path("/"), "bundle3.xml");

		//we should have written the file
		verify(writer).getOutputStream(HadoopPurpose.ARCHIVE, new Path("/"), FileArchiveProcessor.getFileName(TimeSource.SYSTEM_TIME, fileArchiveProcessor.getArchiveTimeUnit()));

		//verify the file has archived our data properly, there should be 3 bundles in the tar.gz
		try (TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(output)))) {

			ArchiveEntry entry = tar.getNextEntry();
			assertNotNull(entry);
			assertEquals("tenant/0/bundle/bundle1.xml", entry.getName());
			String data = IOUtils.toString(tar);
			assertEquals(bundleJson, data);
			assertEquals(size, entry.getSize());

			entry = tar.getNextEntry();
			assertNotNull(entry);
			assertEquals("tenant/0/bundle/bundle2.xml", entry.getName());
			data = IOUtils.toString(tar);
			assertEquals(bundleJson, data);
			assertEquals(size, entry.getSize());

			entry = tar.getNextEntry();
			assertNotNull(entry);
			assertEquals("tenant/0/bundle/bundle3.xml", entry.getName());
			data = IOUtils.toString(tar);
			assertEquals(bundleJson, data);
			assertEquals(size, entry.getSize());

		}

		//make sure the bundles were deleted
		verify(writer).delete(HadoopPurpose.BUNDLE, new Path("/"), "bundle1.xml");
		verify(writer).delete(HadoopPurpose.BUNDLE, new Path("/"), "bundle2.xml");
		verify(writer).delete(HadoopPurpose.BUNDLE, new Path("/"), "bundle3.xml");

	}

	private List<String> buildFileInputMetadata(String tenantID) throws JSONException {

		FileInputMetadata fileInputMetadata = null;
		List<String> messages = new ArrayList<>();
		for(int i = 1; i <= 3; i++) {

			fileInputMetadata = new FileInputMetadata(
				HadoopPurpose.BUNDLE,
				tenantID,
				"machine" + i,
				"/",
				"bundle" + i + ".xml",
				UUID.randomUUID().toString(),
				false
			);

			messages.add(fileInputMetadata.toString());

		}
		return messages;

	}

}