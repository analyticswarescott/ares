package com.aw.common.system.structure;

/**
 * Top level child directly under the root
 */
public enum Hive {

    TENANT("/tenant", new PathResolverTenant()),
    SYSTEM("/system", new PathResolverSystem());

     Hive(String path, PathResolver pr) {
		_pr = pr;
         _path = path;
     }
    private String _path;
	private PathResolver _pr;

    public String getPath() {
        return _path;
    }

	public PathResolver getPathResolver() {
		_pr.setHive();
		return _pr;
	}


}
