package com.aw.common.hadoop.util;


import com.aw.common.hadoop.structure.TargetPath;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.util.TreeSet;

public class HadoopUtil {
    public static final Logger logger = Logger.getLogger(HadoopUtil.class);




    public HadoopUtil() {

    }


    public static TreeSet<HadoopFileInfo> getFileList(TargetPath targetPath) throws Exception {

        TreeSet<HadoopFileInfo> existingList = new TreeSet<HadoopFileInfo>();

        String hadoopPath = targetPath.getPath().toString();

        RecurseFiles(targetPath.getFileSystem(), hadoopPath, existingList);
        return existingList;
    }

/*    public static TreeSet<HadoopFileInfo> getFileList(String hadoopPath) throws Exception {

        TreeSet<HadoopFileInfo> existingList = new TreeSet<HadoopFileInfo>();

        RecurseFiles(hadoopPath, existingList);
        return existingList;
    }*/




    public static void RecurseFiles(FileSystem fs, String hadoopPath, TreeSet<HadoopFileInfo> files ) throws Exception {


        FileStatus[] status = fs.listStatus(new Path(hadoopPath));
        for (int i = 0; i < status.length; i++) {
            if (status[i].isDirectory()) {
                RecurseFiles(fs, status[i].getPath().toString(), files);
            } else {

                FileStatus s = status[i];

                HadoopFileInfo fi = new HadoopFileInfo(s);
                files.add(fi);

            }
        }
    }
}
