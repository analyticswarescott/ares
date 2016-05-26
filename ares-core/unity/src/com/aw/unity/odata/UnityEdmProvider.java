/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.aw.unity.odata;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;

public class UnityEdmProvider extends EdmProvider {

//    static public final Logger logger = Logger.getLogger(UnityEdmProvider.class);
//
//    static final String ENTITY_SET_NAME_MANUFACTURERS = "Manufacturers";
//    static final String ENTITY_SET_NAME_CARS = "Cars";
//    static final String ENTITY_NAME_MANUFACTURER = "Manufacturer";
//    static final String ENTITY_NAME_CAR = "Car";
//
//
//    private static final String NAMESPACE = "com.aw.unity";
//
//    private static final FullQualifiedName ENTITY_TYPE_1_1 = new FullQualifiedName(NAMESPACE, ENTITY_NAME_CAR);
//    private static final FullQualifiedName ENTITY_TYPE_1_2 = new FullQualifiedName(NAMESPACE, ENTITY_NAME_MANUFACTURER);
//
//    private static final FullQualifiedName COMPLEX_TYPE = new FullQualifiedName(NAMESPACE, "Address");
//
//    private static final FullQualifiedName ASSOCIATION_CAR_MANUFACTURER = new FullQualifiedName(NAMESPACE,
//            "Car_Manufacturer_Manufacturer_Cars");
//
//    private static final String ROLE_1_1 = "Car_Manufacturer";
//    private static final String ROLE_1_2 = "Manufacturer_Cars";
//
//    private static final String ENTITY_CONTAINER = "OData";
//
//    private static final String ASSOCIATION_SET = "Cars_Manufacturers";
//
//    private static final String FUNCTION_IMPORT = "NumberOfCars";
//
//    public static HashMap<String, Query> _queries = new HashMap<String, Query>();
//    public static HashMap<String, EntitySet> entitySets = new HashMap<String, EntitySet>();
//    public static HashMap<String, EntityType> entityTypes = new HashMap<String, EntityType>();
//
//    private EdmSimpleTypeKind getType(FieldType type) throws Exception {
//
//    	switch (type) {
//    		case DOUBLE: return EdmSimpleTypeKind.Double;
//    		case FLOAT: return EdmSimpleTypeKind.Decimal;
//    		case INT: return EdmSimpleTypeKind.Int32;
//    		case LONG: return EdmSimpleTypeKind.Int64;
//    		case GEO_LOCATION: return EdmSimpleTypeKind.String;
//    		case IP_ADDRESS: return EdmSimpleTypeKind.String;
//    		case MAC_ADDRESS: return EdmSimpleTypeKind.String;
//    		case STRING: return EdmSimpleTypeKind.String;
//    	}
//
//        throw new Exception(" Odata producer found unsupported Unity data type" + type);
//
//    }
//
//    private EntityType queryToType(Query q) throws Exception {
//
//        // Key
//        List<PropertyRef> keyProperties = new ArrayList<PropertyRef>();
//
//        List<Property> properties = new ArrayList<Property>();
//
//        int keyCount = 0;
//
//        //get attributes:  if no columns, see if we can use metadata for the "select *" use case
//
//        TreeMap<Integer, QueryAttribute> atts = null;
//        for (QueryAttribute att : q.getAttributes()) {
//
//            String attName = att.getField().getName();
//            if (false) { //att.isODataKey()) {
//
//                logger.debug("adding key to metadata: " + attName);
//                keyProperties.add(new PropertyRef().setName(attName));
//
//                keyCount++;
//            }
//            //properties.add(EdmProperty.newBuilder(att.getNameDisplay()).setType(getType(att.getColDataType())));
//            properties.add(new SimpleProperty().setName(attName).setType(getType(att.getColDataType())));
//        }
//
//        if (keyCount == 0) {
//            properties.add(new SimpleProperty().setName("GenKey").setType(EdmSimpleTypeKind.String));
//            keyProperties.add(new PropertyRef().setName("GenKey"));
//        }
//        Key key = new Key().setKeys(keyProperties);
//
//        EntityType et = new EntityType();
//        et = et.setName(q.getName()).setProperties(properties).setKey(key);
//        ;
//
//        return et;
//        // .setNavigationProperties(navigationProperties);
//    }
//
//
//    List<Document> getQueries() throws Exception {
//
//    	DocumentHandler docs = AdminMgr.getDocHandler();
//
//    	return docs.getDocumentsOfType(DocumentType.ODATA_ENTITY);
//
//    }
//
//    @Override
//    public List<Schema> getSchemas() throws ODataException {
//        List<Schema> schemas = new ArrayList<Schema>();
//
//        Schema schema = new Schema();
//        schema.setNamespace(NAMESPACE);
//        Query q;
//        EntityType type = null;
//
//        try {
//           // ArrayList<JSONObject> files = AWFileUtils.getDirToJSONObjList(path);
//            List<Document> files = getQueries();
//
//            for (Document qry : files) {
//
//            	JSONObject queryJ = qry.getBody();
//
//
//                //TODO: null meta is only way to store "design time" query in memory at the moment
//                   //meta will need to be set for runtime use
//                q = QueryFactory.newQuery(queryJ, QueryFormat.JSON, null); //TODO: get unity metadata from somewhere (passed in?)
//                _queries.put(q.getName(), q);
//                EntitySet es = new EntitySet().setName(q.getName()).setEntityType(new FullQualifiedName(NAMESPACE, q.getName()));
//                entitySets.put(q.getName(), es);
//                type = queryToType(q);
//                entityTypes.put(q.getName(), type);
//            }
//        } catch (Exception e) {
//        	e.printStackTrace();
//            throw new ODataException(e);
//        }
//
//
//        schema.setEntityTypes(new ArrayList(entityTypes.values()));
//
///*    List<ComplexType> complexTypes = new ArrayList<ComplexType>();
//    complexTypes.add(getComplexType(COMPLEX_TYPE));
//    schema.setComplexTypes(complexTypes);*/
//
///*    List<Association> associations = new ArrayList<Association>();
//    associations.add(getAssociation(ASSOCIATION_CAR_MANUFACTURER));
//    schema.setAssociations(associations);*/
//
//        List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
//        EntityContainer entityContainer = new EntityContainer();
//        entityContainer.setName(ENTITY_CONTAINER).setDefaultEntityContainer(true);
//
//        entityContainer.setEntitySets(new ArrayList(entitySets.values()));
//
///*    List<AssociationSet> associationSets = new ArrayList<AssociationSet>();
//    associationSets.add(getAssociationSet(ENTITY_CONTAINER, ASSOCIATION_CAR_MANUFACTURER,
//        ENTITY_SET_NAME_MANUFACTURERS, ROLE_1_2));
//    entityContainer.setAssociationSets(associationSets);*/
//
///*   List<FunctionImport> functionImports = new ArrayList<FunctionImport>();
//    functionImports.add(getFunctionImport(ENTITY_CONTAINER, FUNCTION_IMPORT));
//    entityContainer.setFunctionImports(functionImports);*/
//
//        entityContainers.add(entityContainer);
//        schema.setEntityContainers(entityContainers);
//
//        schemas.add(schema);
//
//        return schemas;
//    }
//
//    @Override
//    public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataException {
//
//        return entityTypes.get(edmFQName.getName());
//
//    }
//
//    @Override
//    public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataException {
//        if (NAMESPACE.equals(edmFQName.getNamespace())) {
//            if (COMPLEX_TYPE.getName().equals(edmFQName.getName())) {
//                List<Property> properties = new ArrayList<Property>();
//                properties.add(new SimpleProperty().setName("Street").setType(EdmSimpleTypeKind.String));
//                properties.add(new SimpleProperty().setName("City").setType(EdmSimpleTypeKind.String));
//                properties.add(new SimpleProperty().setName("ZipCode").setType(EdmSimpleTypeKind.String));
//                properties.add(new SimpleProperty().setName("Country").setType(EdmSimpleTypeKind.String));
//                return new ComplexType().setName(COMPLEX_TYPE.getName()).setProperties(properties);
//            }
//        }
//
//        return null;
//    }
//
//    @Override
//    public Association getAssociation(final FullQualifiedName edmFQName) throws ODataException {
//        if (NAMESPACE.equals(edmFQName.getNamespace())) {
//            if (ASSOCIATION_CAR_MANUFACTURER.getName().equals(edmFQName.getName())) {
//                return new Association().setName(ASSOCIATION_CAR_MANUFACTURER.getName())
//                        .setEnd1(
//                                new AssociationEnd().setType(ENTITY_TYPE_1_1).setRole(ROLE_1_1).setMultiplicity(EdmMultiplicity.MANY))
//                        .setEnd2(
//                                new AssociationEnd().setType(ENTITY_TYPE_1_2).setRole(ROLE_1_2).setMultiplicity(EdmMultiplicity.ONE));
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataException {
//
//
//        for (EntitySet ees : entitySets.values()) {
//            logger.debug(ees.getName());
//            logger.debug(ees.getEntityType().toString());
//        }
//
//        EntitySet es = entitySets.get(name);
//        if (es == null) {
//            return null;
//        }
//        return es;
//
///*    if (ENTITY_CONTAINER.equals(entityContainer)) {
//      if (ENTITY_SET_NAME_CARS.equals(name)) {
//        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_1);
//      } else if (ENTITY_SET_NAME_MANUFACTURERS.equals(name)) {
//        return new EntitySet().setName(name).setEntityType(ENTITY_TYPE_1_2);
//      }
//    }
//    return null;*/
//    }
//
//    @Override
//    public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association,
//                                            final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataException {
//        if (ENTITY_CONTAINER.equals(entityContainer)) {
//            if (ASSOCIATION_CAR_MANUFACTURER.equals(association)) {
//                return new AssociationSet().setName(ASSOCIATION_SET)
//                        .setAssociation(ASSOCIATION_CAR_MANUFACTURER)
//                        .setEnd1(new AssociationSetEnd().setRole(ROLE_1_2).setEntitySet(ENTITY_SET_NAME_MANUFACTURERS))
//                        .setEnd2(new AssociationSetEnd().setRole(ROLE_1_1).setEntitySet(ENTITY_SET_NAME_CARS));
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataException {
//        return null;
//    }
//
//    @Override
//    public EntityContainerInfo getEntityContainerInfo(final String name) throws ODataException {
//
//        //if (name == null || "OData".equals(name)) {
//        return new EntityContainerInfo().setName("OData").setDefaultEntityContainer(true);
//
//        // }
//        // return null;
//    }
}
