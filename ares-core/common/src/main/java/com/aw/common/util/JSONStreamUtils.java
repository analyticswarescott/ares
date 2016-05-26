package com.aw.common.util;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * Common JSON streaming utilities
 *
 *
 *
 */
public class JSONStreamUtils {

	/**
	 * Process each array element in a potentially huge stream of json. Will process all occurrences of the
	 * array name in the json tree.
	 *
	 * @param arrayName The key name of the array
	 * @param in The input stream
	 * @param handler The handler that will receive each array element json, one at a time
	 * @throws Exception If anything goes wrong
	 */
	public static void processArrayElements(String arrayName, InputStream in, JSONHandler handler, Object context) throws Exception {

		JsonFactory jfactory = new MappingJsonFactory();

		// get to the spot in the stream

		/*** read from inputstream ***/
		JsonParser jParser = jfactory.createParser(in);
		jParser.disable(Feature.AUTO_CLOSE_SOURCE); //don't close it
		JsonToken tok = advanceToken(jParser);

		long ordinal = 0;
		while (tok != null) {

			//get the current name, look for our array name
			String curArrayName = jParser.getCurrentName();

			//if it matches, process it
			if (arrayName.equals(curArrayName)) {

				while (tok != JsonToken.START_ARRAY) {
					tok = advanceToken(jParser);
				}

				//next object
				tok = advanceToken(jParser);

				while (tok != JsonToken.END_ARRAY && tok != null) {

					//get the json node for this array element
					JsonNode node = jParser.readValueAsTree();

					//send the json to the handler
					String strNode = node.toString();

					//handle the json
					ordinal++;
					handler.handleJson(strNode, ordinal, context);

					//move on
					tok = advanceToken(jParser);

				}

			}

			//next
			tok = advanceToken(jParser);

		}

		return;

	}

	private static JsonToken advanceToken(JsonParser p) throws IOException {
		try {
			return p.nextToken();
		} catch (JsonParseException ep) {
			return advanceToken(p);
		}

	}

}