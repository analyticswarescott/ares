/*
package com.aw.compute.streams.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.aw.util.TwoKeyMap;

*/
/**
 * Processes bundles in json format
 *
 *
 *
 *//*

public abstract class JSONBundleProcessor extends BundleProcessor<JSONBundleContext, JSONObject> {

	private static final long serialVersionUID = 1L;

	protected void processBundle(JSONObject bundle) throws Exception {

		//process the bundle
		processBundle(new JSONBundleContext(bundle));

	}

	@Override
	protected TwoKeyMap<String, String, JSONObject> cacheByTypeAndKey(JSONBundleContext ctx) throws Exception {

		TwoKeyMap<String, String, JSONObject> ret = new TwoKeyMap<String, String, JSONObject>();

		//cache all keys
		Iterator<String> groupKeyIter = ctx.getBundle().keys();
		while (groupKeyIter.hasNext()) {

			String name = groupKeyIter.next();

			if (KEY_FIELDS.containsKey(name)) {

				for (String key : KEY_FIELDS.get(name)) {

					//only cache ones we link via their key field to events
					cacheByKey(ret, name, key, ctx.getBundle().getJSONArray(name));

				}

			}


		}

		//return the two key map
		return ret;

	}

	private void cacheByKey(TwoKeyMap<String, String, JSONObject> map, String name, String key, JSONArray array) throws Exception {

		for (int x=0; x<array.length(); x++) {

			//cache each element by key value
			JSONObject element = array.getJSONObject(x);
			String value = element.optString(key);

			if (value != null) {

				//to upper, be case insensitive - they mix hash cases in the bundle data
				map.put(name, value.toUpperCase(), element);

			}

		}

	}

	@Override
	protected List<JSONObject> getUads(JSONBundleContext ctx) throws Exception {

		JSONArray array = ctx.getBundle().optJSONArray(UAD);

		List<JSONObject> ret = null;
		if (array != null) {
			ret = new ArrayList<JSONObject>();
			for (int x=0; x<array.length(); x++) {
				ret.add(array.getJSONObject(x));
			}
		}

		return ret;

	}

	@Override
	protected Object getValue(JSONObject element, String key, Object def) throws Exception {
		Object ret = element.opt(key);
		return ret == null ? def : ret;
	}

	@Override
	protected String getValue(JSONObject element, String key, String def) {
		return element.optString(key, def);
	}

	@Override
	protected String getValue(JSONObject element, String key) throws Exception {
		return element.getString(key);
	}

	@Override
	protected Iterator<String> keys(JSONObject element) {
		return element.keys();
	}

	@Override
	protected JSONObject getElement(JSONObject element, String key) throws Exception {
		return element.getJSONObject(key);
	}

	@Override
	protected Collection<JSONObject> getElements(JSONObject element, String key) throws Exception {
		Collection<JSONObject> ret = new ArrayList<>();
		JSONArray array = element.getJSONArray(key);
		if (array != null) {
			for (int x=0; x<array.length(); x++) {
				ret.add(array.getJSONObject(x));
			}
		}
		return ret;
	}

	@Override
	protected Collection<Object> getValues(JSONObject element, String key) throws Exception {
		Collection<Object> ret = new ArrayList<>();
		JSONArray array = element.getJSONArray(key);
		if (array != null) {
			for (int x=0; x<array.length(); x++) {
				ret.add(array.get(x));
			}
		}
		return ret;
	}

}
*/
