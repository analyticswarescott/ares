package com.aw.unity;

import com.aw.unity.json.JSONFilterFactory;

/**
 * Unity metadata object for testing
 *
 *
 */
public class TestUnityMeta implements UnityMetadata {

	public TestUnityMeta() {
		m_fieldRepository = new TestFieldRepository();
		m_filterFactory = new JSONFilterFactory(this);
		m_dataTypeRepository = new TestDataTypeRepository();
	}

	@Override
	public DataType getDataType(String dataType) {
		// TODO Auto-generated method stub
		return m_dataTypeRepository.getDataTypes()[0];
	}


	public FieldRepository getFieldRepository() { return m_fieldRepository; }
	public void setFieldRepository(FieldRepository fieldRepository) { m_fieldRepository = fieldRepository; }
	private FieldRepository m_fieldRepository;

	public FilterFactory getFilterFactory() { return m_filterFactory; }
	public void setFilterFactory(FilterFactory filterFactory) { m_filterFactory = filterFactory; }
	private FilterFactory m_filterFactory;

	public DataTypeRepository getDataTypeRepository() { return m_dataTypeRepository; }
	public void setDataTypeRepository(DataTypeRepository dataTypeRepository) { m_dataTypeRepository = dataTypeRepository; }
	private DataTypeRepository m_dataTypeRepository;

}
