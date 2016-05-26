package com.aw.common.hadoop.structure;

import com.aw.common.hadoop.util.HadoopDirectoryInfo;
import com.aw.common.hadoop.exceptions.HadoopStructureException;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


import java.util.ArrayList;

/**
 * Created by scott on 05/11/15.
 */
public class Validator {


    public static ArrayList<HadoopDirectoryInfo> getPurposeDirs(FileSystem fs, Path root) throws Exception {


        ArrayList<HadoopDirectoryInfo> ret = new ArrayList<HadoopDirectoryInfo>();

        FileStatus[] status = fs.listStatus(root);
        for (int i = 0; i < status.length; i++) {
            FileStatus stat = status[i];
            if (stat.isDirectory()) {
                String dirName = stat.getPath().getName();
                if (HadoopPurpose.valueOf(dirName.toUpperCase()) == null) {
                    throw new HadoopStructureException(" Invalid directory name at this level of file system " + dirName);
                }
                else {
                    HadoopPurpose p = HadoopPurpose.valueOf(dirName.toUpperCase());
                    ret.add(new HadoopDirectoryInfo(p,stat.getPath()));
                }

            } else {

                throw new HadoopStructureException(" File found where only valid purpose directories expected ");

            }
        }

        return ret;
    }

}
