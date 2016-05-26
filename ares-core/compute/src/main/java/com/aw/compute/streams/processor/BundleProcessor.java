package com.aw.compute.streams.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.spark.StreamDef;
import com.aw.common.util.ResourceManager;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.Dependent;
import com.aw.compute.referencedata.ReferenceDataManager.ReferenceDataType;
import com.aw.compute.referencedata.ReferenceDataMap;
import com.aw.compute.streams.exceptions.ProcessorInitializationException;
import com.aw.compute.streams.processor.framework.StringTupleProcessor;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformMgr;
import com.aw.unity.DataType;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.UnityInstance;
import com.aw.unity.dg.CommonField;
import com.aw.unity.json.JSONDataType;
import com.aw.unity.json.JSONFieldRef;
import com.aw.util.DateUtil;
import com.aw.util.TwoKeyMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import kafka.producer.KeyedMessage;

/**
 * Base class for processing bundle data in Spark.
 *
 *
 *
 */
public abstract class BundleProcessor<T extends BundleContext, V> implements StringTupleProcessor, Dependent {

	private static final long serialVersionUID = 1L;

	public static final Logger logger = LoggerFactory.getLogger(BundleProcessor.class);

	protected static final String EVENT_ID = "meid";
	protected static final String DETAIL_ID = "medid";
	protected static final String TIME_ID = "esu";

	private static final String UNKNOWN_TYPE = "0";

	public static final String DETAIL_TYPE_SUFFIX = "_detail";

	static final String SQUIRLY_OPEN = "{";
	static final String SQUIRLY_CLOSE = "}";

	private static final String PREFIX = "aw";

	/**
	 * Mappings of sections of the bundle to their key field to lookup the corresponding entry.
	 *
	 * For each field in an event record (uad/ua), there will be a field name in the value list of this map. The
	 * keys correspond to the element name of the reference data within the bundle.
	 */
	public final static Map<String, List<String>> KEY_FIELDS = ImmutableMap.<String, List<String>>builder()
		.put("di", ImmutableList.of("imageMD5"))
		.put("pi", ImmutableList.of("ph", "md5"))
		.put("dev", ImmutableList.of("dh"))
		.put("ua", ImmutableList.of(EVENT_ID)) //inject UA data into each UAD to build each machine event (for now)
		.put("aw", ImmutableList.of(EVENT_ID)) //inject standard dg_* fields from the UA -> UAD
	.build();

	/**
	 * Array id mappings - in cases where we are parsing a field with a particular array id (arrayId_field format),
	 * the id might need to map to another value - this map provides that mapping.
	 */
	public final static Map<String, String> ARRAY_ID_MAPPING = ImmutableMap.<String, String>builder()
		.put("pi", "pi")
		.put("di", "di")
		.put("ua", "ua")
	.build();

	/**
	 * in some cases we need to change names of fields - this is a mapping of these field that should be changed
	 */
	public final static Map<String, String> STATIC_NAME_MAP = ImmutableMap.<String, String>builder()
		.put("p", "policy")
	.build();

	/**
	 * mapping of data type name to aw fiawd prefix
	 */
	public final static Map<String, String> DATATYPE_TO_FIELD_PREFIX = ImmutableMap.<String, String>builder()
		.put("policy", "p")
		.put("alert", "alert")
	.build();

	/**
	 * base operation type field used to determine the unity type for the resulting events from the bundle
	 */
	private static String OPERATION_TYPE = "ot";

	//bundle event types
	protected static final String UA = "ua";
	protected static final String UAD = "uad";

	public Topic getDestTopic() { return destTopic; }
	Topic destTopic;

	/**
	 * Output the keyed message
	 *
	 * @param km The keyed message to output
	 */
	protected void output(KeyedMessage<String, String> km) throws Exception {

		ResourceManager.KafkaProducerSingleton.getInstance(ComputeInjector.get().getInstance(Platform.class)).send(km);

	}

	@Override
	public void init(StreamDef streamDef) throws ProcessorInitializationException {
		logger.debug("initializing processor " + this.getClass().getTypeName());

		try {

			destTopic = streamDef.getDestTopic();

			SecurityUtil.setThreadSystemAccess();

		}
		catch (Exception ex) {
			throw new ProcessorInitializationException(ex.getMessage(), ex);
		}

	}

	protected void processBundle(T ctx) throws Exception {

		//cache the bundle information by type -> key -> object (pi, ua, uad, etc)
		TwoKeyMap<String, String, V> referenceData = cacheByTypeAndKey(ctx);

		//add machine events
		List<V> uads = getUads(ctx);
		if (uads != null && uads.size() > 0) {
			createMachineEvents(ctx, UAD, uads, referenceData);
		}

	}

	/**
	 * Build the reference map of the type specific data from the bundle
	 *
	 * @return The reference data from the bundle
	 * @throws Exception
	 */
	protected abstract TwoKeyMap<String, String, V> cacheByTypeAndKey(T ctx) throws Exception;

	/**
	 * @return The list of uad elements from the bundle
	 * @throws Exception
	 */
	protected abstract List<V> getUads(T ctx) throws Exception;

	/**
	 * Get a string, or the default value if the key isn't found
	 *
	 * @param element The element that may be holding the key
	 * @param key The key that may be held by the element
	 * @param def The default value that will be returned if the element does not contain the key
	 * @return The key or the default value
	 */
	protected abstract String getValue(V element, String key, String def) throws Exception;

	/**
	 * Get a string, or the default value if the key isn't found
	 *
	 * @param element The element that may be holding the key
	 * @param key The key that may be held by the element
	 * @param def The default value that will be returned if the element does not contain the key
	 * @return The key or the default value
	 */
	protected abstract Object getValue(V element, String key, Object def) throws Exception;


	/**
	 * get a value given the key
	 *
	 * @param element the element holding the value
	 * @param key the key for the value
	 * @return the value
	 * @throws Exception if anything goes wrong
	 */
	protected abstract Object getValue(V element, String key) throws Exception;

	/**
	 * Get a collection of values given the key
	 *
	 * @param element the element that holds the collection of values
	 * @param key the key for the values
	 * @return the values
	 * @throws Exception if anything goes wrong
	 */
	protected abstract Collection<Object> getValues(V element, String key) throws Exception;

	/**
	 * get a complex sub object from the parent object
	 *
	 * @param element the parent object
	 * @param key the key for the sub object
	 * @return the sub object
	 * @throws Exception if anything goes wrong
	 */
	protected abstract V getElement(V element, String key) throws Exception;

	/**
	 * get a collection of complex sub objects from the parent object
	 *
	 * @param element the parent object
	 * @param key the key to the sub objects
	 * @return the sub objects
	 * @throws Exception if anything goes wrong
	 */
	protected abstract Collection<V> getElements(V element, String key) throws Exception;

	/**
	 * @param element The element whose keys are needed
	 * @return An iterable of the string keys for the given element
	 */
	protected abstract Iterator<String> keys(V element);

	/**
	 * Build machine events from the array, using the reference data provided
	 *
	 * @param elements
	 * @param referenceData
	 * @throws Exception
	 */
	private void createMachineEvents(T ctx, String id, List<V> uads, TwoKeyMap<String, String, V> referenceData) throws Exception{

		//for each element, apply reference data and transforms based on unity type
		for (V element : uads) {

			//get the event name first to determine unity type
			String otRaw = getValue(element, OPERATION_TYPE, UNKNOWN_TYPE);
			String oType = getOperationTypeMap().get(otRaw);

			//if ot type is null, get the ua's ot type as the type
			if (oType == null) {

				String eventId = getValue(element, EVENT_ID, UNKNOWN_TYPE);

				//if we have an event id, try to get the event
				V event = null;
				if (eventId != null) {

					event = referenceData.get(UA, eventId.toUpperCase());

					//if we have the event, try to get the ot value
					otRaw = getValue(event, OPERATION_TYPE, UNKNOWN_TYPE);

				}

				oType = getOperationTypeMap().get(otRaw);

			}

			//get the data type name
			String strDataType = oType;

			//now get it from unity
			DataType dataType = ComputeInjector.get().getInstance(UnityInstance.class).getMetadata().getDataType(strDataType);

			if (dataType == null) {

				//just log for now TODO: keep statistics on this and let the platform know about it
				ComputeInjector.get().getInstance(PlatformMgr.class).handleError("could not determine unity type from ot value: " + otRaw + " (dataType=" + strDataType + ")", NodeRole.SPARK_WORKER);

			}

			else {

				//now build the event using the information we have
				JSONObject machineEvent = buildEvent(id, dataType, element);

				//inject the reference data as necessary
				injectReferenceData(id, dataType, machineEvent, referenceData, element);

				//add fields that always need to be there
				machineEvent.put(CommonField.DG_BID.toString(), ctx.getBundleID());
				machineEvent.put(CommonField.DG_MID.toString(), ctx.getMachineID());
				machineEvent.put(CommonField.DG_UTYPE.toString(), strDataType);

				KeyedMessage<String, String> km = new KeyedMessage<String, String>(Topic.toTopicString(ctx.getTenantID(), Topic.MACHINE_EVENT), ctx.getTenantID(), machineEvent.toString());

				//send it to kafka
				output(km);

			}

		}

	}

	/**
	 * Build a single event given the raw json and reference data
	 *
	 * @param dataType
	 * @param element
	 * @param referenceData
	 * @return
	 * @throws Exception
	 */
	private JSONObject buildEvent(String prefix, DataType dataType, V element) throws Exception {

		JSONDataType jsonDataType = (JSONDataType)dataType;
		JSONObject ret = new JSONObject();

		Iterator<String> keys = keys(element);
		while (keys.hasNext()) {
			String rawKey = keys.next();
			String fieldKey = rawKey;

			//look up in static mapping - if there, use the mapped name
			if (STATIC_NAME_MAP.containsKey(fieldKey)) {
				fieldKey = STATIC_NAME_MAP.get(fieldKey);
			}

			//build the unity name, which we know is the prefix + "_" + rawName to keep things globally unique
			String key = toFieldName(prefix, fieldKey);

			//check common fields if current prefix doesn't resolve
			if (!jsonDataType.hasPath(key)) {
				key = toFieldName(PREFIX, fieldKey);
			}

			//see if the field should be injected according to unity
			if (jsonDataType.hasPath(key)) {

				Field field = jsonDataType.getFieldFromPath(key);

				//based on the type of field, get data from the element
				Object value = null;
				switch (field.getType()) {
					case OBJECT:

						DataType subType = dataType.getUnity().getDataType(field.getDataType());

						//support arrays of complex sub objects TODO: always the same prefix?
						if (field.isArray()) {
							JSONArray array = new JSONArray();
							for (V subObject : getElements(element, rawKey)) {
								array.put(buildEvent(DATATYPE_TO_FIELD_PREFIX.get(field.getDataType()), subType, subObject));
							}
							value = array;
						}

						//build a json array of sub objects
						else {
							value = buildEvent(prefix, subType, getElement(element, rawKey));
						}

						break;

					default:

						//support arrays of scalars
						if (field.isArray()) {
							JSONArray array = new JSONArray();
							for (Object subObject : getValues(element, rawKey)) {
								array.put(subObject);
							}
							value = array;
						}

						else {
							value = getValue(element, rawKey);
						}
				}
				setValue(ret, key, value, jsonDataType.getFieldFromPath(key));

			}

		}

		//we now have a built object
		return ret;

	}

	/**
	 * Common way to build a candidate field using a prefix and raw name
	 *
	 * @param prefix The field prefix, usually indicates a category of field
	 * @param rawKey The raw field name
	 * @return
	 */
	protected static String toFieldName(String prefix, String rawKey) {
		return prefix + "_" + rawKey;
	}

	/**
	 * Take a raw machine event and inject the required reference data
	 *
	 * @param dataType
	 * @param machineEvent
	 * @param referenceData
	 * @throws Exception
	 */
	private void injectReferenceData(String id, DataType dataType, JSONObject machineEvent, TwoKeyMap<String, String, V> referenceData, V rawEvent) throws Exception {

		JSONDataType jsonDataType = (JSONDataType)dataType;

		//keep track of what we injected to recurse
		Map<String, V> injected = new HashMap<>();

		//for the fields in the type, get them from the data in the bundle
		for (JSONFieldRef ref : jsonDataType.getFieldRefs()) {

			//get the unity field for this json reference
			Field field = dataType.getField(ref.getName());

			//parse the components from the json name
			String name = ref.getPath()[0];

			//check the field
			int separator = name.indexOf('_');

			//pi, di, etc
			String arrayId = name.substring(0, separator);

			//field within that type that we need
			String rawName = name.substring(separator + 1);

			if (KEY_FIELDS.containsKey(arrayId)) {

				for (String keyField : KEY_FIELDS.get(arrayId)) {
					inject(machineEvent, arrayId, rawEvent, rawName, name, keyField, field, referenceData, injected);
				}

			}

		}

		//now for all injected objects, recurse to make sure we handle reference object chaining (e.g. uad -> ua -> pi)
		for (String arrayId : injected.keySet()) {
			if (!arrayId.equals(id)) {
				//we just added a value to the machine event - now try to inject with this reference object as the raw object to see if we need to chain reference data
				injectReferenceData(arrayId, dataType, machineEvent, referenceData, injected.get(arrayId));
			}
		}

	}

	private void inject(JSONObject machineEvent, String arrayId, V rawEvent, String rawName, String name, String keyField, Field field, TwoKeyMap<String, String, V> referenceData, Map<String, V> injected) throws Exception {

		//get the key to the reference data from the raw event
		String value = getValue(rawEvent, keyField, null);
		if (value != null) {

			//get the reference object
			V refObject = tryToGetWithReferenceKey(field, referenceData, ARRAY_ID_MAPPING.get(arrayId), value);

			//inject the reference data value from the reference object
			Object injectionValue = getTryLower(refObject, rawName);

			//it's not always there - pi -> ema is listed in DGRuleMeta.xml as being a field, but it's not in our test bundle
			if (injectionValue != null) {

				if (field.getType() == FieldType.OBJECT) {

					//if it's a collection of objects, build an array
					if (field.isArray()) {
						injectionValue = toArray(arrayId, field.getUnity().getDataType(field.getDataType()), (Collection<V>)injectionValue);
					}

					else {

						//otherwise build a single object
						injectionValue = buildEvent(arrayId, field.getUnity().getDataType(field.getDataType()), (V)injectionValue);

					}

				}

				setValue(machineEvent, name, injectionValue, field);

				//don't chain into aw data
				if (!arrayId.equals(PREFIX)) {
					injected.put(arrayId, refObject); //we don't inject different reawrence objects for the same array id
				}

			}

		}

	}

	private void setValue(JSONObject object, String fieldName, Object value, Field field) throws Exception {

		if (field.getType() == FieldType.TIMESTAMP) {

			//convert to epoch millis
			object.put(fieldName, DateUtil.fromMSToUnixTime(Long.parseLong(String.valueOf(value))));

		}

		else {

			//build the object from the unity key - get from the raw json using the raw key
			object.put(fieldName,  value);

		}

	}

	private JSONArray toArray(String prefix, DataType dataType, Collection<V> values) throws Exception {
		JSONArray ret = new JSONArray();
		for (V value : values) {
			ret.put(buildEvent(prefix, dataType, value));
		}
		return ret;
	}

	/**
	 * We connect reference data with the events by key values. With the way the bundle json works right now, some of the keys are not in
	 * the same format between reference data (PI for example) and the events (UA for example). An example of this problem would be the fact
	 * that some md5 hashes start and end with brackets '{}' and are lower case, while others are upper case without brackets.
	 *
	 * @param field
	 * @param referenceData
	 * @param arrayId
	 * @param value
	 * @return
	 */
	private V tryToGetWithReferenceKey(Field field, TwoKeyMap<String, String, V> referenceData, String arrayId, String value) {

		//ignore case
		value = value.toUpperCase();

		V refObject = referenceData.get(arrayId, value);

		//try to strip squirlies if it's a GUID
		if (refObject == null) {

			//whatever the state is of the value, reverse it.. (if it has squirlies, remove them, etc)
			if (value.startsWith(SQUIRLY_OPEN)) {
				value = value.substring(1, value.length() - 1);
			}

			else {
				value = SQUIRLY_OPEN + value + SQUIRLY_CLOSE;
			}

			refObject = referenceData.get(arrayId, value);

		}

		return refObject;

	}

	//get the value for the key - if not there, try lowercase (seems there's a  bug in DGRuleMeta.xml where SHA1 for pi should be sha1
	private Object getTryLower(V object, String key) throws Exception {
		Object ret = null;
		if (object != null) {
			ret = getValue(object, key, (Object)null);
			if (ret == null) {
				ret = getValue(object, key.toLowerCase(), (Object)null);
			}
		}
		return ret;
	}

	//used to resolve to operation type string
	protected ReferenceDataMap<String, String> getOperationTypeMap() throws Exception {

		//lazily load the reference data because we're in spark and can't be sure this property is set yet
		if (m_otMap == null) {
			m_otMap = ComputeInjector.get().getInstance(ReferenceDataType.OT_DESCRIPTION_LOOKUP.getType());
		}

		return m_otMap;

	}

	transient ReferenceDataMap<String, String> m_otMap = null;

}


