package com.aw.common.system.structure;

import org.apache.hadoop.fs.Path;

/**
 * Resolves the root directory for the current hive using the supplied root path and purpose
 */
public interface PathResolver {
    /**
     * Interface for resolving paths from abstract purpose to an absolute path
     *
     * @param root
     * @param purpose
     * @return
     */
    public Path getPurposeRoot(Path root, Purpose purpose);

	public Path getTenantRoot(Path root);

	public void setHive();

}
