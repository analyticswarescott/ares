package com.aw.common.hadoop.util;

import com.aw.common.hadoop.structure.HadoopPurpose;
import org.apache.hadoop.fs.Path;

/**
 * Object to hold information on Hadoop managed (HadoopPurpose) directories
 */
public class HadoopDirectoryInfo {

    public HadoopPurpose dirPurpose;
    public Path dirPath;

    public HadoopDirectoryInfo(HadoopPurpose purpose, Path path) {
      dirPath = path;
      dirPurpose = purpose;
    }

}
