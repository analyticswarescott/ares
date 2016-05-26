package com.aw.compute.streams.processor.framework;

import java.io.Serializable;

import com.aw.common.Initializable;
import com.aw.common.spark.StreamDef;

/**
 * Base processor class allowing initialization with external data
 *
 *
 *
 */
public interface Processor extends Initializable<StreamDef>, Serializable {

}
