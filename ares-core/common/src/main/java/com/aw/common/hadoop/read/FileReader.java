package com.aw.common.hadoop.read;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.aw.common.hadoop.FileAccessorBase;
import com.aw.common.hadoop.exceptions.FileReadException;
import com.aw.common.hadoop.structure.HadoopPurpose;
import com.aw.common.hadoop.structure.TargetPath;
import com.aw.common.system.FileInputMetadata;
import com.aw.platform.Platform;

/**
 * Class to read files from HDFS
 */
public class FileReader extends FileAccessorBase {

	static final Logger logger = Logger.getLogger(FileReader.class);

	@Inject
	public FileReader(Provider<Platform> platform) {
		super(platform);
	}

	@com.google.inject.Inject
	public FileReader(com.google.inject.Provider<Platform> platform) {
		super(platform);
	}

	public FileStatus getFileInfo(FileInputMetadata metadata) throws IOException {

		Path fullPath = resolveFullPath(_paths[0], metadata.getPurpose(), new Path(metadata.getPath()), metadata.getFilename());
		return _paths[0].getFileSystem().getFileStatus(fullPath);

	}

	public boolean exists(HadoopPurpose purpose, Path path, String fileName) throws Exception {

		check(purpose, path, fileName);

		for (int x=0; x<_paths.length; x++) {

			//if we find the file, return trueS
			logger.info("checking for existence of " + purpose + "/" + path + "/" + fileName + " (" + x + ")");
			Path fullPath = resolveFullPath(_paths[x], purpose, path, fileName);
			if (_paths[x].getFileSystem().exists(fullPath)) {
				FileStatus status = _paths[x].getFileSystem().getFileStatus(fullPath);
				return true;
			}

		}

		//we didn't find the file
		return false;

	}

    public FileWrapper read(HadoopPurpose purpose, Path detailPath, String fileName) throws Exception {

    	check(purpose, detailPath, fileName);

    	InputStream in = null;
    	Exception firstE = null; //remember last exception for bubbling up
    	int index = 0; //remember which path we got it from
    	long size = 0L;

    	for (index=0; index<_paths.length; index++) {

    		//get the path we will try
    		TargetPath path = _paths[index];

    		try {

                //open file from path
                Path readPath = resolveFullPath(path, purpose, detailPath, fileName);
                in = path.getFileSystem().open(readPath);
                size = path.getFileSystem().getFileStatus(readPath).getLen();

    		} catch (Exception e) {
    			if (firstE == null) {
    				firstE = e;
    			}
    		}

    	}

    	if (in == null) {
    		throw new Exception("could not read file purpose=" + purpose + " path=" + detailPath + " filename=" + fileName, firstE);
    	}

        FileWrapper fw = new FileWrapper();
        fw.setFullyAvailable(index == 0); //file is fully available if the hdfs cluster (always index 0) had the file
        fw.setLastException(firstE); //in case there were errors, include the first exception here
        fw.setInputStream(in);
        fw.setSize(size);

        return fw;

    }

    private void check(HadoopPurpose purpose, Path detailPath, String filename) throws Exception {

    	if (_paths == null) {
    		throw new Exception("no available paths to read purpose=" + purpose + " path=" + detailPath + " file=" + filename);
    	}

        if (getStatus() != Status.DFS_UP) {
            throw new FileReadException(" no DFS path available for read. Primary path is currently " + _paths[0].getPath().toString());
        }

    }


    @Override
    public void sync() throws Exception {
        //file reader does not sync
    }
}
