package com.aw.compute.streams.processor.framework;

import org.apache.spark.streaming.api.java.JavaDStream;

import com.aw.compute.streams.exceptions.StreamProcessingException;

import scala.Tuple2;

/**
 * For any spark streaming job within the platform, this interface must be implemented. An implementation of this
 * interface can be injected into any StreamDef configuration to extend the processing functionality within the DG
 * spark streaming platform. A stream of tuples will be passed in, with the first value in the tuple being the
 * tenant ID and the second being the message payload.
 *
 *
 *
 */
public interface StreamHandler {

	/**
	 * Handle a Tuple2 stream. The tuple key is the tenant ID, the tuple value is the handler-specific payload.
	 *
	 * @param stream The stream to handler
	 */
	public void handle(JavaDStream<Tuple2<String, String>> stream) throws StreamProcessingException;

}
