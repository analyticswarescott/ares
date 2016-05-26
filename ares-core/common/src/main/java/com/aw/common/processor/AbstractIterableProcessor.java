package com.aw.common.processor;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes an Iterable K=tenant, V=message
 *
 *
 */
public abstract class AbstractIterableProcessor implements IterableProcessor, Serializable {

	private static final long serialVersionUID = 1L;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

}
