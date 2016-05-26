package com.aw.common.hadoop.write;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.inject.Provider;

import com.aw.common.system.structure.PathResolverTenant;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;

import com.aw.common.hadoop.FileAccessorBase;
import com.aw.common.hadoop.exceptions.FileWriteException;
import com.aw.common.hadoop.exceptions.LocalCacheFailException;
import com.aw.common.system.structure.PathResolver;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.structure.TargetPath;
import com.aw.common.hadoop.util.HadoopFileInfo;
import com.aw.common.hadoop.util.HadoopUtil;
import com.aw.platform.Platform;


/**
 * Base class for writing to a file System.  Subclasses decide on hive and runtime path determination (e.g. dynamic tenant)
 */
public class FileWriter  extends FileAccessorBase {
    public static final Logger logger = Logger.getLogger(FileWriter.class);

    public FileWriter(Provider<Platform> platform) throws Exception {
    	super(platform);
    }

    @Override
    public void initialize(PathResolver resolver) throws Exception {
        super.initialize(resolver);

        //when a writer initializes, check for local cache to sync up

        sync();
    }

    /**
     * Write string to file
     *
     * @param purpose
     * @param detailPath
     * @param fileName
     * @param data
     * @return
     * @throws Exception
     */
    public FileStatus writeStringToFile(HadoopPurpose purpose, Path detailPath, String fileName, String data) throws Exception {
        InputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        return writeStreamToFile(purpose, detailPath, fileName, in);
    }

	public void deleteTenantRoot(String tenantID) throws Exception {
		TargetPath t = _paths[0];
		Path tenantRoot = _pathResolver.getTenantRoot(t.getPath());
		deleteDir(tenantRoot);
	}
	public void createTenantRoot(String tenantID) throws Exception {
		TargetPath t = _paths[0];
		Path tenantRoot = _pathResolver.getTenantRoot(t.getPath());
		createDir(tenantRoot);
	}

	protected void deleteDir(Path path) throws Exception{

		TargetPath t = _paths[0];
		t.getFileSystem().delete(path, true);


	}

	protected void createDir(Path path) throws Exception{
		TargetPath t = _paths[0];
		t.getFileSystem().mkdirs(path);
	}

	/**
	 * delete one file
	 *
	 * @param purpose
	 * @param basePath
	 * @param filePrefix
	 * @return
	 * @throws Exception
	 */
    public boolean delete(HadoopPurpose purpose, Path basePath, String filename) throws Exception {

        checkStatus();

        boolean ret = false;

        try {

            if (!_paths[0].isAcessible()) {
                logger.warn(" skipping target path until next status check :" + _paths[0].getPath().toString() );
            }

            else {

                //get the full path
                Path root = resolveFullPath(_paths[0], purpose, basePath);

                //delete the file
                ret = _paths[0].getFileSystem().delete(root.suffix(File.separatorChar + filename), false);

            }

        } catch (Exception ex) {
    		throw new Exception("error while trying to delete purpose=" + purpose + ", basePath=" + basePath + ", filename=" + filename, ex);
        }

        return ret;

    }

    /**
     * Try to delete all files matching the filename prefix in the given path
     *
     * @param purpose The purpose we are deleting
     * @param basePath The base path
     * @param filePrefix The filename prefix
     * @return
     * @throws Exception
     */
    public int deleteAll(HadoopPurpose purpose, Path basePath, String filePrefix) throws Exception {

        checkStatus();

        //TODO: only one path - the read HDFS cluster

        //keep an exception if one occurs
        Exception lastE = null;

        //keep track of how many we've deleted
        int deleted = 0;

        //get a list of all files
    	for (TargetPath t : _paths) {

            try {
                //don't keep retrying paths that are not available

                //TODO: evaluate this -- upside is no failure wait on every write -- downside is tests
                if (!t.isAcessible()) {
                    logger.warn(" skipping target path until next status check :" + t.getPath().toString() );
                    continue;
                }

                //get the root path for our attempt
                Path root = resolveFullPath(t, purpose, basePath);

                //list the files that match the prefix
                FileStatus[] files = t.getFileSystem().listStatus(root, new PrefixFilter(filePrefix));

                for (FileStatus file : files) {
                	t.getFileSystem().delete(file.getPath(), false);
                	deleted++;
                }

                break;
            } catch (Exception ex) {
                if (t.isDFS()) {
                    _status = Status.DFS_DOWN;
                }
                logger.error(" failed to write to target path " + t.getPath().toString() + " : " + ex.getMessage());
                lastE = ex;
            }

    	}

    	//throw an exception here if we encountered errors
    	if (lastE != null) {
    		throw new Exception("error while trying to delete purpose=" + purpose + ", basePath=" + basePath + ", prefix=" + filePrefix);
    	}

    	//otherwise tell the caller how many files were actually deleted
    	return deleted;

    }

    /**
     * Get the output stream directly for a path. The output stream is then the reponsibility of the caller for closing.
     *
     * @param purpose The purpose for the path
     * @param detailPath The path
     * @param fileName The filename
     * @return The output stream
     * @throws Exception If anything goes wrong
     */
    public OutputStream getOutputStream(HadoopPurpose purpose, Path detailPath, String fileName) throws Exception {

    	checkStatus();

        int successCount = 0;
        FileStatus stat = null;
        Exception e = null;
        FSDataOutputStream ret = null;

        for (TargetPath t : _paths) {

            try {

                Path writePath = resolveFullPath(t, purpose, detailPath, fileName);
			    FileSystem fs = t.getFileSystem();
			    FSDataOutputStream out = fs.create(writePath);
			    ret = out;

			    break;

            } catch (Exception ex) {
                if (t.isDFS()) {
                    _status = Status.DFS_DOWN;
                }
                logger.error(" failed to write to target path " + t.getPath().toString() + " : " + ex.getMessage());
                e = ex;
            }
        }

        return ret;

    }

    /**
     * Write stream to file
     *
     * @param purpose
     * @param detailPath
     * @param fileName
     * @param in
     * @return
     * @throws Exception
     */
    public FileStatus writeStreamToFile(HadoopPurpose purpose, Path detailPath, String fileName, InputStream in) throws Exception {
    	checkStatus();

        int successCount = 0;
        FileStatus stat = null;
        Exception e = null;
        for (TargetPath t : _paths) {

        	if (t == null) {
        		continue;
        	}

            try {
                //don't keep retrying paths that are not available

                //TODO: evaluate this -- upside is no failure wait on every write -- downside is tests
                if (!t.isAcessible()) {
                    logger.warn(" skipping target path until next status check :" + t.getPath().toString() );
                    continue;
                }


				//for tenant writes, tenant root must pre-exist (i.e. be build at provision time
				if (_pathResolver instanceof PathResolverTenant) {
					Path tenantRoot = _pathResolver.getTenantRoot(t.getPath());
					if (!t.getFileSystem().exists(tenantRoot)) {
						throw new FileWriteException(" tenant root does not exist");
					}
				}

                Path writePath = resolveFullPath(t, purpose, detailPath, fileName);
                stat = writeStreamToTarget(t, writePath, in);

                //TODO: need to verify (i.e. byte length vs. fs.len?
                successCount++;
                break;
            } catch (Exception ex) {
                if (t != null && t.isDFS()) {
                    _status = Status.DFS_DOWN;
                }
                logger.error(" failed to write to target path " + t.getPath().toString() + " : " + ex.getMessage());
                e = ex;
            }

        }

        if (successCount != 1) {
            throw new FileWriteException(" unable to write file to any available destination, chained last exception", e);
        }

        return stat;
    }

    /**
     * write a stream to a specified target path
     *
     * @param targetPath
     * @param writePath
     * @param in
     * @return
     * @throws Exception
     */
    protected FileStatus writeStreamToTarget(TargetPath targetPath, Path writePath, InputStream in) throws Exception {

        FileSystem fs = targetPath.getFileSystem();
        FSDataOutputStream out = fs.create(writePath);
        int bytes = write(in, out.getWrappedStream());
        FileStatus stat = fs.getFileStatus(writePath);

        return stat;


    }


    //should work for local or remote write
    private int write(InputStream in, OutputStream out) throws Exception {
        int bytes = IOUtils.copy(in, out);
        out.flush();
        out.close();
        return bytes;
    }

    @Override
    public synchronized void sync() throws Exception {

        //reScanTargetPaths();
        //if primary path is a DFS and is accessible, then sync
        if (_paths[0].isAcessible() && _paths[0].isDFS()) {
            //Main HDFS is up, so we can sync anything in secondary paths
            logger.info(" performing sync on local caches ");
            for (int i = 1; i < _paths.length; i++) {
                syncLocalCache(i);
            }

        }
        else {logger.error("Sync called with DFS not accessible ");}

    }

    private synchronized void syncLocalCache(int secondaryPathIndex) throws Exception {
        //attempt to sync a local cache to HDFS when it is determined to be online

        //get recursive list of files from secondary
        Set<HadoopFileInfo> files = HadoopUtil.getFileList(_paths[secondaryPathIndex]);

        FileSystem sourceFS = _paths[secondaryPathIndex].getFileSystem();
        FileSystem targetFS = _paths[0].getFileSystem();

        for (HadoopFileInfo hfi : files) {

            InputStream in = sourceFS.open(hfi.getPath());
            Path writePath = redirect(hfi.getPath(), _paths[secondaryPathIndex], _paths[0]);
            writeStreamToTarget(_paths[0], writePath, in);

            in.close();

            boolean deleteOK = sourceFS.delete(hfi.getPath(), false);
            if (!deleteOK) {
                throw new LocalCacheFailException(" delete failed (return false no exception) from local cache ");
            }

        }


    }

    //utility methods around part file names
    private static final String PART = "-part-";

    /**
     * Get the filename of a file part
     *
     * @param filename The file in parts
     * @param part The part
     * @return The full filename
     */
    public static String toPartName(String filename, int part) {
    	return toPartPrefix(filename) + part;
    }

    /**
     * Get the filename for a part of this file, minus the part number
     *
     * @param filename
     * @return
     */
    public static String toPartPrefix(String filename) {
    	return filename + PART;
    }

    /**
     * Filter paths based on a prefix
     *
     *
     *
     */
    private class PrefixFilter implements PathFilter {

    	public PrefixFilter(String prefix) {
    		m_prefix = prefix;
		}

    	@Override
    	public boolean accept(Path path) {
    		return path.getName().startsWith(m_prefix);
    	}

    	//the prefix we are looking for
    	private String m_prefix;

    }

}
