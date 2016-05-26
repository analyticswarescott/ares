package com.aw.common.system.structure;

import com.aw.common.hadoop.exceptions.HadoopStructureException;
import com.aw.common.rest.security.TenantAware;
import org.apache.hadoop.fs.Path;

/**
 * Resolve a HadoopPurpose and root to a valid filesystem path
 *
 */
public abstract class AbstractPathResolver implements PathResolver, TenantAware {

    protected Hive _hive;

    public AbstractPathResolver() {
        setHive();

    }

    public Path getPurposeRoot(Path root, Purpose purpose) {
        if (!purpose.isValid(_hive)) {
            throw new HadoopStructureException(" Purpose " + purpose + " not valid for hive " + _hive);
        }
        return resolvePurposeRoot(root, purpose);
    }

	public Path getTenantRoot(Path root) {
		return new Path(root.toString() + Hive.TENANT.getPath() +  Path.SEPARATOR_CHAR + getTenantID());
	}

    public abstract void setHive();
    protected abstract Path resolvePurposeRoot(Path root, Purpose purpose);
}
