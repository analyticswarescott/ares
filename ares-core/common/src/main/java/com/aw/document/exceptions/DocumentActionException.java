package com.aw.document.exceptions;

import com.aw.common.exceptions.InvalidRequestException;

public class DocumentActionException extends InvalidRequestException {


    public DocumentActionException(String s) {
        super(s);
    }
}
