package com.aw.unity.query;

import com.aw.common.util.JSONUtils;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.exceptions.InvalidQueryException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A query attribute is a value requested in a query, i.e. a column. The value might be a dimension (i.e. property) of the data, or it might be a measure
 * (i.e. count, count distinct, sum) of the data.
 *
 *
 *
 */
public class QueryAttribute {

  /**
   * Ordering supported per query attribute
   */
  public enum Order {
	  ASC,
	  DESC,
	  NONE;

	  @JsonCreator
	  public static Order forValue(String val) { return Order.valueOf(val.toUpperCase()); }
  }

  /**
   * Aggregate functions available
   * TODO: make date formats configurable eventually, and these would become the defaults
   */
  public enum Aggregate {

	  //timestamp aggregates - only valid on timestamps
	  SECOND("yyyy-MM-dd hh:mm:ss a"),
	  MINUTE("yyyy-MM-dd hh:mm a"),
	  HOUR("yyyy-MM-dd hh a"),
	  DAY("yyyy-MM-dd"),
	  WEEK("yyyy-MM-dd"),
	  QUARTER("yyyy-MM"),
	  MONTH("yyyy-MM"),
	  YEAR("yyyy"),

	  //general aggregate types
	  NONE,
	  SUM,
	  AVG,
	  MIN,
	  MAX,
	  COUNT_DISTINCT, //count distinct
	  COUNT;

	  private Aggregate() {
		  m_dateFormat = null;
	  }

	  private Aggregate(String dateFormat) {
		m_dateFormat = dateFormat;
	  }

	  public String getDateFormat() { return m_dateFormat; }
	  private String m_dateFormat;

	  @JsonCreator
	  public static Aggregate forValue(String val) { return Aggregate.valueOf(val.toUpperCase()); }
  }

   public QueryAttribute() {
   }

   public QueryAttribute(Field field, Order order, Aggregate aggregate) {
	   m_field = field;
	   m_order = order;
	   m_aggregate = aggregate;
   }

   /**
    * @return The type of the field reference by the query attribute
    */
   public FieldType getColDataType() {
	   return m_field.getType();
   }

   //copy constructor
   private QueryAttribute(QueryAttribute attrib) {
	   m_aggregate = attrib.m_aggregate;
	   m_constant = attrib.m_constant;
	   m_field = attrib.m_field;
	   m_order = attrib.m_order;
   }

   /**
    * @param value
    * @return A version of this attribute with a constant value
    */
   public QueryAttribute constant(String value) {
	   QueryAttribute ret = new QueryAttribute(this);
	   ret.m_constant = value;
	   return ret;
   }

   /**
    * @param name The new name of this attribute
    * @return A version of this attribute with a new name. The original attribute is not modified
    */
   public QueryAttribute changeField(Field field) {
	   QueryAttribute ret = new QueryAttribute(this);
	   ret.m_field = field;
	   return ret;
   }

   @Override
	public String toString() {
	   return JSONUtils.objectToString(this);
	}

   /**
    * Validate the attribute
    */
   public void validate() {

	   validateField();
	   validateAggregate();

   }

   private void validateAggregate() {

	   //null aggregate is always ok
	   if (m_aggregate == null) {
		   return;
	   }

	   //make sure the aggregate type is valid considering the field type
	   switch (m_aggregate) {
		   case NONE:
			   //none is always valid
			   break;
		   case SECOND:
		   case MINUTE:
		   case HOUR:
		   case DAY:
		   case WEEK:
		   case MONTH:
		   case QUARTER:
		   case YEAR:
			   if (m_field.getType() != FieldType.TIMESTAMP) {
				   throw new InvalidQueryException("query field " + m_field + " aggregate type " + m_aggregate.name().toLowerCase() + " is not allowed - only timestamps can use this aggregate type");
			   }
			   break;
		   default:
			   if (m_field.getType() == FieldType.TIMESTAMP) {
				   throw new InvalidQueryException("query field " + m_field + " is a timestamp - invalid aggregate: " + m_aggregate.name().toLowerCase() + " - only valid aggregate types are: " +
				       Aggregate.SECOND.name().toLowerCase() + ", " +
				       Aggregate.MINUTE.name().toLowerCase() + ", " +
				       Aggregate.HOUR.name().toLowerCase() + ", " +
				       Aggregate.WEEK.name().toLowerCase() + ", " +
				       Aggregate.DAY.name().toLowerCase() + ", " +
				       Aggregate.QUARTER.name().toLowerCase() + ", " +
				       Aggregate.MONTH.name().toLowerCase() + ", " +
				       Aggregate.YEAR.name().toLowerCase()
				   );
			   }
			   break;
	   }

   }

   private void validateField() {

	   if (m_field == null) {
		   throw new InvalidQueryException("query attribute field cannot be missing");
	   }

   }

   /**
    * @return Any ordering applied to this attribute
    */
   public Order getOrder() { return m_order; }
   public void setOrder(Order order) { m_order = order; }
   private Order m_order = Order.NONE;

   /**
    * @return The field in the data model referenced by this attribute
    */
   @JsonIgnore
   public Field getField() { return m_field; }
   @JsonIgnore
   public void setField(Field field) { m_field = field; }
   @JsonIgnore
   private Field m_field;

   /**
    * @return The aggregate function used for this attribute.
    */
   public Aggregate getAggregate() { return m_aggregate; }
   public void setAggregate(Aggregate aggregate) { m_aggregate = aggregate; }
   private Aggregate m_aggregate;

   /**
    * If set, field is ignored and this value is used instead
    *
    * @return The constant literal value to use for this query attribute
    */
   public String getConstant() { return m_constant; }
   public void setConstant(String constant) { m_constant = constant; }
   private String m_constant = null;

}
