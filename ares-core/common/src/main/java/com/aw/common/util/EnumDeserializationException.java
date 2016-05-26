package com.aw.common.util;

import java.io.IOException;

/**
 * @author jhaightdigitalguardian.com.
 */
public class EnumDeserializationException extends IOException {

	String unparsableEnum;

	public EnumDeserializationException(String unparsableEnum) {
		super(unparsableEnum);
		this.unparsableEnum = unparsableEnum;
	}

	public EnumDeserializationException(String unparsableEnum, Exception e) {
		super(e);
		this.unparsableEnum = unparsableEnum;
	}
	public EnumDeserializationException(String unparsableEnum, String message, Exception e) {
		super(message, e);
		this.unparsableEnum = unparsableEnum;
	}

	public String getUnparsableEnum() {
		return unparsableEnum;
	}
}
