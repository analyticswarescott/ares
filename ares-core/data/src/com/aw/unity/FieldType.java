package com.aw.unity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aw.unity.query.ConstraintOperator;
import com.aw.unity.query.OpHandler;
import com.aw.unity.query.ValueParser;
import com.aw.unity.query.datatypes.BooleanValueParser;
import com.aw.unity.query.datatypes.DoubleValueParser;
import com.aw.unity.query.datatypes.FloatValueParser;
import com.aw.unity.query.datatypes.IntValueParser;
import com.aw.unity.query.datatypes.IpInHandler;
import com.aw.unity.query.datatypes.IpValueParser;
import com.aw.unity.query.datatypes.LongValueParser;
import com.aw.unity.query.datatypes.ObjectParser;
import com.aw.unity.query.datatypes.StringValueParser;
import com.aw.unity.query.datatypes.TimestampBetweenHandler;
import com.aw.unity.query.datatypes.TimestampGteLteHandler;
import com.aw.unity.query.datatypes.TimestampInEqHandler;
import com.aw.unity.query.datatypes.TimestampValueParser;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The known types in the system.
 *
 *
 *
 */
public enum FieldType {

	STRING(new StringValueParser()),

	INT(new IntValueParser()),

	LONG(new LongValueParser()),

	FLOAT(new FloatValueParser()),

	DOUBLE(new DoubleValueParser()),

	IP_ADDRESS(new IpValueParser(),
				new IpInHandler()), //both v4 and v6

	MAC_ADDRESS(new StringValueParser()), //TODO: type this

	GEO_LOCATION(new StringValueParser()), //TODO: type this

	BOOLEAN(new BooleanValueParser()),

	TIMESTAMP(new TimestampValueParser(),
		   	  	new TimestampBetweenHandler(),
		   	  	new TimestampInEqHandler(ConstraintOperator.IN),
		   	  	new TimestampInEqHandler(ConstraintOperator.EQ),
		   	  	new TimestampGteLteHandler(ConstraintOperator.GTE),
		   	  	new TimestampGteLteHandler(ConstraintOperator.LTE)),

	/**
	 * Complex sub object
	 */
	OBJECT(new ObjectParser()),

	GUID(new StringValueParser());

	 //TODO: more types..

	FieldType(ValueParser valueParser, OpHandler... opHandlers) {

		setValueParser(valueParser);
		if (opHandlers != null) {
			for (OpHandler h : opHandlers) {
				m_opHandlers.put(h.getOperator(), h);
			}
		}

	}

	public Object parse(DataType parentType, Object data) {
		return getValueParser().parse(parentType, null, data);
	}

	public Object parse(DataType parentType, Field field, Object data) {
		return getValueParser().parse(parentType, field, data);
	}

	public Object parse(String str) {
		return parse((DataType)null, str);
	}

	public Object parse(ConstraintOperator op, String str) {
		return getValueParser().parse(op, Collections.singletonList(str));
	}

	public boolean hasOpHandlers() {
		return m_opHandlers.size() > 0;
	}

	/**
	 * @return Parses raw strings to types objects for this field type
	 */
	public ValueParser getValueParser() { return m_valueParser; }
	public void setValueParser(ValueParser valueParser) { m_valueParser = valueParser; }
	private ValueParser m_valueParser;

	/**
	 * @return Custom operator handlers, keyed by the ConstraintOperator they handle.
	 */
	public Map<ConstraintOperator, OpHandler> getOpHandlers() { return m_opHandlers; }
	public void setOpHandlers(Map<ConstraintOperator, OpHandler> opHandlers) { m_opHandlers = opHandlers; }
	private Map<ConstraintOperator, OpHandler> m_opHandlers = new HashMap<ConstraintOperator, OpHandler>();

	@JsonCreator
	public static FieldType forValue(String value) { return FieldType.valueOf(value.toUpperCase()); }

}
