package com.aw.common.system.structure;

import org.apache.hadoop.fs.Path;

/**
 * Path resolver for Tenant hive reads/writes
 */
public class PathResolverTenant extends AbstractPathResolver  {

    public PathResolverTenant() {
		super();
    }

	public PathResolverTenant(Hive h) {
		_hive = h;
	}

    @Override
    public void setHive() {
        _hive = Hive.TENANT;
    }

    @Override
    public Path resolvePurposeRoot(Path root, Purpose purpose) {


        return new Path(root.toString() + _hive.getPath() +  Path.SEPARATOR_CHAR + getTenantID() + Path.SEPARATOR_CHAR + purpose.getPath().toString());

    }



}
