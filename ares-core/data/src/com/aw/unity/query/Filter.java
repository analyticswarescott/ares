package com.aw.unity.query;

import java.util.Collection;
import java.util.List;

import com.aw.common.Validatable;
import com.aw.unity.Data;
import com.aw.unity.Field;

/**
 * Created by scott on 09/10/15.
 * Updated by jlehmann
*/
public interface Filter extends Validatable {

	/**
	 * Extends validation concept to provide pathing in error messages
	 *
	 * @param path The path so far
	 */
	public void validate(String path);

	/**
	 * Does the filter match the incoming data?
	 *
	 * @param data The data to match against
	 * @return Whether the filter matches the data
	 */
    public boolean match(Data data);

    /**
     * @return The human readable filter
     */
    public String printCanonical();

    /**
     * @return Is the filter active?
     */
    public boolean isEnabled();

    /**
     * @return Child filters, if any
     */
    public List<Filter> getFilters();

    /**
     * @return get all filter constraints for the given field
     */
    public Collection<FilterConstraint> findAll(Field field);

    /**
     * @return If true, logic of this filter is NOT'd
     */
    public boolean isNot();

    /**
     * @return A full copy of the filter, so that modifications made to the structure of the returned filter do not modify the original - leaf nodes should not be assumed to be copied
     */
    public Filter deepCopy();

}
