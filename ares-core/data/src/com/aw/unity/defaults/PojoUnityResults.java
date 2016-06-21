package com.aw.unity.defaults;

import java.util.Iterator;
import java.util.List;

import com.aw.common.util.VersionedObject;
import com.aw.unity.UnityResults;

/**
 * Results in the form of a list of versioned plain old java objects, based on the data source's ability to convert data to pojos
 *
 *
 *
 */
public class PojoUnityResults<T> implements UnityResults, Iterable<VersionedObject<T>> {

	public PojoUnityResults() {
	}

	public PojoUnityResults(List<VersionedObject<T>> pojos) {
		m_pojos = pojos;
	}

	@Override
	public Iterator<VersionedObject<T>> iterator() {
		return m_pojos.iterator();
	}

	public List<VersionedObject<T>> getPojos() { return m_pojos; }
	public void setPojos(List<VersionedObject<T>> pojos) { m_pojos = pojos; }
	private List<VersionedObject<T>> m_pojos;

}
