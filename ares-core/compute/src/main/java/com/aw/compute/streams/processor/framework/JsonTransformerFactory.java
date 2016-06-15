package com.aw.compute.streams.processor.framework;

/**
 * get a transformer based on an event typr
 */
public interface JsonTransformerFactory {

	public JsonTransformer getTransformer(String eventType) throws Exception;

}
