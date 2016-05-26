package com.aw.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Preconditions;

/**
 * an action factory that can create actions from multiple child factories from JSON
 *
 *
 *
 */
public final class DefaultRootActionFactory implements RootActionFactory {

	ActionFactory[] factories;
	Map<String, ActionFactory> factoryByType = new HashMap<String, ActionFactory>();

	public DefaultRootActionFactory(ActionFactory... factories) {
		this.factories = factories;
	}

	@Override
	public Action newAction(Object data) throws Exception {

		JSONObject json = null;
		if (!(data instanceof JSONObject)) {
			String strJson = data.toString();
			json = new JSONObject(strJson);
		} else {
			json = (JSONObject)data;
		}

		//get the type from the json
		String type = json.getString(Action.TYPE);

		ActionFactory factory = getFactory(type);

		Preconditions.checkNotNull(factory, "could not resolve action type (no type factory) for " + type);

		//get the action type and create the action
		ActionType actionType = factory.toType(type);

		Preconditions.checkNotNull(actionType, "could not resolve action type " + type);

		return actionType.newAction(json);

	}

	@Override
	public ActionFactory[] getFactories() {
		return factories;
	}

	@Override
	public List<ActionType> getAllTypes() {
		List<ActionType> ret = new ArrayList<>();
		for (ActionFactory factory : factories) {
			ret.addAll(factory.getAllTypes());
		}
		return ret;
	}

	@Override
	public Class<? extends Action> getBaseType() {
		return Action.class;
	}

	@Override
	public boolean hasType(String strType) {
		return getFactory(strType) != null;
	}

	@Override
	public ActionType toType(String strType) {

 		ActionFactory factory = getFactory(strType);
 		Preconditions.checkState(factory != null, "unsupported action type detected: " + strType);
 		return factory.toType(strType);

	}

	ActionFactory getFactory(String type) {

		//look for the factory by type
		ActionFactory factory = factoryByType.get(type);
		if (factory == null) {

			//get the action by type if we haven't looked it up already
			for (ActionFactory curFactory : factories) {

				//if we find a matching factory, we're done
				if (curFactory.hasType(type)) {

					factory = curFactory;

					//remember it so we don't go through the array every time
					factoryByType.put(type, curFactory);

					break;

				}

			}

		}

		return factory;

	}

}
