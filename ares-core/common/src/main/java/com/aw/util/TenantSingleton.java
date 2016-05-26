package com.aw.util;

import java.util.HashMap;
import java.util.Map;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.rest.security.TenantAware;

/**
 * A singleton per tenant
 *
 *
 */
public class TenantSingleton<T> implements TenantAware {

	public TenantSingleton(Class<T> type, TenantSingletonFactory<T> factory) {
		m_factory = factory;
		m_type = type;
	}

	/**
	 * @return The singleton for the tenant based on the current thread context
	 */
	public T get() {

		String tenant = getTenantID();
		return get(tenant);

	}

	/**
	 * @param tenantID The tenant ID whose singleton is needed
	 * @return The singleton for the requested tenant ID
	 */
	public T get(String tenantID) {

		try {

			T ret = m_map.get(tenantID);
			if (ret == null) {
				ret = m_factory.newInstance();
				m_map.put(tenantID, ret);
			}

			return ret;

		} catch (Exception e) {
			throw new InitializationException("could not create singlton of type " + m_type, e);
		}

	}

	private Map<String, T> m_map = new HashMap<String, T>();
	private Class<T> m_type;
	private TenantSingletonFactory<T> m_factory;

}
