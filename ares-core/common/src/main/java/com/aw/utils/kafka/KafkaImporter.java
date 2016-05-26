package com.aw.utils.kafka;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaImporter implements Runnable {

    public static Logger logger = Logger.getLogger(KafkaImporter.class);

    private String sourcePath;
    private String topic;

    private ProducerConfig kafkaProducerConfig;

    public KafkaImporter(
            String topic,
            String kafkaMetadataBrokerList,
            String sourcePath
    ) {
        this.topic = topic;
        this.sourcePath = sourcePath;

        Properties props = new Properties();
        props.put("metadata.broker.list", kafkaMetadataBrokerList);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        kafkaProducerConfig = new ProducerConfig(props);
    }

    public void sendMessage(String topic, String key, String message) {
        Producer<String, String> kafkaClient = new Producer<>(kafkaProducerConfig);
        KeyedMessage<String, String> data = new KeyedMessage<>(topic, key, message);
        kafkaClient.send(data);
    }

    public void doImport() throws Exception {
        File sourceFile = new File(sourcePath);
        Producer<String, String> kafkaClient = new Producer<>(kafkaProducerConfig);

        logger.info("Processing " + sourcePath + ".");

        if ( isZipFile(sourceFile ) ) {
            // process as zip file
            ZipFile zipFile = new ZipFile(new File(sourcePath));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                process(topic, zipFile, entry, kafkaClient);
            }
            zipFile.close();
        } else {
            // process as raw json
            InputStream fileInputStream = new FileInputStream(sourceFile);
            process(kafkaClient, topic, fileInputStream);
        }

        kafkaClient.close();
        logger.info("Finished importing " + sourcePath + ".");
    }

    private void process(String topic, ZipFile zipFile, ZipEntry entry, Producer<String, String> kafkaClient) throws Exception {
        String name = entry.getName();

        String targetBundle = System.getenv("TARGET_BUNDLE");
        if (targetBundle != null && !new File(name).getName().equals(targetBundle)) {
            logger.info("Skipping " + name + ".");
            return;
        }

        logger.info("Processing " + name + "..." + ", for topic: " + topic);

        InputStream fileStream = zipFile.getInputStream(entry);
        process(kafkaClient, topic, fileStream);

        fileStream.close();
    }

    private void process(Producer<String, String> kafkaClient, String topic, InputStream fileStream)
            throws IOException, JSONException {
        Random rnd = new Random();
        boolean lineEndingDelimited = "true".equals(System.getenv("LINE_ENDING_DELIMITED"));
        boolean randomizeKeys = "true".equals(System.getenv("RANDOMIZE_KEYS"));
        String tid = null;
        if (topic.contains("_")) {
            tid = topic.substring(0, topic.indexOf("_"));
        }

        if (!lineEndingDelimited) {
            // in the case of sending a bundle, send the entire thing at once
            String entireDocument = IOUtils.toString(fileStream);

            if ( randomizeKeys ) {
                //TODO: improve tenant ID replacement handling for continuous testing use case
                if (tid != null) {
                    entireDocument = entireDocument.replace("\"tid\" : \"1\"", "\"tid\" : \"" + tid + "\"" );
                }

                JSONObject lj = new JSONObject(entireDocument);
                JSONArray uads = (JSONArray) lj.get("uad");

                for (int x=0; x<uads.length(); x++) {
                	Object uad = uads.get(x);
                    JSONObject uadj = (JSONObject) uad;
                    uadj.put("medid", UUID.randomUUID().toString());
                }
                String key = tid != null ? tid : ("192.168.2." + rnd.nextInt(255));
                KeyedMessage<String, String> data = new KeyedMessage<>(topic, key, lj.toString());
                kafkaClient.send(data);
            } else {
                String key = tid != null ? tid : ("192.168.2." + rnd.nextInt(255));
                KeyedMessage<String, String> data = new KeyedMessage<>(topic, key, entireDocument);
                kafkaClient.send(data);
            }

            logger.info("... done. Sent bundle to kafka.");
        } else {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileStream));
            String line;

            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String key = tid != null ? tid : ("192.168.2." + rnd.nextInt(255));
                KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, key, line);
                kafkaClient.send(data);
                count++;
            }
            logger.info("... done. Sent " + count + " entries to kafka.");
            bufferedReader.close();
        }
    }

    private boolean isZipFile(File file) throws IOException {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
        if (file.length() < 4) {
            return false;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        return test == 0x504b0304;
    }

    public static void main(String[] s) throws Exception {
        String topic = s[0];
        String kafkaMetadataBrokerList = s[1];
        String sourcePath = s[2];



        KafkaImporter importer = new KafkaImporter(topic, kafkaMetadataBrokerList, sourcePath);
        importer.doImport();
    }

    @Override
    public void run() {

        try {
            doImport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
