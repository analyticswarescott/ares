	package com.aw.common.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.exceptions.InvalidFormatException;
import com.aw.document.Document;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JSONUtils {

	private static Pattern arrayIndexPattern = Pattern.compile("(.+)\\[([0-9])\\]");

	public static final Logger logger = Logger.getLogger(JSONUtils.class);
	public static String JSONElementDelimiter = ".";

	private static final JsonSerializer<Double> DOUBLE_SERIALIZER = new JsonSerializer<Double>() {

		public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException ,JsonProcessingException {
			BigDecimal dec = new BigDecimal(value);
			gen.writeNumber(dec.toPlainString());
		}

		public java.lang.Class<Double> handledType() {
			return Double.class;
		}

	};

	/**
	 * KeyDeserializers that looks at superclasses - for some reason jackson doesn't do this by default
	 *
	 *
	 */
	private static class DefaultKeyDeserializers extends SimpleKeyDeserializers {

		private static final long serialVersionUID = 1L;

		private Map<Class<?>, KeyDeserializer> deserializers = new HashMap<Class<?>, KeyDeserializer>();

		@Override
		public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config,
				BeanDescription beanDesc) {

			//get the raw class type
			Class<?> cls = type.getRawClass();
			KeyDeserializer ret = null;

			//if it's an enum, toUpper it and invoke valueOf
			if (cls.isEnum()) {
				ret = deserializers.get(cls);
				if (ret == null) {
					ret = new EnumKeyDeserializer(cls);
				}

				//reuse instance
				this.deserializers.put(cls, ret);
			}

			//fall back - would only happen for non-enums
			if (ret == null) {
				ret = super.findKeyDeserializer(type, config, beanDesc);
			}

			return ret;

		}

	}
	private static class EnumKeyDeserializer extends KeyDeserializer {

		Class<?> cls;

		public EnumKeyDeserializer(Class<?> cls) {
			this.cls = cls;
		}

		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			try {

				Method method = this.cls.getMethod("valueOf", String.class);
				if (method == null) {
					throw  new UnsupportedOperationException("no valueOf method for enum " + cls); //shouldn't happen
				} else {
					return method.invoke(null, key.toUpperCase());
				}

			} catch (Exception e) {
				throw new IOException("error deserializing map key", e); //shouldn't happen
			}
		}

	};

	public static class InstantDeserializer extends StdDeserializer<Instant> {

		private boolean epoch;

		public InstantDeserializer(boolean epoch) {
			super(Instant.class);
			this.epoch = epoch;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			if (epoch) {
				return Instant.ofEpochMilli(p.getValueAsLong());
			} else {
				try {
					return parseDate(p.getValueAsString());
				} catch (Exception e) {
					throw new IOException("error parsing date " + p.getValueAsString(), e);
				}
			}
		}

	};

	public synchronized static String formatTimestamp(Instant instant) {
		return JSON_DATE_FORMAT.format(Date.from(instant));
	}

	public synchronized static Instant parseDate(String value) throws ParseException {
		return JSON_DATE_FORMAT.parse(value).toInstant();
	}

	public static class InstantSerializer extends  StdSerializer<Instant> {

		private static final long serialVersionUID = 1L;

		private boolean epoch;

		public InstantSerializer(boolean epoch) {
			super(Instant.class);
			this.epoch = epoch;
		}

		@Override
		public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			if (epoch) {
				gen.writeNumber(value.toEpochMilli());
			} else {
				try {
					gen.writeString(formatTimestamp(Date.from(value).toInstant()));
				} catch (Exception e) {
					throw new IOException("error writing date for value " + value, e);
				}
			}
		}

	};

	/**
	 * Used to toLower all enums on the way out
	 */
	public static class ToLowerSerializer extends StdSerializer<Object> {

		private static final long serialVersionUID = 1L;

		public ToLowerSerializer() {
			super(Object.class);
		}

		public ToLowerSerializer(Class<?> type) {
			super(type, false);
		}

		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
			gen.writeString(value.toString().toLowerCase());
		}

	}

	public static class EnumSerializer extends StdSerializer<Enum> {

		private static final long serialVersionUID = 1L;

		public EnumSerializer() {
			super(Enum.class);
		}

		@Override
		public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
			gen.writeString(value.toString().toLowerCase());
		}

	}

	private static class JSONableSerializer extends StdSerializer<JSONable> {

		private static final long serialVersionUID = 1L;

		public JSONableSerializer() {
			super(JSONable.class);
		}

		@Override
		public void serialize(JSONable value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			try {
				jgen.writeRawValue(value.toJSON());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

	}

	/**
	 * Used to toUpper all enums on the way in
	 */
	private static class EnumDeserializer extends StdDeserializer<Enum<?>> implements ContextualDeserializer {

		private static final long serialVersionUID = 1L;

		private Class<?> m_contextualType;

		public EnumDeserializer(Class<?> contextualType) {
			this();
			m_contextualType = contextualType;
		}

		public EnumDeserializer() {
			super(Enum.class);
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
			if (m_contextualType != null) {
				return this;
			}

			//TODO: is this a performance killer? it saves us from having to manually add serializer annotations on all enums..
			//since we are deserializing here i don't think this is an issue - would be a bigger deal if we were writing millions of enums
			//out using this method but we won't be
			//one way to solve this new'ing each time might be to key off of the value itself in a map
			if (property.getType().getContentType() != null) {
				return new EnumDeserializer(property.getType().getContentType().getRawClass());
			} else {
				return new EnumDeserializer(property.getType().getRawClass());
			}
		}

		@Override
		public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {

				//make sure we have the contextual type
				if (m_contextualType == null) {
					throw new IOException("Couldn't determine enum type for enum value " + p.getValueAsString());
				}

				Method method = m_contextualType.getDeclaredMethod("valueOf", String.class);
				return (Enum<?>)method.invoke(null, p.getValueAsString().toUpperCase());

			} catch (Exception e) {
				throw new EnumDeserializationException(p.getValueAsString(), "Unable to deserialize value: " + p.getValueAsString(), e);
			}
		}

	}

	/**
	 * get a json object from root given the path of keys - this method will throw an exception
	 * if the path isn't found
	 *
	 * @param root
	 * @param path
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray getArray(JSONObject root, String... path) throws JSONException {

		return (JSONArray)get(root, path);

	}

	/**
	 * get a json object from root given the path of keys - this method will throw an exception
	 * if the path isn't found
	 *
	 * @param root
	 * @param path
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject getObject(JSONObject root, String... path) throws JSONException {
		return (JSONObject)get(root, path);
	}

	/**
	 * get a json object from root given the path of keys - this method will throw an exception
	 * if the path isn't found
	 *
	 * @param root
	 * @param path
	 * @return
	 * @throws JSONException
	 */
	public static Object get(JSONObject root, String... path) throws JSONException {

		if (path == null || path.length == 0) {
			return root;
		}

		JSONObject object = root;

		//up to the last entry, move through the json path
		for (int entry=0; entry<path.length-1; entry++) {

			object = object.getJSONObject(path[entry]);

			//if we end up with null, that's an exception
			if (object == null) {
				throw new InvalidFormatException("Couldn't locate path in json: " + Arrays.toString(path));
			}

		}

		//return last object in the path
		return object.get(path[path.length-1]);

	}

	public static void putAll(JSONArray src, JSONArray dst) throws JSONException {
		for (int x=0; x<src.length(); x++) {
			dst.put(src.get(x));
		}
	}

	public static <T> T getLeafValue(JSONObject hudMetadata, String path) throws JSONException {
		Optional<T> result = JSONUtils.processLeaf(hudMetadata, path, (node, key, parent) -> {
			return Optional.of((T) node);
		});
		return result.get();
	}

	public static <T> Optional<T> processLeaf( Object jsonObject, String path, JSONUtilsNodeProcessor<T> consumer ) throws JSONException {
		StringTokenizer st = new StringTokenizer(path, JSONElementDelimiter);
		Object currentNode = jsonObject;
		String key = null;
		Object parent = null;
		while ( st.hasMoreTokens() ) {
			String nextToken = st.nextToken(JSONElementDelimiter);
			Matcher arrayIndexMatcher = arrayIndexPattern.matcher(nextToken);
			if ( arrayIndexMatcher.matches() ) {
				key = arrayIndexMatcher.group(1);
				int index = Integer.valueOf(arrayIndexMatcher.group(2));

				parent = currentNode;
				currentNode = ((JSONArray)((JSONObject) currentNode).get(key)).get(index);
			} else {
				key = nextToken;
				parent = currentNode;
				currentNode = ((JSONObject) currentNode).get(nextToken);
			}
		}
		if (key != null) {
			return consumer.accept(currentNode, key, parent);
		}

		return Optional.empty();
	}

	public static String replaceSingleTokenInString(String source,  String find, String replaceWith) throws Exception {
		return source.replace(find, replaceWith);
	}

	public static void overlay( JSONObject source, JSONObject target) throws JSONException {
		if ( source == null || target == null ) {
			// no-op
			return;
		}
		overlay(source, target, null);
	}

	public static void overlay( JSONObject source, JSONObject target, String ... exclusions) throws JSONException {
		Iterator<String> keys = source.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (exclusions != null && Arrays.asList(exclusions).contains(key)) {
				return;
			}
			Object value = source.get(key);
			if (value != null) {
				target.put(key, value);
			}
		}
	}

	/**
	 * Load a java object from a json string - default typing disabled (doesn't use "type" attribute for class names)
	 *
	 * @param json
	 * @param type
	 * @return
	 */
	public static final <T> T objectFromString(String json, Class<T> type, Module... modules) throws IOException {
		return objectFromString(json, type, false, modules);
	}

	/**
	 * Creates java objects from json using the parameters provided
	 *
	 * @param json The raw json
	 * @param type The type of object to return
	 * @param defaultTyping Whether type attributes should be honored for non-concrete classes
	 * @param modules Custom modules to use during deserialization
	 * @return The object
	 * @throws IOException If anything goes wrong
	 */
	public static final <T> T objectFromString(String json, Class<T> type, boolean defaultTyping, Module... modules) throws IOException {
		return objectFromString(json, type, defaultTyping, true, modules);
	}
	public static final <T> T objectFromString(String json, Class<T> type, boolean defaultTyping, boolean epochTime, Module... modules) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

		//ignore unknown properties in the json
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//set up date format
		if (!epochTime) {
			//simple date format is not thread safe - TODO: refactor to a date formatter that is thread safe
			mapper.setDateFormat(new SimpleDateFormat(JSON_DATE_FORMAT.toPattern()));
		}

		if (defaultTyping) {
			mapper.enableDefaultTyping();
		}

		SimpleModule deserModule = new SimpleModule("deser");

		//if the type being deserialized is an enum, set that here
		if (Enum.class.isAssignableFrom(type)) {
			deserModule.addDeserializer(Enum.class, new EnumDeserializer(type));
		} else {
			deserModule.addDeserializer(Enum.class, new EnumDeserializer());
		}
		deserModule.addDeserializer(Instant.class, new InstantDeserializer(epochTime));
		deserModule.setKeyDeserializers(new DefaultKeyDeserializers());

		mapper.registerModule(deserModule);

		if (modules != null) {
			for (Module module : modules) {
				mapper.registerModule(module);
			}
		}

		return mapper.readValue(json, type);

	}

	/**
	 * Converts a json array to a list of values. The type of each array elements must exist in a "type" property providing
	 * the concrete subtype of the type passed in if it is an interface.
	 *
	 * @param json
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static final <T> List<T> listFromString(String json, Class<T> type) throws Exception {

		List<T> ret = new ArrayList<T>();

		//TODO: do this with jackson - i tried a little and it was failing even with type property
		JSONArray array = new JSONArray(json.toString());
    	for (int x=0; x<array.length(); x++) {
    		JSONObject jsonObject = array.getJSONObject(x);

    		//get a concrete type
    		Class<? extends T> concreteType = type;
    		if (type.isInterface()) {
    			concreteType = (Class<? extends T>)Class.forName(jsonObject.getString("type"));
    		}

    		T object = JSONUtils.objectFromString(jsonObject.toString(), concreteType);
    		ret.add(object);
    	}

    	return ret;

	}

	/**
	 * Convert a java object to a json string. Default typing flag is on, so types will be written out.
	 *
	 * @param json
	 * @param type
	 * @return
	 */
	public static final String objectToString(Object object, Module... modules) {

		if (object == null) {
			return null;
		}

		else if (object instanceof JSONObject || object instanceof JSONArray) { // || object instanceof String) {
			return object.toString();
		}

		else if (object instanceof JSONable) {
			return ((JSONable)object).toJSON();
		}

		return objectToString(object, false, modules);

	}

	/**
	 * Equivalent to calling objectToString(object, true, false, false, modules)
	 *
	 * @param object
	 * @param modules
	 * @return
	 */
	public static final String objectToStringEpochTime(Object object, Module... modules) {
		return objectToString(object, true, false, false, modules);
	}

	/**
	 * Load a java object from a json string.
	 *
	 * @param json
	 * @param type
	 * @return
	 */
	public static final String objectToString(Object object, boolean defaultTyping, Module... modules) {
		return objectToString(object, defaultTyping, false, modules);
	}

	public static final String objectToString(Object object, boolean defaultTyping, boolean ignoreJSONable, Module... modules) {
		return objectToString(object, true, defaultTyping, ignoreJSONable, modules);
	}

	/**
	 * Convert an object to a json string
	 *
	 * @param object The object to convert
	 * @param epochTime Whether to use epoch millis as the time
	 * @param defaultTyping
	 * @param ignoreJSONable
	 * @param modules
	 * @return
	 */
	public static final String objectToString(Object object, boolean epochTime, boolean defaultTyping, boolean ignoreJSONable, Module... modules) {

		try {

			if (object instanceof JSONObject || object instanceof JSONArray) {
				return object.toString();
			}

			//get our mapper
			ObjectMapper mapper = getMapper(epochTime, defaultTyping, ignoreJSONable, modules);

			//write out the object as json - for now formatted
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidFormatException("Can't serialize to json", e);
		}

	}

	/**
	 * Sort the json keys and return the string
	 *
	 * @param rawJson
	 * @return
	 * @throws Exception
	 */
	public static String toStringSorted(JSONObject json) throws Exception {

		return toStringSorted(json, true);

	}

	public static String toStringSorted(JSONObject json, boolean pretty) throws Exception {

		//get our mapper
		ObjectMapper mapper = getMapper(false, false, true);
		Object node = mapper.readValue(json.toString(), Object.class);

		//set up serialization
		mapper.configure(SerializationFeature.INDENT_OUTPUT, pretty);

		if (pretty) {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
		} else {

			return mapper.writeValueAsString(node);
		}

	}

	private static ObjectMapper getMapper(boolean epochTime, boolean defaultTyping, boolean ignoreJSONable, Module... modules) {

		//TODO: singleton
		ObjectMapper mapper = new ObjectMapper();

		//set up property naming strategy
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

		//omit null properties
		mapper.setSerializationInclusion(Include.NON_NULL);

		//set up serialization
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		//set up date format
		if (!epochTime) {
			mapper.setDateFormat(new SimpleDateFormat(JSON_DATE_FORMAT.toPattern()));
		}

		//we handle enums anyway so this probably isn't needed
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

		//consistent key order
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

		//don't write e notation
		mapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

		SimpleModule customModule = new SimpleModule("custom");
		customModule.addSerializer(Enum.class, new ToLowerSerializer(Enum.class));
		customModule.addSerializer(double.class, DOUBLE_SERIALIZER);
		customModule.addSerializer(Double.class, DOUBLE_SERIALIZER);
		customModule.addSerializer(Instant.class, new InstantSerializer(epochTime));

		if (!ignoreJSONable) {
			customModule.addSerializer(new JSONableSerializer());
		}

		mapper.registerModule(customModule);

		if (defaultTyping) {
			mapper.enableDefaultTyping();
		}

		if (modules != null) {
			for (Module module : modules) {
				mapper.registerModule(module);
			}
		}

		return mapper;

	}
	/**
	 * Assumes documents hold json. This method will merge the documents into a single json document.
	 * Priority of the resulting merge will be dependent on document creation date. Newer json
	 * will be take precedence over older json. Json arrays will be concatenated, but duplicates will
	 * be ignored.
	 *
	 * @param docs The documents whose json needs to be merged
	 * @return The merged json object
	 * @throws Exception
	 */
	public static org.codehaus.jettison.json.JSONObject merge(List<Document> docs) throws Exception {

		org.codehaus.jettison.json.JSONObject ret = new org.codehaus.jettison.json.JSONObject();

		Collections.sort(docs, Document.ORDER_BY_DATE);

		for (Document doc : docs) {

			merge(new org.codehaus.jettison.json.JSONObject(doc.getBodyAsString()), ret);

		}

		return ret;

	}

	//put all keys in source into target, adding to arrays and overwriting leaf values
	public static void merge(org.codehaus.jettison.json.JSONObject source, org.codehaus.jettison.json.JSONObject target) throws Exception {

		Iterator<String> keys = source.keys();
		while (keys.hasNext()) {

			String key = keys.next();
			if (target.has(key)) {

				//recurse into objects
				if (target.get(key) instanceof org.codehaus.jettison.json.JSONObject) {
					merge(source.getJSONObject(key), target.getJSONObject(key));
					continue;
				}

				//concatenate arrays
				else if (target.get(key) instanceof org.codehaus.jettison.json.JSONArray) {
					JSONArray result = concat(source.getJSONArray(key), target.getJSONArray(key));
					target.put(key, result);
					continue;
				}

			}

			//else just overwrite
			target.put(key, source.get(key));

		}

	}

	//add source elements to target array, ignore duplicates
	private static JSONArray concat(org.codehaus.jettison.json.JSONArray source, org.codehaus.jettison.json.JSONArray target) throws Exception {

		JSONArray result = new JSONArray();

		//no duplicate values with somewhat reasonable performance
		Set<String> values = new TreeSet<String>();

		//hash the target entries to detect duplicates
		for (int x=0; x<target.length(); x++) {
			values.add(target.get(x).toString());
		}

		for (int x=0; x<source.length(); x++) {
			Object obj = source.get(x);
			if (values.add(obj.toString())) {
				result.put(obj);
			}
		}

		//add all of target after
		for (int x=0; x<target.length(); x++) {
			result.put(target.get(x));
		}

		return result;

	}

	/**
	 * Objects can be JSONObject or String types.
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean jsonEquals(Object o1, Object o2) throws Exception {

		String s1 = o1.toString();
		String s2 = o2.toString();

		ObjectMapper mapper = new ObjectMapper();
		JsonNode fromString1 = mapper.readTree(s1);
		JsonNode fromString2 = mapper.readTree(s2);

		return fromString1.equals(fromString2);

	}

	/**
	 * Default JSON date format
	 */
	public static final SimpleDateFormat JSON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	/**
	 * Update an existing object from json
	 *
	 * @param json The raw json string
	 * @param toUpdate The object to update
	 * @throws IOException If anything goes wrong
	 */
	public static void updateFromString(String json, Object toUpdate) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		//set up property naming strategy
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        //ignore unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);



        //TODO: allow this to be passed in
        mapper.setDateFormat(new SimpleDateFormat(JSON_DATE_FORMAT.toPattern()));

		SimpleModule deserModule = new SimpleModule("deser");
		deserModule.addDeserializer(Enum.class, new EnumDeserializer());
		deserModule.addDeserializer(Instant.class, new InstantDeserializer(true));
		deserModule.setKeyDeserializers(new DefaultKeyDeserializers());
		mapper.registerModule(deserModule);

        ObjectReader reader = mapper.readerForUpdating(toUpdate);
        reader.readValue(json);
	}

	public static HashMap<String, Object> jsonToMap(Object o) throws JSONException {

	    HashMap<String, Object> map = new HashMap<String, Object>();

	    JSONObject jObject = null;
	    if (o instanceof JSONObject) {
	    	jObject = (JSONObject)o;
	    }

	    else {
	    	jObject = new JSONObject(String.valueOf(o));
	    }

	    Iterator<?> keys = jObject.keys();

	    while( keys.hasNext() ){
	        String key = (String)keys.next();
	        Object value = jObject.get(key);
	        map.put(key, value);


	    }

	    return map;


	}





}
