package com.aw.unity.query.datatypes;

import com.aw.unity.DataType;
import com.aw.unity.Field;

/**
 * Parses IP values
 *
 *
 *
 */
public class IpValueParser extends AbstractParser {

	@Override
	public Object parse(DataType parentType, Field field, Object value) {

		String strValue = String.valueOf(value);

		//if it contains a mask, parse it as a cidr
		if (strValue.contains(CIDR.CIDR_MASK_CHAR)) {
			return new CIDR(strValue);
		}

		//else assume it's just a network address
		else {
			return new IpAddress(strValue);
		}

	}

}
