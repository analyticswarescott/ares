package com.aw;


import com.aw.common.system.EnvironmentSettings;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;

import java.io.File;

/**
 * Create local HDFS cluster for testing
 */
public class MiniHadoopServiceWrapper {

    private int port = 0;
    private MiniDFSCluster cluster;

    public static final String LOCAL_CACHE_DIR = "/data/hdfs_cache";

    public MiniHadoopServiceWrapper(int port) {
        this.port = port;
    }

    public boolean start() throws Exception {
        Configuration conf = new Configuration();
		// Disable starting the jersey container - creates conflicts with the jax-rs v2 jars
		conf.setBoolean(DFSConfigKeys.DFS_WEBHDFS_ENABLED_KEY, false);
		conf.set("dfs.namenode.servicerpc-bind-host", "0.0.0.0");

        File baseDir = new File(EnvironmentSettings.getDgData() + File.separator + "temp_hdfs");
        FileUtil.fullyDelete(baseDir);

        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        if (port != 0) {
            builder.nameNodePort(port);
        }

        cluster = builder.build();
        return true;
    }

    public boolean isRunning() {
    	return cluster.isClusterUp();
    }

	public void stop() {
		cluster.shutdown();
	}
}
