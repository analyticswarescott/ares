package com.aw.common.hadoop;

import com.aw.common.system.structure.PathResolver;
import org.apache.hadoop.fs.Path;

/**
 * Base class for readers and writers to Hadoop and backup local file systems
 */
public interface FileAccessor {

    /**
     * Use this method to initialize a reader or writer using Platform settings for file paths
     * @param resolver
     * @throws Exception
     */
    void initialize(PathResolver resolver) throws Exception;

    /**
     * This method can be used to initialize an accessor without a running Platform
     * @param resolver
     * @param root
     * @param paths
     * @throws Exception
     */
    void initialize(PathResolver resolver, Path root,  Path[] paths) throws Exception;
}
