package com.aw.compute.streams.processor;
/*
package com.aw.compute.streams.processor.impl;

import com.aw.platform.PlatformSettingsProvider;
import com.aw.compute.streams.exceptions.ProcessingException;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.processor.framework.IterableProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.kafka.OffsetRange;
import org.codehaus.jettison.json.JSONObject;


import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;

public class HDFSProcessor implements IterableProcessor {
    private String hdfs_root_url;
    private String hdfs_conf_dir;
    private String write_dir;
    private String file_name_root;

    @Override
    public void process(String string, Iterable<String> messages) throws ProcessingException {

    }

    @Override
    public void init(JSONObject processor_data) throws ProcessorInitializationException {
        */
/*System.out.println("HDFSProcessor:OnInit");
        hdfs_root_url = PlatformSettingsProvider.getHDFSRoot();
        //hdfs_conf_dir = processor_data.get("hdfs_conf_dir").toString();
        //write_dir = processor_data.get("write_dir").toString();
        file_name_root = processor_data.get("file_name_root").toString();*//*


    }



*/
/*

    protected void OnHandleRDD(OffsetRange[] offsetRanges, JavaRDD<String> vals) {
        System.out.println("HDFSProcessor:OnHandleRDD");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");
        String writeSequence = dateFormatter.format(System.currentTimeMillis());
        String targetFileNameFull = hdfs_root_url + write_dir + "/" + file_name_root + writeSequence;
        String tempFileNameFull = targetFileNameFull + "_tmp";

        // trouble with this is resulting JSON has newlines making it impossible to read, obj-file fixes this
        //vals.saveAsTextFile(tempFileNameFull);
        vals.saveAsObjectFile(tempFileNameFull); //per Scott


        try {
            Configuration configuration = new Configuration();
            configuration.addResource(new Path(hdfs_conf_dir + File.separator + "core-site.xml"));
            configuration.addResource(new Path(hdfs_conf_dir + File.separator + "hdfs-site.xml"));
            //configuration.set("hadoop.job.ugi", "hduser");
            //2. Create an InputStream to read the data from local file
            //InputStream inputStream = new BufferedInputStream(new FileInputStream("/home/client/localsystem/file/path/sample.txt"));
            //3. Get the HDFS instance
            FileSystem hdfs = FileSystem.get(new URI(hdfs_root_url), configuration);
            FileUtil.copyMerge(hdfs, new Path(tempFileNameFull), hdfs, new Path(targetFileNameFull), true, hdfs.getConf(), null);
            FileUtil.fullyDelete(new File(tempFileNameFull));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

*//*

}
*/
