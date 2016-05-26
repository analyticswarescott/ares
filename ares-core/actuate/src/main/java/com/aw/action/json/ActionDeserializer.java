package com.aw.action.json;

import java.io.IOException;

import com.aw.action.Action;
import com.aw.action.RootActionFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * reads actions from json
 *
 *
 *
 */
public class ActionDeserializer<T extends Action> extends StdDeserializer<T> {

	private static final long serialVersionUID = 1L;

	private RootActionFactory factory;

	public ActionDeserializer(RootActionFactory factory, Class<T> type) {
		super(type);
		this.factory = factory;
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		//get the raw json
		JsonNode node = p.readValueAsTree();
		String json = node.toString();

		try {

			//build the action using the action manager
			return (T)factory.newAction(json);

		} catch (Exception e) {

			//package up any exception
			throw new IOException("error building action from raw json", e);

		}

	}


}
