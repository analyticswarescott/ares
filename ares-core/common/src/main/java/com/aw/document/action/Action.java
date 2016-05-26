package com.aw.document.action;

/**
 * A mapping of action to handler classes
 */
public enum Action {

	/**
	 * Disable mirroring - I don't think this is the right approach for document redundancy - DB technology
	 * needs to cluster
	 */
    MIRROR("com.aw.document.action.ActionMirror"),

    NOTIFY;


    private String _handler;

    Action() {
        _handler = null;
    }

    Action(String handler) {
      _handler = handler;
    }

    public String getHandler() {
        return _handler;
    }

}
