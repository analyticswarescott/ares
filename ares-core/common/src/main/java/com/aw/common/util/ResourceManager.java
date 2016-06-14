package com.aw.common.util;


import java.io.Serializable;

import com.aw.common.rdbms.DBMgr;
import com.aw.document.jdbc.postgres.PostgresJDBCProvider;
import com.aw.platform.Platform;
import com.aw.platform.PlatformUtils;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import javax.inject.Provider;

public class ResourceManager implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class KafkaProducerSingleton {
        public static void stop() {
            instance = null;
        }
        static private transient Producer<String, String> instance = null;

        public static Producer<String, String> getInstance(Platform platform) throws Exception {
            if (instance == null) {
                ProducerConfig config = new ProducerConfig(PlatformUtils.getKafkaProducerParams(platform));
                instance = new Producer<>(config);
            }
            return instance;
        }
    }





}
