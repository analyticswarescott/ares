package com.aw.unity.query.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aw.unity.Data;
import com.aw.unity.Field;
import com.aw.unity.FieldType;
import com.aw.unity.exceptions.InvalidFilterException;
import com.aw.unity.query.AbstractFilter;
import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterConstraint;
import com.aw.unity.query.OpHandler;

/**
 * Base class for constraints.
 *
 *
 *
 */
public abstract class AbstractFilterConstraint extends AbstractFilter implements FilterConstraint {

	public AbstractFilterConstraint() {
	}

	public AbstractFilterConstraint(Field field) {
		setField(field);
		initialize();
	}

	@Override
	public Collection<FilterConstraint> findAll(Field field) {
		if (field.equals(getField())){
			return Collections.singleton(this);
		} else {
			return Collections.emptySet();
		}
	}

	//set our custom handler flag on initialization
	private void initialize() {
		FieldType type = getField().getType();
		if (type.hasOpHandlers()) {
			m_customHandler =  type.getOpHandlers().get(getOperator());
		}
	}

	/**
	 * Apply the custom handler for thie operator/field type combination. Will throw an exception if there is
	 * no custom handler.
	 *
	 * @param lValue The value from the data
	 * @param rValues The value(s) in the filter
	 * @return Whether the custom operator handler matched
	 */
	private boolean applyCustomHandler(Object lValue) {
		if (!hasCustomHandler()) {
			throw new InvalidFilterException("asked to apply a custom filter op handler when we don't have one for this fieldType/operator combination.");
		}
		return m_customHandler.match(lValue, getValues());
	}

	/**
	 * @return Whether we have a custom handler for this constraint / field type combination.
	 */
	protected boolean hasCustomHandler() {
		if (!m_initialized) {
			initialize();
			m_initialized = true;
		}
		return m_customHandler != null;
	}
	protected OpHandler getCustomHandler() { return m_customHandler; }
	private OpHandler m_customHandler = null;
	private boolean m_initialized = false;

	//we never have any child filters
	@Override
	public List<Filter> getFilters() {
		return Collections.emptyList();
	}

	//TODO: find out if we need this
	public String getSQL() {
		throw new UnsupportedOperationException("SQL not supported");
	}

	public final boolean match(Data data) {

		//apply custom operator handling if applicable
		if (hasCustomHandler()) {
			Object value = data.getValue(getField());
			return applyCustomHandler(value);
		}

		//otherwise apply default handling
		else {
			return matchDefault(data);
		}

	}

	public void setValues(Collection<Object> values) {
		m_values = values;

		if (!hasCustomHandler()) {
			setValuesDefault(values);
		}
	}

	/**
	 * If a custom operator handler isn't installed, provide default behavior for setting values
	 *
	 * @param values
	 */
	protected abstract void setValuesDefault(Collection<Object> values);

	/**
	 * If a custom operator handler isn't installed, default behavior must be defined for this
	 * constraint operator type. This method must be implemented by all subclasses in order to
	 * provide that implementation.
	 *
	 * @param data The data to match
	 * @return Whether the data matched the constraint
	 */
	protected abstract boolean matchDefault(Data data);

	@Override
	public void validate() {
		validate("");
	}

	@Override
	public void validate(String path) {

		//build the path to this constraint
		path = path + " -> (" + printCanonical() + ")";

		//by default don't allow null operator, field, or values
		if (getOperator() == null) {
			throw new InvalidFilterException("constraint operator cannot be missing", path);
		}

		if (getValues() == null) {
			throw new InvalidFilterException("constraint values cannot be missing", path);
		}

		if (getField() == null) {
			throw new InvalidFilterException("constraint field cannot be missing", path);
		}

	}

	/**
	 * Constraints, for now, are not deep copied during a deep copy of a filter, only the group structure
	 */
	public Filter deepCopy() {

		return this;

	}

	@Override
	public String toString() {
		return printCanonical();
	}

	/**
     * @return The field to which this constraint applies
     */
    public Field getField() { return m_field; }
	public void setField(Field field) {
		m_field = field;
		if (m_field == null) {
			throw new InvalidFilterException("constraint cannot have a null field");
		}
	}
	private Field m_field;

	/**
	 * Is the constraint enabled
	 */
	public boolean isEnabled() { return m_enabled; }
	public void setEnabled(boolean enabled) { m_enabled = enabled; }
	private boolean m_enabled = true;

	/**
	 * Is this constraint NOT'ed
	 */
	public boolean isNot() { return m_not; }
	public void setNot(boolean not) { m_not = not; }
	private boolean m_not = false;

	public Collection<Object> getValues() { return m_values; }
	private Collection<Object> m_values = new ArrayList<Object>();

}
