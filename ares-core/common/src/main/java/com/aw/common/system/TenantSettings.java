package com.aw.common.system;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * scott - We shouldn't be using this anymore, right?
 *
 *
 * @deprecated This class should be phased out in favor of com.aw.awmmon.tenant.TenantSettings
 */
@Deprecated
public class TenantSettings {

   public enum Setting {

        ZOOKEEPER_URL,
        ELASTICSEARCH_HOSTS,
        HDFS_URL,
        KAFKA_BROKER_LIST,
        JDBC_URL,
        DBMS_VENDOR;



       public static Setting forValue(String val) {
           return Setting.valueOf(val.toUpperCase());
       }

       @JsonValue
       public String toString() {
           return name().toUpperCase();
       }

    }

}
