package com.aw.unity;

import com.aw.unity.query.Query;

public class TestDataSource extends UnityDataSourceBase {

	@Override
	public UnityRunner execute(Query query) {
		return new TestRunner();
	}

}
