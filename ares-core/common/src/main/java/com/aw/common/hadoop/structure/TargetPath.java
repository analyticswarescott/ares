package com.aw.common.hadoop.structure;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.HdfsName;

/**
 * Wrapper for a target file system (HDFS or local)
 */
public class TargetPath {
    public static final Logger logger = LoggerFactory.getLogger(TargetPath.class);
    protected FileSystem _fs;
    protected boolean _primary;
    protected boolean _accessible;
    protected Path _path;

    private boolean scanned = false;

    public void set_isPrimary(boolean b) {
        _primary = b;
    }

    public boolean isPrimary() {
        return _primary;
    }

    public void set_isAccessible(boolean b) {
        _accessible = b;
    }

    public boolean isAcessible() {
        return _accessible;
    }

    public FileSystem getFileSystem() {
        return _fs;
    }

    public Path getPath()
    {

        return _path;
    }

    /**
     * for system-tests test purposes
     */
    public boolean delete() throws Exception {
        return _fs.delete(_path, true);
    }

    public void scan(Platform platform) throws Exception{
        if (isDFS()) {
            _fs = new DistributedFileSystem();

            //TODO: handle configuration


			Configuration conf = new Configuration(false);

			String clusterName = PlatformUtils.getHadoopClusterName(platform);

			conf.set("fs.defaultFS",_path.toString());
			conf.set("fs.default.name", conf.get("fs.defaultFS"));
			conf.set("dfs.nameservices", clusterName);
			conf.set("dfs.ha.namenodes." + clusterName,
				PlatformUtils.getHadoopNameNodeList(platform));

			for (PlatformNode nn : PlatformUtils.getHadoopNameNodes(platform)) {
				conf.set("dfs.namenode.rpc-address." + clusterName + "." + nn.getHost(),
					nn.getHost() + ":" + nn.getSettingInt(HdfsName.PORT));
			}

			if (PlatformUtils.getHadoopNameNodes(platform).size() >1) {
				conf.set("dfs.client.failover.proxy.provider." + clusterName
					, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
			}


			Iterator<Map.Entry<String, String>> i = conf.iterator();
			while (i.hasNext()) {
				Map.Entry e = i.next();
				logger.debug(e.getKey() + ":" + e.getValue());
			}

			//_fs.initialize(_path.toUri(), new Configuration()); //before HA
			//_fs  =  FileSystem.get(URI.create(conf.get("fs.defaultFS")), conf);
			_fs = FileSystem.newInstance(URI.create(conf.get("fs.defaultFS")), conf);


        }
        else {
            //TODO: improve handling of a relative path for local cache
            if (!scanned) {
                //try DG_HOME
                Path root = new Path(EnvironmentSettings.fetch(EnvironmentSettings.Setting.ARES_HOME));
                Path finalPath = new Path(root.toString() + _path.toString());
                _path = finalPath;
                _fs = finalPath.getFileSystem(new Configuration());
                _fs.mkdirs(finalPath);
                scanned = true;
            }
        }

        //now mark if accessible or not
        try {
            boolean b = _fs.exists(_path);
            set_isAccessible(b);

        }
        catch (Exception ex) {
            set_isAccessible(false);
            logger.error(" FS unavailable at path "  + _path + " due to :" + ex.getMessage());
        }



    }

    public TargetPath(Path path, Platform platform) throws Exception {
        _path = path;
        scan(platform);
    }

    public boolean isDFS() {
        if (_path.toString().startsWith("hdfs://")) {
            return true;
        }
        else {return false;}
    }

    public static File walkToRoot(File start, String rootDir) {
        Path p = new Path(start.getPath());
        String name = p.getName();
        if (name.equals(rootDir)) {
            return start;
        }
        else {
            File parent = start.getParentFile();
            return walkToRoot(parent, rootDir);
        }

    }



}
