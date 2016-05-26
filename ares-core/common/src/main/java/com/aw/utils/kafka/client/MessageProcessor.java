package com.aw.utils.kafka.client;

import java.util.Properties;


/**
 * Interface for Processing incoming messages.
 * @author bartonneil
 *
 */
public interface MessageProcessor {


	/**
	 * Initialize the message processor.
	 * @param props
	 * @throws Exception
	 */
	void initialize(Properties props) throws Exception;


	//void processMessage(long offsetId, byte[] payload);


	/**
	 * Messages processed in batches, in all-or-nothing approach.  The entire batch is
	 * considered processed if no errors encountered.  Nothing is considered processed
	 * if an Exception throw
	 * @param batchPayload
	 * @throws Exception
	 */
	void processBatch(byte[][] batchPayload) throws Exception;


	/**
	 * Shutdown gracefully
	 */
	void shutdown();

}
