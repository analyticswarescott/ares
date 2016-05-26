package com.aw.action;

import java.util.ArrayList;
import java.util.List;

import com.aw.action.exceptions.ActionCopyException;

public class Util {

	/**
	 * Create a copy of all action instances passed in
	 *
	 * @param actions
	 * @return
	 */
	public static <T extends Action> List<T> copyAll(List<T> actions) throws ActionCopyException {

		List<T> ret = new ArrayList<T>();

		for (T action : actions) {
			ret.add(action.copy());
		}

		return ret;

	}

}
