package com.aw.unity;

import com.aw.unity.defaults.DataUnityResults;
import com.aw.unity.defaults.DefaultUnityResults;
import com.aw.unity.defaults.PojoUnityResults;
import com.aw.unity.odata.ODataUnityResults;
import com.aw.unity.query.AbstractUnityRunner;
import com.aw.unity.query.Query;

public class TestRunner extends AbstractUnityRunner {

	@Override
	protected <T> PojoUnityResults<T> getPojoData(Query query, Class<T> type) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	protected void execute(Query query) {
	}

	@Override
	protected DefaultUnityResults getRowData(Query query) {
		return new DefaultUnityResults();
	}

	@Override
	protected ODataUnityResults getOData(Query query) {
		return new ODataUnityResults();
	}

	@Override
	protected DataUnityResults getData(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

}
