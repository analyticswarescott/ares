package com.aw.common.hadoop.util;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;

public class HadoopFileInfo implements Comparable {

    public String Path;
    public Double SizeMB;
    public long ModifiedLong;
    public DateTime ModifiedDateTime;

    public Path getPath() {
        return new Path(Path);
    }

    public HadoopFileInfo(FileStatus fs) {

        Path = fs.getPath().toString();
        long mt = fs.getModificationTime();
        ModifiedLong = mt;
        DateTime mod = new DateTime(mt);
        ModifiedDateTime = mod;



        long l = fs.getLen();

        double mb = Double.parseDouble( Long.toString(l))/1048576;
        mb = Math.round(mb*100.0)/100.0;

        SizeMB = mb;



    }


    @Override
    public int compareTo(Object o) {
        HadoopFileInfo ocomp = (HadoopFileInfo) o;
        return Long.compare(this.ModifiedLong, ocomp.ModifiedLong);

    }

    @Override
    public String toString() {
        return Path + "  " + SizeMB + "  MB  " + ModifiedDateTime;
    }


}
