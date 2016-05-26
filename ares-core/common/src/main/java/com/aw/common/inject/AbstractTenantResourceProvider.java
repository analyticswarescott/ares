package com.aw.common.inject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.apache.log4j.Logger;

import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.tenant.Tenant;

/**
 * A tenant-aware dependency provider
 */
public abstract class AbstractTenantResourceProvider<T> implements Provider<T> {

	private static final Logger LOGGER = Logger.getLogger(AbstractTenantResourceProvider.class);

	/**
	 * Map of tenant specific objects
	 */
	protected Map<Tenant, T> resources = new HashMap<Tenant, T>();

	/**
	 * reset cached resources
	 */
	public void reset() {
		resources.clear();
	}

	@Override
	public T get() {

		//TODO: have ThreadLocalStore hold an actual Tenant object
		String id = ThreadLocalStore.get() == null ? Tenant.SYSTEM_TENANT_ID : ThreadLocalStore.get().getTenantID(); //TODO: inject this
		Tenant tenant = new Tenant(id);

		T ret = this.resources.get(tenant);

		//if no resource for this tenant yet
		if (ret == null) {

			try {

				//create a new resource if one doesn't exist for this tenant yet
				ret = getInstance(tenant);

			} catch (Exception e) {
				new RuntimeException("error building instance for tenant " + tenant.getTenantID(), e).printStackTrace();
				System.exit(0);

			}

			//cache the new resource
			this.resources.put(tenant, ret);

		}

		return ret;

	}

	/**
	 * @return A new instance of the resource for the given tenant
	 */
	protected abstract T getInstance(Tenant tenant) throws Exception;

}
