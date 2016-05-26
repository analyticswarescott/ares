package com.aw.common.system.structure;


import org.apache.hadoop.fs.Path;

/**
 * Path resolver for System hive reads/writes
 */
public class PathResolverSystem extends AbstractPathResolver {

    public PathResolverSystem() {
        setHive();
    }


	@Override
    public void setHive() {
        _hive = Hive.SYSTEM;
    }

    @Override
    public Path resolvePurposeRoot(Path root, Purpose purpose) {

        String hp = _hive.getPath();
        String pp = purpose.getPath();

        String np = root + hp + pp;

        Path newPath = new Path(np);
        return newPath;
 /*       return root.suffix(_hive.getPath())
                .suffix(purpose.getPath());*/
    }




}
