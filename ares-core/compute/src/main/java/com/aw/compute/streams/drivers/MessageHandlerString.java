package com.aw.compute.streams.drivers;

import java.io.Serializable;

import org.apache.spark.api.java.function.Function;

import kafka.message.MessageAndMetadata;

public class MessageHandlerString implements Serializable {


    public Function getA() {
        return new Function<MessageAndMetadata<String, String>, String>() {

            @Override
            public String call(
                    MessageAndMetadata<String, String> arg0)
                    throws Exception {
               return arg0.message();
            }

        };
    }

}
