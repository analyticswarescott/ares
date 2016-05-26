package com.aw.common.hadoop;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import com.aw.common.hadoop.structure.HadoopPurpose;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.TestPlatform;
import com.aw.common.hadoop.read.FileReader;
import com.aw.common.hadoop.read.FileWrapper;
import com.aw.common.system.structure.PathResolver;
import com.aw.common.system.structure.PathResolverSystem;
import com.aw.common.hadoop.structure.TargetPath;
import com.aw.common.hadoop.util.HadoopFileInfo;
import com.aw.common.hadoop.write.FileWriter;
import com.aw.common.inject.TestProvider;

import junit.framework.Assert;

public class FileWriterTest {

	private static final String DATA_TO_WRITE =
			"{\n" +
					"    \"data\": \"test\",\n" +
			"}";

	private static final String PLATFORM_DEF =
			"{\n" +
					"    \"display_name\": \"local platform\",\n" +
					"    \"description\": \"local platform\",\n" +
					"    \"author\": \"aw\",\n" +
					"    \"body_class\" : \"com.aw.platform.DefaultPlatform\",\n" +
					"    \"body\": {\n" +
					"        \"nodes\": {\n" +
					"            \"localhost\": {\n" +
					"                \"elasticsearch\": {\n" +
					"                    \"port\": 9200\n" +
					"                },\n" +
					"                \"kafka\": {\n" +
					"                    \"port\": 9092\n" +
					"                },\n" +
					"                \"hdfs\": {\n" +
					"                    \"port\": 9000\n" +
					"                },\n" +
					"                \"zookeeper\": {\n" +
					"                    \"port\": 2181\n" +
					"                },\n" +
					"                \"spark\": {},\n" +
					"                \"rest\": {\n" +
					"                    \"port\": 8080,\n" +
					"                    \"hdfs_cache_path\" : \"/hdfs_cache\"\n" +
					"                }\n" +
					"            }\n" +
					"        }\n" +
					"    }\n" +
					"}";






//disabling
//	@Test
	public void testFileWriteNormal() throws Exception {

		JSONObject data = new JSONObject(DATA_TO_WRITE);

		Path[] pathset = new Path[2];
		Path p1 = new Path(File.separatorChar +  "p1");
		pathset[0] = p1;
		Path p2 = new Path(File.separatorChar +  "p2");
		pathset[1] = p2;

		//File f = new File(".");

		//SYSTEM HIVE
		PathResolver pr = new PathResolverSystem();
		FileWriter sysFW = new FileWriter(new TestProvider<>(new TestPlatform()));


		sysFW.initialize(pr, pathset, new TestPlatform());
		FileStatus stat = sysFW.writeStringToFile(HadoopPurpose.CONFIG, new Path("/somestuff"), "foo.doc", DATA_TO_WRITE);

		HadoopFileInfo hfi = new HadoopFileInfo(stat);
		assertEquals("expect file length to match string bytes ", stat.getLen(), DATA_TO_WRITE.getBytes().length);


		FileReader reader = new FileReader(new TestProvider<>(new TestPlatform()));
		reader.initialize(pr, pathset, new TestPlatform());


		Map status = sysFW.getCacheStatus();
		Assert.assertEquals(" expect second cache path to not exist (cache position 1) ", -1, status.get(1));

		FileWrapper fw = reader.read(HadoopPurpose.CONFIG, new Path("/somestuff"), "foo.doc");
		String readString = fw.getAsString();
		assertEquals("expect read string to be the same as original ", DATA_TO_WRITE, readString);

		TargetPath targetPath = new TargetPath(p1, new TestPlatform());
		targetPath.delete();

	}

/*	@Test
	public void testFileWriteFailToCache() throws Exception {

		assertEquals("do test here", "a string", new JSONObject(DATA_TO_WRITE).toString());

	}

	@Test
	public void testFileWriterSyncCache() throws Exception {

		assertEquals("do test here", "a string", new JSONObject(DATA_TO_WRITE).toString());

	}*/


}
