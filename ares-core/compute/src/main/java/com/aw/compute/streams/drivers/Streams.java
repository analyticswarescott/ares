package com.aw.compute.streams.drivers;

import com.aw.common.spark.StreamDef;
import com.aw.common.tenant.Tenant;
import com.aw.util.ListMap;

/**
 * The streams configured to run within a driver
 *
 *
 *
 */
public class Streams {

	public Streams(ListMap<Tenant, StreamDef> tenantToStreams) {
		this.tenantToStreams = tenantToStreams;
	}

	public Streams() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tenantToStreams == null) ? 0 : tenantToStreams.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Streams other = (Streams) obj;
		if (tenantToStreams == null) {
			if (other.tenantToStreams != null)
				return false;
		} else if (!tenantToStreams.equals(other.tenantToStreams))
			return false;
		return true;
	}

	/**
	 * @return The full set of streams that should be running within this driver
	 */
	public ListMap<Tenant, StreamDef> getTenantToStreams() { return this.tenantToStreams;  }
	public void setTenantToStreams(ListMap<Tenant, StreamDef> streamsPerTenant) { this.tenantToStreams = streamsPerTenant; }
	private ListMap<Tenant, StreamDef> tenantToStreams;

}
