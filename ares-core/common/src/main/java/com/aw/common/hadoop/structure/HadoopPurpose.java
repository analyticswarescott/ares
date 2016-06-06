package com.aw.common.hadoop.structure;

import com.aw.common.system.FileInputMetadata;
import com.aw.common.system.structure.Hive;
import com.aw.common.system.structure.Purpose;

/**
 * Enum for purpose-driven managed directories on a file system under a dynamic root
 */
public enum HadoopPurpose implements Purpose{

    EVENTS("/events", new Hive[]{Hive.TENANT}),
    CONFIG("/config", new Hive[]{Hive.TENANT, Hive.SYSTEM}),
	PATCH("/patch", new Hive[]{Hive.SYSTEM}),
    LOGGING("/log", new Hive[]{Hive.SYSTEM}),
	INCIDENT("/incident", new Hive[]{Hive.TENANT}),
	ARCHIVE("/archive", new Hive[]{Hive.TENANT});

    HadoopPurpose(String path, Hive[] validFor) {

        _path = path;
        _validFor = validFor;
    }
    private String _path;
    private Hive[] _validFor;

    public String getPath() {
        return _path;
    }

    /**
     * Get the logical path
     *
     * @param metadata
     * @return
     */
    public String getPath(FileInputMetadata metadata) {
    	String subpath = metadata.getPath() + metadata.getFilename();
    	if (subpath.startsWith("/") || subpath.startsWith("\\")) {
    		subpath = subpath.substring(1);
    	}
		return _path + "/" + subpath;
    }

    public boolean isValid(Hive hive) {

        for (Hive h : _validFor) {

            if (hive == h) {
                return true;
            }
        }
        return false;
    }


}
