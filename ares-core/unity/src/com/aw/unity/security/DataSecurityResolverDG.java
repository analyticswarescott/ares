package com.aw.unity.security;

import java.util.HashMap;
import java.util.List;

import com.aw.unity.query.Filter;
import com.aw.unity.query.FilterGroup;

/**
 * TODO: finish system-tests with DGMC role stuff here
 *
 *
 */
public class DataSecurityResolverDG extends DataSecurityResolverBase {

    //federation types needed to resolve filters
    private static final String USER = "user";
    private static final String MACHINE = "machine";

    private HashMap<String, Object> _cache = new HashMap<String, Object>();


    public DataSecurityResolverDG() {

        try {
           // Document doc = docs.ge
            //TODO: get from jackson Tenant object once created
          //  String uri = t.get("mwa").toString();
           // _rest = new RestCommUtils(uri);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


//    private JSONObject resolveUser() throws Exception {
//
//        String tid = UnityMgr.getTenantID();
//        String uid = UnityMgr.getUserID();
//
//        String ret = _rest.getString("/WkrFedUserGroup/" + tid + "/" + uid);
//
//        JSONObject u = new JSONObject(ret);
//
//        return u;
//
//    }


	@Override
	public Filter getFilterForRoles(List<String> keys) {

//		try {
//			JSONArray filterArray = new JSONArray();
//			JSONObject filterGroup = new JSONObject();
//			for (String role : keys) {
//
//				RowConstraint rc = new RowConstraint();
//
//			    JSONObject filter = new JSONObject();
//
//				JSONArray vals = new JSONArray();
//				for (String val: rc.getValues()){
//			 		vals.put(val);
//				}
//
//				filter.put(FilterConstraint.ATTRIB_FILTER_OPERATOR, rc.getOperator());
//				filter.put(FilterConstraint.ATTRIB_ENABLED, true);
//				filter.put(FilterConstraint.ATTRIB_COL_ID, rc.getColumn());
//				filter.put(FilterConstraint.ATTRIB_COL_NAME, rc.getColumn());
//				filter.put(FilterConstraint.ATTRIB_FILTER_VALUES, vals );
//				filterArray.put(filter);
//
//			}
//
//			filterGroup.put(FilterGroup.ATTRIB_GROUP_FILTERS, filterArray);
//			filterGroup.put(FilterGroup.ATTRIB_GROUP_OPERATOR, FilterGroup.GroupOperator.AND);
//			UnityMetadata meta = getUnity().getMetadata();
//
//			Filter filter = getUnity().newFilter(filterGroup.toString());
//			return filter;
//		} catch (Exception e) {
//			throw new InvalidDataException("Cannot build group filter for roles " + keys, e);
//		}

		return new FilterGroup();

	}

	private static final String[] USERS = {"tonyt", "fi"};
	private static final String[] MACHINES = {"Mach3571314", "Mach96056032", "Mach37826637"};

	private static final String MACHINE_NAME = "Machine_Name";
	private static final String USER_NAME = "User_Name";

//	public class RowConstraint{
//
//		private String operator = null;
//		private String column = null;
//		private String[] values= null;
//
//
//        RowConstraint(){
//            //TODO: re-work of Sue's stuff in process
//        }
//
//		RowConstraint(String operator, String column, String[] values) {
//			this.operator = operator;
//			this.column = column;
//			this.values = values;
//		}
//	    public String getOperator(){
//	    	return this.operator;
//	    }
//	    public String getColumn(){
//	    	return this.column;
//	    }
//	    public String[] getValues(){
//	    	return this.values;
//	    }
//
//	}
//
//    public static String[] fake() {
//        return new String[1];
//    }

}
