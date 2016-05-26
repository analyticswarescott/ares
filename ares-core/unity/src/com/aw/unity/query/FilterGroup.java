package com.aw.unity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.exceptions.InvalidFilterException;

/**
 * TODO: get JSON out of here
 *
 * Created by scott on 09/10/15.
 * Modified by jlehmann on 10/20/15.
 */
public class FilterGroup extends AbstractFilter implements Filter {

	public enum GroupOperator {
		AND("AND"),
		OR("OR");

		GroupOperator(String operator){
			this.operator = operator;
		}

		public static GroupOperator fromString(String str) {
			try {
				 return GroupOperator.valueOf(str.toUpperCase());
			} catch (Exception e) {
				return null;
			}
		}

		private final String operator;
		public String getOperator() {
			return operator;
		}
	}

	//TODO: make this an enum
	public static final String ATTRIB_GROUP_OPERATOR = "operator";
	public static final String ATTRIB_GROUP_FILTERS = "filter";

	public static final String ATTRIB_ENABLED = "enabled";

	private List<Filter> m_filters = new ArrayList<Filter>();
	public List<Filter> getFilters() {
		return m_filters;
	}

	/**
	 * Creates an empty filter
	 */
	public FilterGroup() {
	}

	public FilterGroup(GroupOperator op, List<Filter> filters) {
		m_operator = op;
		m_filters.addAll(filters);
	}

	public FilterGroup(GroupOperator op) {
		this(op, Collections.emptyList());
	}

	/**
	 * find a constraint for the given field
	 *
	 * @param field the field to look for
	 * @return the constraint for this field
	 */
	public Collection<FilterConstraint> findAll(Field field) {

		Collection<FilterConstraint> ret = new HashSet<FilterConstraint>();
		Utils.findAll(ret, this, field);
		return ret;

	}

	public String printCanonical() {
		StringBuilder ret = new StringBuilder();

		for (Filter f : m_filters) {
			if (ret.length() == 0) {
				ret.append("( ");
			}

			else if (ret.length() > 0) {
				ret.append(" ");
				ret.append(m_operator.getOperator());
				ret.append(" ");
			}

			ret.append(f.printCanonical());
		}

		ret.append(" )");

		return ret.toString();
	}

	public FilterGroup addFilter(Filter filter) {
		m_filters.add(filter);
		return this;
	}

	@Override
	public void validate() {

		validate("");

	}

	public void validate(String path) {

		//operators can never be missing
		if (m_operator == null) {
			throw new InvalidFilterException("group operator cannot be null on a filter", path);
		}

		//make sure children are ok
		int index = 0;
		for (Filter filter : m_filters) {
			filter.validate(path + "[" + index++ + "]");
		}

	}

	public boolean match(Data data) {

		//return true for empty object
		if (m_filters.size() == 0){
			return true;
		}

		for (Filter f : m_filters) {
			boolean b = f.match(data);

			//apply not
			if (f.isNot()) {
				b = !b;
			}

			//trigger return on OR+true  and  AND+false
			if (b && m_operator == GroupOperator.OR) {
				return true;
			}
			if (!b && m_operator == GroupOperator.AND) {
				return false;
			}
		}
		//if nothing triggered, then AND must be true and OR must be false
		if (m_operator == GroupOperator.OR) {
			return false;
		}
		else if (m_operator == GroupOperator.AND ) {
			return true;
		}
		else {throw new RuntimeException(" unsupported filter logic operator " + m_operator.getOperator());}

	}

	public Filter deepCopy() {

		FilterGroup ret = new FilterGroup();
		for (Filter filter : ret.m_filters) {
			ret.addFilter(filter.deepCopy());
		}

		return ret;

	}

	@Override
	public String toString() {
		return printCanonical();
	}

	public GroupOperator getOperator() { return m_operator; }
	public void setOperator(GroupOperator operator) { m_operator = operator; }
	private GroupOperator m_operator = GroupOperator.AND;

	public boolean isEnabled() { return m_enabled; }
	public void setEnabled(boolean enabled) { m_enabled = enabled; }
	private boolean m_enabled = true;

	/**
	 * Whether this filter's logic is NOT'd - defaults to false
	 */
	public boolean isNot() { return m_not; }
	public void setNot(boolean not) { m_not = not; }
	private boolean m_not = false;

}
