package com.aw.unity.query.datatypes;

import org.apache.commons.net.util.SubnetUtils;

/**
 * CIDR notation support - v4 only for now
 *
 *
 *
 */
public class CIDR extends SubnetUtils implements Addressable {

	public static final String CIDR_MASK_CHAR = "/";

	public CIDR(String pattern) {
		super(pattern);
	}

	@Override
	public boolean contains(IpAddress ip) {
		//TODO: write some custom code around this to make it faster
		return getInfo().isInRange(ip.getAddress().getHostAddress());
	}

	@Override
	public String toString() {
		return getInfo().getCidrSignature();
	}

	@Override
	public int compareTo(Addressable o) {

		if (!(o instanceof CIDR)) return -1; //we come before non-cidrs

		else {
			return getInfo().getCidrSignature().compareTo(((CIDR)o).getInfo().getCidrSignature());
		}

	}


}
