package com.aw.action.json;

import com.aw.action.Action;
import com.aw.action.ActionFactory;
import com.aw.action.RootActionFactory;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * allows for json serialization of actions to and from json
 *
 *
 *
 */
public class ActionModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	private SimpleModule actionModule;

	public ActionModule(RootActionFactory rootFactory) {

		addDeserializer(Action.class, new ActionDeserializer<>(rootFactory, Action.class));

		//register for subtypes of Action based on installed factories
		for (ActionFactory factory : rootFactory.getFactories()) {
			addDeserializer(factory.getBaseType(), new ActionDeserializer(rootFactory, factory.getBaseType()));
		}

	}

}
