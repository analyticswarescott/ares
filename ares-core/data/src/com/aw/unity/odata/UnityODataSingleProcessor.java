package com.aw.unity.odata;

import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

public class UnityODataSingleProcessor extends ODataSingleProcessor {
//    public static final Logger logger = Logger.getLogger(ODataSingleProcessor.class);
//
//    public UnityODataSingleProcessor() {
//    }
//
//    @Override
//    public ODataResponse readEntitySet(final GetEntitySetUriInfo uriInfo, final String contentType)
//            throws ODataException {
//
//        EdmEntitySet entitySet;
//        List<Map<String, Object>> entities = null;
//        if (uriInfo.getNavigationSegments().size() == 0) {
//            entitySet = uriInfo.getStartEntitySet();
//
//            Query query = UnityEdmProvider._queries.get(entitySet.getName());
//
//            AbstractUnityRunner u;
//            try {
//            	UnityInstance unity = getUnity();
//            	UnityRunner runner = unity.execute(query);
//            	entities = ((ODataUnityResults)runner.getResults(ResponseFormat.ODATA)).getResults();
//            } catch (Exception e1) {
//                throw new ODataException(" Unity error ", e1);
//            }
//
//            return EntityProvider.writeFeed(contentType, entitySet, entities, EntityProviderWriteProperties.serviceRoot(
//                    getContext().getPathInfo().getServiceRoot()).build());
//        }
//
//        return null;
//    }
//
//    @Override
//    public ODataResponse readEntity(final GetEntityUriInfo uriInfo, final String contentType) throws ODataException {
//
//        if (uriInfo.getNavigationSegments().size() == 0) {
//            EdmEntitySet entitySet = uriInfo.getStartEntitySet();
//
//            Query originQuery = UnityEdmProvider._queries.get(uriInfo.getStartEntitySet().getName());
//
//            try {
//
//                JSONObject entityQueryJSON = new JSONObject(originQuery.toString());
//
//                entityQueryJSON.remove("filter_group");
//                JSONArray newFilterGroup = new JSONArray();
//                entityQueryJSON.put("filter_group", newFilterGroup);
//
//                JSONObject newFilter = new JSONObject();
//                newFilterGroup.put(newFilter);
//                newFilter.put("operator", GroupOperator.AND.name());
//                JSONArray filterArray = new JSONArray();
//                newFilter.put("filter", filterArray);
//
//                for (KeyPredicate predicate : uriInfo.getKeyPredicates()) {
//                    String value = predicate.getLiteral();
//                    String propertyName = predicate.getProperty().getName();
//                    JSONObject filter = new JSONObject();
//                    filter.put("field", propertyName);
//                    filter.put("operator", ConstraintOperator.EQ.name());
//                    JSONArray values = new JSONArray();
//                    values.put(value);
//                    filter.put("values", values);
//                    filterArray.put(filter);
//                }
//
//            	UnityInstance instance = UnityMgr.getInstance();
//
//            	Query entityQuery = QueryFactory.newQuery(entityQueryJSON, QueryFormat.JSON, instance.getMetadata());
//
//            	List odata = (List)instance.execute(entityQuery);
//                Object o = odata.get(0);
//
//                Map<String, Object> data = (Map<String, Object>) o;
//                if (data != null) {
//                    URI serviceRoot = getContext().getPathInfo().getServiceRoot();
//                    ODataEntityProviderPropertiesBuilder propertiesBuilder =
//                            EntityProviderWriteProperties.serviceRoot(serviceRoot);
//
//                    return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
//                }
//
//            } catch (Exception e) {
//                throw new ODataException("Could not query for " + uriInfo.toString(), e);
//            }
//        }
//
//        throw new ODataNotImplementedException();
//    }

}
