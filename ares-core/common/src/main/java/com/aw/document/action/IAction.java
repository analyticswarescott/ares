package com.aw.document.action;

import com.aw.document.Document;
import com.aw.platform.Platform;

/**
 * Created by scott on 08/11/15.
 */
public interface IAction {

    public void doAction(Platform platform, Document doc, Operation operation) throws Exception;

}
