package com.aw.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.BaseIntegrationTest;
import com.aw.TestDependencies;
import com.aw.common.util.AWFileUtils;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.Kafka;
import com.aw.utils.kafka.KafkaImporter;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;

public class KafkaImporterTest extends BaseIntegrationTest {

    @Override
    protected boolean usesSpark() {
        return false;
    }

    @Override
    protected boolean startsSpark() {
        return false;
    }

    @Override
    protected boolean usesElasticsearch() {return false;}

    @Override
    protected boolean usesKafka() {return true;}


    private String createTopic(String topic) throws Exception {
        topic = topic != null ? topic : UUID.randomUUID().toString();
        ZkClient zk = new ZkClient(PlatformUtils.getZKQuorum(TestDependencies.getPlatform().get()), 5000, 5000, ZKStringSerializer$.MODULE$);
        AdminUtils.createTopic(zk, topic, 1, 1, new Properties());

        return topic;
    }

    @Test
    public void test() throws Exception {

    	importTest();
    	importTestRandomizedKeys();
    	importTestLineEndingDelimited();
    	importTestTenantTopic();

    }

    public void importTest() throws Exception {
        doImportTest(null);
    }

    public void importTestRandomizedKeys() throws Exception {
        customizeEnvironment("RANDOMIZE_KEYS", "true");
        doImportTest(null);
    }

    public void importTestLineEndingDelimited() throws Exception {
		customizeEnvironment("LINE_ENDING_DELIMITED", "true");
        doImportTest(null);
    }

    public void importTestTenantTopic() throws Exception {
		customizeEnvironment("LINE_ENDING_DELIMITED", "true");
        doImportTest("dlp_1");
    }

    private void doImportTest(String topicName) throws Exception {

        String topic = createTopic(topicName);
        String brokerList = TestDependencies.getPlatform().get().getNode(NodeRole.KAFKA).getHost() +
                ":" + TestDependencies.getPlatform().get().getNode(NodeRole.KAFKA).getSettingInt(Kafka.PORT);

        JSONObject topicInfo = new JSONObject();

        JSONArray uads = new JSONArray();
        JSONObject uad = new JSONObject();
        uad.put(UUID.randomUUID().toString(), UUID.randomUUID().toString() );
        uads.put( uad );
        topicInfo.put("uad", uads);
        topicInfo.put("medid", UUID.randomUUID().toString());
        topicInfo.put("tid", "1");
        topicInfo.put(UUID.randomUUID().toString(), UUID.randomUUID());

        File importTestDir = Files.createTempDirectory("importTestDir").toFile();

        File sourceFile = new File(importTestDir, topic);
        AWFileUtils.writeJSONToFile(topicInfo, sourceFile);

        KafkaImporter importer = new KafkaImporter( topic, brokerList, sourceFile.getAbsolutePath() );

        // send a message to kafka to the specified topic
        importer.sendMessage( topic, UUID.randomUUID().toString(), UUID.randomUUID().toString() );

        // run an import from the topic info source file
        importer.doImport();

        // create a zip file from the import test dir

        File sourceFileZip = new File(testDir, topic + ".zip");
        addFilesToZip(importTestDir, sourceFileZip);

        // import the zip file
        KafkaImporter importerZip = new KafkaImporter( topic, brokerList, sourceFileZip.getAbsolutePath() );
        importerZip.doImport();
        importerZip.run();

        // import the zip file via the main method
        KafkaImporter.main(new String[] { topic, brokerList, sourceFileZip.getAbsolutePath() });
    }

    private void addFilesToZip(File source, File destination) throws IOException, ArchiveException {
        OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
        Collection<File> fileList = FileUtils.listFiles(source, null, true);

        for (File file : fileList) {
            String entryName = getEntryName(source, file);
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            archive.putArchiveEntry(entry);
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        }

        archive.finish();
        archiveStream.close();
    }

    private String getEntryName(File source, File file) throws IOException {
        int index = source.getAbsolutePath().length() + 1;
        String path = file.getCanonicalPath();

        return path.substring(index);
    }
}
