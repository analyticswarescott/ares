package com.aw.document.exceptions;

import com.aw.common.exceptions.InvalidRequestException;

public class DocumentSecurityException extends InvalidRequestException {

	private static final long serialVersionUID = 1L;

	public DocumentSecurityException(String s) {
        super(s);
    }
}
