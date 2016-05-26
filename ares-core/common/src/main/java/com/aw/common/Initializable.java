package com.aw.common;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.tenant.Tenant;
import com.aw.common.zookeeper.ZkAccessor;

/**
 * Anything that can be initialized
 *
 *
 *
 * @param <T> The type of data to initialize with
 */
public interface Initializable<T> {

	/**
	 * Initialize the object if it is initializable
	 *
	 * @param obj The object to initialize
	 */
	@SuppressWarnings("unchecked")
	public static <T> void initialize(Object obj, T data, Tenant tenant, ZkAccessor zk) throws InitializationException {

		if (obj instanceof Initializable) {
			((Initializable<T>)obj).init(data);
		}

	}

	public void init(T data) throws InitializationException;


}
