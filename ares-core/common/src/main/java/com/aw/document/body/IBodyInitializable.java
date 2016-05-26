package com.aw.document.body;

import com.aw.document.DocumentHandler;

/**
 * For objects that require custom initialization beyond simple JSON properties
 */
public interface IBodyInitializable {

    /**
     * Initialize the object with data and document handler for config info
     * @param data
     * @param docs
     * @throws Exception
     */
    public void initialize(Object data, DocumentHandler docs) throws Exception;
}
