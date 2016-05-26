package com.aw.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of an action factory that takes a set of ActionType instances
 *
 *
 *
 */
public class DefaultActionFactory implements ActionFactory {

	private ActionType[] types;
	private Map<String, ActionType> typeMap;
	private Class<? extends Action> baseType;

	public DefaultActionFactory(Class<? extends Action> baseType, ActionType... types) {

		this.baseType = baseType;
		this.types = types;

		//cache by name
		this.typeMap = Arrays.asList(types).stream().collect(Collectors.toMap(ActionType::name, (type) -> type));

	}

	@Override
	public List<ActionType> getAllTypes() {
		return Arrays.asList(types);
	}

	@Override
	public boolean hasType(String strType) {
		return typeMap.containsKey(strType.toUpperCase());
	}

	@Override
	public ActionType toType(String strType) {
		return typeMap.get(strType.toUpperCase());
	}

	@Override
	public Class<? extends Action> getBaseType() {
		return baseType;
	}

}
