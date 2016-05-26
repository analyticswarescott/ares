package com.aw.common.zookeeper.structure;

import com.aw.common.system.structure.Hive;
import com.aw.common.system.structure.Purpose;

/**
 * Enum for purpose-driven managed directories on a file system under a dynamic root
 */
public enum ZkPurpose implements Purpose {

	LOCKING("/locks", new Hive[]{Hive.TENANT}),
	OP_SEQUENCE("/op_sequence", new Hive[]{Hive.TENANT}),
	TASK_DATA("/task/data", new Hive[]{Hive.TENANT, Hive.TENANT}),
	RUNNING_TASK("/task/running", new Hive[]{ Hive.TENANT}),
	OFFSETS("/offsets", new Hive[]{Hive.TENANT}),
    CLUSTER("/cluster", new Hive[]{Hive.SYSTEM}),
	UPGRADE("/upgrade", new Hive[]{Hive.SYSTEM});



    ZkPurpose(String path, Hive[] validFor) {

        _path = path;
        _validFor = validFor;
    }
    private String _path;
    private Hive[] _validFor;

    public String getPath() {
        return _path;
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
