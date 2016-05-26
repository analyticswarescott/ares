/*
package com.aw.compute.streams.processor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.aw.unity.Field;
import com.aw.unity.FieldRepository;
import com.aw.unity.UnityInstance;
import com.aw.unity.UnityMetadata;
import com.aw.unity.dg.BundleFields;
import com.aw.util.Statics;
import com.aw.util.TwoKeyMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

*/
/**
 * Base class for processing JSON bundle data in Spark.
 *
 *
 *
 *//*

public abstract class XMLBundleProcessor extends BundleProcessor<XMLBundleContext, Map<String, Object>> {

	private static final long serialVersionUID = 1L;

	public static final String TAG_BUNDLE = "bundle";
	public static final String MACHINE_ID = "mid";
	public static final String BUNDLE_ID = "bid";
	public static final String TENANT_ID = "tid";

	//special handling for this, it's not in the DGRuleMeta.xml data model
	public static final String CUSTOM_DATA_RAW = "customData";

	public static final String VALUE_KEY = "value";
	public static final String NAME_KEY = "name";
	public static final String SCOPE_KEY = "scope";
	public static final String VALUES_KEY = "values";

	//all custom fields to their unity equivalent

	public final static Map<String, String> CUSTOM_DATA_FIELD_MAPPINGS = ImmutableMap.<String, String>builder()
		.put(CUSTOM_DATA_RAW, "custom_data")
		.build();

	protected void processBundle(InputSource source) throws Exception {

		//create a handler to build the context object
		XMLBundleHandler handler = new XMLBundleHandler();

		//connect up the filter to the parser
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();

        //parse the xml bundle
        sp.parse(source, handler);

        //process the xml bundle
        processBundle(handler.getContext());

	}

	@Override
	protected TwoKeyMap<String, String, Map<String, Object>> cacheByTypeAndKey(XMLBundleContext ctx) throws Exception {
		return ctx.getReferenceData();
	}

	@Override
	protected List<Map<String, Object>> getUads(XMLBundleContext ctx) throws Exception {
		return ctx.getUads();
	}

	@Override
	protected Object getValue(Map<String, Object> element, String key) throws Exception {
		Object ret = null;
		if (element != null) {
			ret = element.get(key);
		}

		if (ret == null) {
			throw new NoSuchElementException("missing required key: " + ret);
		}

		return ret;
	}

	@Override
	protected String getValue(Map<String, Object> element, String key, String def) {
		Object ret = getValue(element, key, (Object)def);
		return ret == null ? null : ret.toString();
	}

	@Override
	protected Object getValue(Map<String, Object> element, String key, Object def) {
		Object ret = null;
		if (element != null) {
			ret = element.get(key);
		}

		if (ret == null) {
			ret = def;
		}

		return ret;
	}

	@Override
	protected Collection<Object> getValues(Map<String, Object> element, String key) throws Exception {
		return (Collection<Object>)element.get(key);
	}

	@Override
	protected Map<String, Object> getElement(Map<String, Object> element, String key) throws Exception {
		return (Map<String, Object>)element.get(key);
	}

	@Override
	protected Collection<Map<String, Object>> getElements(Map<String, Object> element, String key) throws Exception {
		return (Collection<Map<String, Object>>)element.get(key);
	}

	@Override
	protected Iterator<String> keys(Map<String, Object> element) {
		if (element == null) {
			return Collections.emptyIterator();
		} else {
			return element.keySet().iterator();
		}
	}

	*/
/**
	 * Attempts to convert the xml attributes to a string, string map. Unity type information is used to verify.
	 *
	 * @param attributes
	 * @return
	 *//*

	private Map<String, Object> toMap(String key, Attributes attributes) {
		Map<String, Object> map = new HashMap<String, Object>();

		//get the field repository
		UnityMetadata metadata = getDependency(UnityInstance.class).getMetadata();
		FieldRepository repo = metadata.getFieldRepository();

		//first build a map of attribute name->value
		for (int x=0; x<attributes.getLength(); x++) {

			//get the raw name of the attribute
			String rawName = attributes.getQName(x);

			//get the value for the attribute
			String rawValue = attributes.getValue(x);

			//build a field name from the raw name
			String fieldName = toFieldName(key, rawName);

			//check uad as well as this may contain the same raw field
			String uadFieldName = toFieldName(UAD, rawName);

			//see if we know about this field (meaning we use it in a unity data type)
			Field field = null;
			if (repo.hasField(fieldName)) {
				field = repo.getField(fieldName);
			}

			else if (repo.hasField(uadFieldName)) {
				field = repo.getField(uadFieldName);
			}

			//check known raw fields that map to common fields
			else if (BundleFields.BUNDLE_TO_COMMON.containsKey(rawName)) {
				field = BundleFields.BUNDLE_TO_COMMON.get(rawName).asField();
			}

			//if there is a field, we can process the value
			if (field != null) {

				//we do, cache it
				String value = decodeValue(field, rawValue);

				//we use this field in the repo, cache it
				map.put(rawName, value);

			}

			//else it's not a unity field, just cache it
			else {
				map.put(rawName, rawValue);
			}

		}			// TODO Auto-generated method stub


		return map;
	}

	*/
/**
	 * Get the final field value for the raw value given the unity field it corresponds to
	 *
	 * @param rawValue
	 * @param field
	 * @return
	 *//*

	private String decodeValue(Field field, String rawValue) {

		//if we don't need to do anything, we'll just return the original raw value
		String value = rawValue;

		switch (field.getType()) {

			//strings are base64 encoded
			case STRING:

				//if it's not an ID (some are marked as strings) and is a valid length, decode it as base 64
				//TODO: continue on failure?
				if (!rawValue.startsWith(BundleProcessor.SQUIRLY_OPEN) && rawValue.length() > 1) {
					//the base 64 encoded data is coming in little endian, UTF-16 from the agent bundles on windows
					value = new String(Base64.getDecoder().decode(rawValue), Statics.UTF_16_LE);
				}
				break;

			default:

				//unless it's a string, we don't need to do anything
				break;

		}

		return value;

	}

	// TODO Auto-generated method stub

	*/
/**
	 * The SAX handler for parsing xml bundles
	 *//*

	public class XMLBundleHandler extends DefaultHandler {

		public XMLBundleContext getContext() { return m_ctx; }
		XMLBundleContext m_ctx = new XMLBundleContext();

		//the stack of objects being built
		Deque<BundleObject> objectStack = new LinkedList<BundleObject>();

		//current object being built and its tag
		BundleObject curObject;

		@Override
		public void startElement(String uri, String localName, String tag, Attributes attributes)
				throws SAXException {

			//set top level properties on the first element and that's it for that element
			if (TAG_BUNDLE.equals(tag)) {

				handleBundle(tag, attributes);
				return;

			}

			//build an initial object from the attributes
			Map<String, Object> data = toMap(tag, attributes);

			//create the new child object
			BundleObject newObject = new BundleObject(data, tag);

			//link and put the current parent object (tag) on the stack if applicable
			if (curObject != null) {

				//check that we don't have an attribute/tag clash
				Object objChildren = curObject.getData().get(tag);
				Preconditions.checkState(objChildren == null || (objChildren instanceof Collection), "bundle " + m_ctx.getBundleID() + ": attribute name and child tag name clash: " + tag);

				//child tags are added to a collection on the parent - get that now
				Collection<Map<String, Object>> children = (Collection<Map<String, Object>>)curObject.getData().get(tag);
				if (children == null) {
					children = new ArrayList<Map<String, Object>>();
					curObject.getData().put(tag, children);
				}

				//add the child to the collection
				children.add(newObject.getData());

				//add the parent object to the stack, we're moving further down into the xml structure
				objectStack.addLast(curObject);

			}

			//the current object is now the new child object
			curObject = newObject;

		}

		@Override
		public void endElement(String uri, String localName, String tag) throws SAXException {

			//complete the current object if applicable
			if (curObject != null && curObject.getTag().equals(tag)) {
				endObject();
			}

		}

		*/
/**
		 * ugly method to convert custom data - we don't have good field names here, so we are using our own
		 * @param parent
		 * @param customData
		 *//*

		private void convertCustomData(BundleObject parent, BundleObject customData) {

			Map<String, Object> data = parent.getData();

			for (String key : CUSTOM_DATA_FIELD_MAPPINGS.keySet()) {

				//put customData in as its unity field name equivalent
				List<Map<String, Object>> customDataList = (List<Map<String, Object>>)data.remove(key);

				if (customDataList == null) {
					continue;
				}

				//this will hold the new custom data objects
				List<Map<String, Object>> newList = new ArrayList<>();

				//convert to the proper name from the raw name
				data.put(CUSTOM_DATA_FIELD_MAPPINGS.get(key), newList);

				//only one custom data object
				Map<String, Object> customDataObj = customDataList.get(0);

				//for each key, there is a list of custom_data instances
				for (String type : customDataObj.keySet()) {

					//for each key, go through the list of data
					Collection<Map<String, Object>> typeData = (Collection<Map<String, Object>>)customDataObj.get(type);
					for (Map<String, Object> curData : typeData) {

						//convert each custom data variable to a flattened format for processing
						newList.add(convertCustomVariable(curData));

					}

				}

			}

		}

		*/
/**
		 * Convert a single named custom data variable holding one or more values to a format
		 * that the bundle processor framework can understand. This method flattens out
		 * the xml structure to result in a simple json object with three keys: name, scope, values
		 *
		 *
		 *
		 * @param curData the old format
		 * @return the new format
		 *//*

		private Map<String, Object> convertCustomVariable(Map<String, Object> curData) {

			Map<String, Object> newData = new HashMap<String, Object>();

			for (String curKey : curData.keySet()) {

				Object object = curData.get(curKey);
				if (SCOPE_KEY.equals(curKey)) {
					newData.put(SCOPE_KEY, curData.get(SCOPE_KEY));
				}

				else if (NAME_KEY.equals(curKey)) {
					newData.put(NAME_KEY, curData.get(NAME_KEY));
				}

				//else must be the values
				else if (object instanceof Collection) {

					//last key are the values
					List<String> values = new ArrayList<String>();
					for (Map<String, Object> value : (Collection<Map<String, Object>>)object) {
						values.add(String.valueOf(value.get(VALUE_KEY)));
					}
					newData.put(VALUES_KEY, values);

				}

			}

			return newData;

		}

		*/
/**
		 * At this point we have a curObject set, and need to handle the data somehow. At the end of this method,
		 * curObject should be pointing to the parent object in the stack, or null if the stack is empty.
		 *//*

		private void endObject() {

			String tag = curObject.getTag();

			if (tag.equals(CUSTOM_DATA_RAW) && objectStack.size() > 0) {
				convertCustomData(objectStack.getLast(), curObject);
			}

			//TODO: special handling for custom data, create a custom data object

			//get the key field if any
			if (KEY_FIELDS.containsKey(tag)) {
				for (String key : KEY_FIELDS.get(tag)) {

					//if there's something to cache, cache it
					if (curObject.getData() != null && curObject.getData().size() > 0) {
						m_ctx.cacheReferenceData(tag, key, curObject.getData());
					}

				}
			}

			//else add uads to the uad list - we know we need these fields
			else if (UAD.equals(tag)) {
				m_ctx.cacheUad(curObject.getData());
			}

			//pop the parent object from the stack if applicable
			if (!objectStack.isEmpty()) {
				curObject = objectStack.removeLast();
			} else {
				curObject = null;
			}

		}

		private void handleBundle(String tag, Attributes attributes) {

			//pull out the machine, bundle, and tag ids - strip the {} as they are not part of a standard RFC4122 UUID
			m_ctx.setMachineID(attributes.getValue(MACHINE_ID));
			m_ctx.setBundleID(attributes.getValue(BUNDLE_ID));
			m_ctx.setTenantID(attributes.getValue(TENANT_ID));

		}

		*/
/**
		 * Holds the structure of bundle data for a particular spot in the bundle tree, along with the
		 * tag name from the xml.
		 *//*

		private class BundleObject {

			public BundleObject(Map<String, Object> data, String tag) {
				this.data = data;
				this.tag = tag;
			}

			private Map<String, Object> getData() { return data; }
			private Map<String, Object> data;

			private String getTag() { return tag; }
			private String tag;

			@Override
			public String toString() {
				return "[" + tag + " values=" + data.size() + "]";
			}

		}

	}

}

*/
