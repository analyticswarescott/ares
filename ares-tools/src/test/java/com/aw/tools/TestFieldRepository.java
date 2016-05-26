package com.aw.tools;

import com.aw.unity.Field;
import com.aw.unity.FieldRepository;

public class TestFieldRepository implements FieldRepository {

	/**
	 * Wraps another field repository for testability
	 *
	 * @param repo The field repository to wrap
	 */
	public TestFieldRepository(FieldRepository repo) {
		m_repo = repo;
	}

	@Override
	public Field[] getFields() {
		return m_repo.getFields();
	}

	@Override
	public Field getField(String name) {
		return m_repo.getField(name);
	}

	@Override
	public boolean hasField(String name) {
		return m_repo.hasField(name);
	}

	private FieldRepository m_repo;
}
