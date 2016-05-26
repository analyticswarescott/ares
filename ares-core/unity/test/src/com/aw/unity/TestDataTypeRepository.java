package com.aw.unity;

public class TestDataTypeRepository implements DataTypeRepository {

	@Override
	public DataType resolveDataType(Object rawData) {
		return  m_dataTypes[0];
	}

	@Override
	public DataType getDataType(String name) {
		return m_dataTypes[0];
	}

	@Override
	public DataTypeResolver[] getResolvers() {
		return new DataTypeResolver[0];
	}

	public DataType[] getDataTypes() { return m_dataTypes; }
	public void setDataTypes(DataType[] dataTypes) { m_dataTypes = dataTypes; }
	private DataType[] m_dataTypes = new DataType[] { new TestDataType() };

}
