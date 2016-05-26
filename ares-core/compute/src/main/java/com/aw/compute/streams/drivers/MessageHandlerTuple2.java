package com.aw.compute.streams.drivers;

import java.io.Serializable;

import org.apache.spark.api.java.function.Function;

import com.aw.common.rest.security.SecurityUtil;

import kafka.message.MessageAndMetadata;
import scala.Tuple2;

public class MessageHandlerTuple2 implements Serializable {


    public Function getA() {
        return new Function<MessageAndMetadata<String, String>, Tuple2<String, String>>() {

            @Override
            public Tuple2<String, String> call(
                    MessageAndMetadata<String, String> arg0)
                    throws Exception {

            	SecurityUtil.setThreadSystemAccess();

                return new Tuple2<String, String>(arg0.key(), arg0.message());

            }

        };
    }

}
