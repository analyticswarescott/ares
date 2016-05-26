package com.aw.utils.kafka.client;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;


/**
 * Partitioner that always specifies partition 0.
 * @author bartonneil
 *
 */
public class ZeroPartitioner implements Partitioner {


	public ZeroPartitioner(VerifiableProperties props) {}


	@Override
	public int partition(Object arg0, int arg1) {
		return 0;
	}

}
