package com.aw.unity.query.datatypes;

import java.net.InetAddress;

import com.aw.unity.exceptions.InvalidFilterException;
import com.google.common.net.InetAddresses;

/**
 * An IP address represents a single v4 or v6 address on the network.
 *
 *
 *
 */
public class IpAddress implements Addressable {

	public IpAddress(String addr) {
		try {
			m_address = InetAddresses.forString(addr);
		} catch (Exception e) {
			throw new InvalidFilterException("Error parsing address " + addr + ": " + e.getMessage());
		}
	}

	/**
	 * An addressable object can return whether an IP address is "in" it.. in this case, true is only returned on equality.
	 */
	@Override
	public boolean contains(IpAddress ip) {
		return m_address == ip.m_address || (m_address != null && m_address.equals(ip.m_address));
	}

	@Override
	public int compareTo(Addressable o) {
		if (!(o instanceof IpAddress)) return 1; //we come after any other addressables

		else {
			return ((IpAddress)o).m_address.getHostAddress().compareTo(m_address.getHostAddress());
		}
	}

	@Override
	public String toString() {
		return m_address.getHostAddress();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_address == null) ? 0 : m_address.hashCode());
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
		IpAddress other = (IpAddress) obj;
		if (m_address == null) {
			if (other.m_address != null)
				return false;
		} else if (!m_address.equals(other.m_address))
			return false;
		return true;
	}

	public InetAddress getAddress() { return m_address; }
	public void setAddress(InetAddress address) { m_address = address; }
	private InetAddress m_address;

}
