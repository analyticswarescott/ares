package com.aw.common.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.structure.TargetPath;
import com.aw.common.hadoop.util.HadoopFileInfo;
import com.aw.common.hadoop.util.HadoopUtil;
import com.aw.common.system.structure.Hive;
import com.aw.common.system.structure.PathResolver;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;
import com.aw.platform.PlatformUtils;
import com.aw.platform.roles.HdfsName;
import com.aw.platform.roles.Rest;

/**
 * Path resolution class extended by any readers and writers (read, write, cache sync)
 */
public abstract class FileAccessorBase {

	static final org.apache.log4j.Logger logger = Logger.getLogger(FileAccessorBase.class);

    public enum Status {
        DFS_UP,
        DFS_DOWN,
        DFS_UNKNOWN,
        UNKNOWN
    }

    private Provider<Platform> platformProvider;

    public FileAccessorBase(Provider<Platform> platform) {
    	this.platformProvider = platform;
	}

    protected TargetPath[] _paths;

    public PathResolver getPathResolver() { return _pathResolver; }
    protected PathResolver _pathResolver;

    protected Hive _hive;

    protected long _verifyTTL; //when to check again
    protected Status _status = Status.UNKNOWN;

    protected long _verifyInterval = 5000;

    public Status getStatus() {
        return _status;
    }

    public synchronized void checkStatus() throws Exception {
    	checkStatus(false);
    }
    public synchronized void checkStatus(boolean force) throws Exception {

    	//always check for updated status if we aren't up yet
    	if (_status != Status.DFS_UP) {
    		force = true;
    	}

    	//if not a forced check
    	if (!force) {

        	//check if we need to
    		if (_status != Status.DFS_UP && _verifyTTL != 0  &&  System.currentTimeMillis() >= _verifyTTL) {
	    		return;
	    	}

    	}

        Status oldStatus = Status.valueOf(_status.toString());

        Platform platform = platformProvider.get();

        //get primary path
        PlatformNode hdfs = platform.getNode(NodeRole.HDFS_NAME);
        PlatformNode rest = platform.getNode(NodeRole.REST);

        String hdfsURI = null;
        Path[] paths = null;

        if (hdfs != null) {

			//use nameservice/cluster name for HA, use host:port for local mode
			if (PlatformUtils.getHadoopNameNodes(platform).size() > 1) {
				hdfsURI = "hdfs://" + platform.getNode(NodeRole.HDFS_NAME)
					.getSetting(HdfsName.HA_CLUSTER_NAME)
					+ hdfs.getSetting(HdfsName.HDFS_ROOT_PATH);
			}
			else {
				hdfsURI = "hdfs://" + platform.getNode(NodeRole.HDFS_NAME).getHost()
					+ ":" +
					platform.getNode(NodeRole.HDFS_NAME).getSetting(HdfsName.PORT)
					+ hdfs.getSetting(HdfsName.HDFS_ROOT_PATH);
			}


			paths = new Path[1];

			//setup primary path
			Path tPri = new Path(hdfsURI);
			paths[0] = tPri;

		}
        else {
            //installMode -- set local as primary path -- HDFS node registration will re-initialize

            paths = new Path[1];
            String local_path = rest.getSetting(Rest.LOCAL_FILE_CACHE_PATH);
            Path pLocal = new Path(local_path);
            Path tSec = pLocal;
            paths[0] = tSec;

        }


        initialize(_pathResolver, paths, platform);
        reScanTargetPaths(platform);
        if (_status != Status.DFS_UP) {
            //set time to check again
            _verifyTTL = System.currentTimeMillis() + _verifyInterval;
        }
        else {
            _verifyTTL = 0;
            if (oldStatus != Status.DFS_UP) {
                sync(); //TODO: what to do with an error here
            }
        }

    }


    public Path getAbsolutePath(HadoopPurpose purpose, Path path, String fileName) throws IOException {

    	//path 0 is the primary path - if this fails, the call should fail
    	Path writePath = resolveFullPath(_paths[0], purpose, path, fileName);
    	return _paths[0].getFileSystem().resolvePath(writePath);

    }

    /**
     * Initialize using platform information
     */
    public synchronized void initialize(PathResolver resolver) throws Exception {

        _pathResolver = resolver;
        checkStatus(true);
    }

    /**
     * Overload is public so unit tests can explicitly
     * @param paths First path in the array will be considered the primary path, all others failovers
     */
    public void initialize(PathResolver resolver, Path[] paths, Platform platform) throws Exception {

        _pathResolver = resolver;

        _paths = new TargetPath[paths.length];
        int i = 0;
        for (Path p : paths) {
            TargetPath tp = new TargetPath(p, platform);
            if (i==0) {tp.set_isPrimary(true);}
            _paths[i] = tp;
            i++;
        }

    }


    public void reScanTargetPaths(Platform platform) throws Exception{

        _status = Status.DFS_UNKNOWN;
        for (TargetPath targetPath : _paths) {
            targetPath.scan(platform);
            if (targetPath.isDFS()) {
                if (targetPath.isAcessible()) {
                    _status = Status.DFS_UP;
                }
                else { _status = Status.DFS_DOWN;}
            }
        }

    }

    /**
     * Get a path to a specific path within the given purpose
     *
     * @param targetPath
     * @param purpose
     * @param detailPath
     * @return
     */
    public Path resolveFullPath(TargetPath targetPath, HadoopPurpose purpose, Path detailPath) {
        Path purposeRoot = _pathResolver.getPurposeRoot(targetPath.getPath(), purpose);
        return purposeRoot.suffix(detailPath.toString());
    }

    /**
     * Get a path to a particular file underneath detailPath
     *
     * @param targetPath
     * @param purpose
     * @param detailPath
     * @param fileName
     * @return
     */
    public Path resolveFullPath(TargetPath targetPath, HadoopPurpose purpose, Path detailPath, String fileName) {
    	return resolveFullPath(targetPath, purpose, detailPath).suffix(File.separatorChar + fileName);
    }

    /**
     * re-direct a relative path from a source to a new target to allow for local>>central sync
     * @param sourcePath
     * @param source
     * @param target
     * @return
     */
    public Path redirect(Path sourcePath, TargetPath source,  TargetPath target) {

        //TODO: implement path parsing for re-direction
        //for now assume the file:/ means + X to get slash-less relative path
        int offset = 5;
        if (SystemUtils.IS_OS_WINDOWS) {
            offset = 7; //due to the Drive letter and colon
        }

        String relativePart = sourcePath.toString().substring(source.getPath().toString().length() + offset);
        String newPath = target.getPath() + relativePart;
        return new Path(newPath);
    }

    public Path resolveDirectoryPath(TargetPath targetPath, HadoopPurpose purpose, Path detailPath) {

        Path purposeRoot = _pathResolver.getPurposeRoot(targetPath.getPath(), purpose);
        Path finalPath = purposeRoot.suffix(detailPath.toString());

        return finalPath;

    }


    /**
     * return file counts by target path index for status purposes
     * @return
     */
    public Map getCacheStatus() throws Exception {

        HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();

        int i = 0;
        for (TargetPath target : _paths) {
            try {
                String path = target.getPath().toString();
                Set<HadoopFileInfo> files = HadoopUtil.getFileList(target);
                ret.put(i, files.size());
            }
            catch (Exception ex) {
                ret.put(i, -1); //-1 code for error accessing FS
            }

            i++;
        }

        return ret;
    }


    public abstract  void sync() throws Exception;


}
