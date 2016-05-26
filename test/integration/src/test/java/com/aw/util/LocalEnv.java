package com.aw.util;


import com.aw.utils.tests.KafkaZookeeperServiceWrapper;

/**
 * Created by scott on 02/10/15.
 */
public class LocalEnv {


    public static void start() {
        KafkaZookeeperServiceWrapper kafkaZookeeperServiceWrapper = KafkaZookeeperServiceWrapper.create();
        kafkaZookeeperServiceWrapper.start();

        //ElasticSearchServiceWrapper elasticSearchServiceWrapper = ElasticSearchServiceWrapper.create();


        Thread.currentThread().setDaemon(true);
    }

    public static void stop  () {
        Thread.currentThread().setDaemon(false);
    }


    public static void main(String[] args) {


        KafkaZookeeperServiceWrapper kafkaZookeeperServiceWrapper = KafkaZookeeperServiceWrapper.create();
        kafkaZookeeperServiceWrapper.start();

        //ElasticSearchServiceWrapper elasticSearchServiceWrapper = ElasticSearchServiceWrapper.create();


        Thread.currentThread().setDaemon(true);
    }
}
