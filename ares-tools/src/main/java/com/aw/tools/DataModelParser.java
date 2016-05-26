package com.aw.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Created by eric on 10/21/15.
 *
 * parse agent rule file and reporting supplemental file to create
 *
 */
public class DataModelParser {


    public static void main(String[] args) throws Exception {


        if(args.length!=2){
            System.out.println("Usage: AgentRuleFullFilePath ReportingRuleFileFullPath");
            return;
        }

        String agentFilePath = args[0];
        String reportingFilePath = args[1];

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


        boolean dtdValidate = false;
        boolean xsdValidate = false;
        dbf.setNamespaceAware(true);
        dbf.setValidating(dtdValidate || xsdValidate);

        // ...

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document docAgentRules = db.parse(new File(agentFilePath));  // supplied by agent team
        Document docReportingRules = db.parse(new File(reportingFilePath)); // supplemental information



        Node metadata = docAgentRules.getDocumentElement();
        Node operationTypes = null;
        Map<String,Node> bundleClasses = new HashMap<String,Node>();

        NodeList list = metadata.getChildNodes();

        for (int i=0; i < list.getLength(); i++) {

            Node subnode = list.item(i);

            String nodeName = subnode.getLocalName();


            if(nodeName!=null) {


                if (nodeName.equalsIgnoreCase("constant")) {
                    operationTypes = subnode;
                } else if (nodeName.contains("bundleClass")) {

                    NamedNodeMap nnm = subnode.getAttributes();
                    Node element = nnm.getNamedItem("element");
                    bundleClasses.put(element.getNodeValue(), subnode);

                }
            }


        }



        //output #1 ES DDL
        JSONObject reportingMaster = new JSONObject();  //resulting master for BundleProcessing
        JSONArray reportingElements = new JSONArray();
        reportingMaster.put("bundle_elements", reportingElements);

        for(Node bundleClass : bundleClasses.values()){

            NamedNodeMap nnm = bundleClass.getAttributes();
            Node element = nnm.getNamedItem("element");
            String elementName = element.getNodeValue();

            Node reportingTransforms = getReportingTransformsForElementName(docReportingRules, elementName);
            Node reportingOverrides = getReportingOverridesForElementName(docReportingRules, elementName);
            String primary_key = getReportingElementPrimaryKey(docReportingRules, elementName);

            JSONObject reportingCollection = new JSONObject();
            reportingElements.put(reportingCollection);
            reportingCollection.put("name",elementName);
            reportingCollection.put("primary_key",primary_key);
            JSONArray reportingAttributes = new JSONArray();
            reportingCollection.put("attributes",reportingAttributes);//collection of the actual reporting



            //class
            NodeList classes = bundleClass.getChildNodes();

            for (int i=0; i < classes.getLength(); i++) {

                Node class_ = classes.item(i);  // xml has reserved words


                String agent_class_name = "none";
                nnm = class_.getAttributes();
                if(nnm!=null) {
                    Node attribute = nnm.getNamedItem("name");
                    if (attribute != null) {
                        agent_class_name = attribute.getNodeValue();
                    }
                }

                //in agent doc, next level down is called 'attrib'
                NodeList attribs = class_.getChildNodes();

                if(attribs.getLength()!=0) {


                    for (int y = 0; y < attribs.getLength(); y++) {

                        Node attrib = attribs.item(y);
                        nnm = attrib.getAttributes();

                        if (nnm != null) {

                            JSONObject reportingAttribute = new JSONObject();
                            reportingAttributes.put(reportingAttribute);
                            reportingAttribute.put("agent_class_name", agent_class_name);

                            ///<attrib name="tid" type="DGPROP_GUID" desc="template guid"/>
                            reportingAttribute.put("name", nnm.getNamedItem("name").getNodeValue());
                            reportingAttribute.put("agent_type", nnm.getNamedItem("type").getNodeValue());
                            reportingAttribute.put("reporting_type", getReportingTypeFromAgentAttribute(attrib));
                            reportingAttribute.put("desc", nnm.getNamedItem("desc").getNodeValue());

                            //deal with any attribute override or additional properties
                            putReportingOverrides(reportingAttribute,getReportingOverrideForAgentAttribute(reportingOverrides,(String)reportingAttribute.get("name")));

                            //get any transforms that have this attribute as source
                            List<Node> transforms = getTransformsForSource(reportingTransforms,(String)reportingAttribute.get("name"));

                            for(Node transform : transforms){

                                JSONObject transformAttribute = new JSONObject();
                                transformAttribute.put("agent_class_name", reportingAttribute.get("agent_class_name"));
                                reportingAttribute.put("name", getSafeAttributeValue(transform, "name"));
                                reportingAttribute.put("desc", reportingAttribute.get("desc")); //?
                                reportingAttribute.put("source_attrib", getSafeAttributeValue(transform, "source_attrib"));
                                reportingAttribute.put("transform_type", getSafeAttributeValue(transform,"transform_type"));
                                reportingAttribute.put("transform_arg1", getSafeAttributeValue(transform,"transform_arg1"));
                                reportingAttribute.put("reporting_type", getSafeAttributeValue(transform,"reporting_type"));

                            }

                        }


                    }



                }



            }


        }


        System.out.println(reportingMaster.toString());

        //master model built, save it?

        //now generate ES DDL
        JSONObject esDLL = new JSONObject();
        esDLL.put("settings", createESSettingsNode());
        JSONObject mappings = new JSONObject();
        esDLL.put("mappings", mappings);

        JSONArray master_elements = reportingMaster.getJSONArray("bundle_elements");

        Node reportingStructures = getChildNode(docReportingRules.getDocumentElement(), "reporting_structures");

        if(reportingStructures!=null){

            NodeList structures = reportingStructures.getChildNodes();

            for(int i=0; i<structures.getLength();i++) {


                Node reportingStructure = structures.item(i);



                if (reportingStructure.hasChildNodes()) {


                    String IndexName = getSafeAttributeValue(reportingStructure, "name");
                    String id = getSafeAttributeValue(reportingStructure, "primary_key");




                    JSONObject index = new JSONObject();
                    mappings.put(IndexName, index);
                    JSONObject _id = new JSONObject();
                    index.put("_id", _id);
                    _id.put("path",id);


                    JSONObject properties = new JSONObject();
                    index.put("properties", properties);


                    //now add the stuff
                    Node bundleElements = getChildNode(reportingStructure, "bundle_elements");

                    if (bundleElements != null && bundleElements.hasChildNodes()) {

                        processBundleElements(master_elements, properties, bundleElements); //adds each collection to properties

                    }

                    //now deal with child collections
                    Node childStructures = getChildNode(reportingStructure, "child_structures");

                    if (childStructures != null && childStructures.hasChildNodes()) {

                        NodeList children = childStructures.getChildNodes();

                        for (int z = 0; z < children.getLength(); z++) {

                            //<child_structure id="violated_policies" name="violated_policies">
                            Node child_structure = children.item(z);

                            // <child_structure id="violated_policies" name="violated_policies">
                            String child_name = getSafeAttributeValue(child_structure, "id");

                            if(child_name!=null && child_name.length()>0) {

                                JSONObject child = new JSONObject();
                                properties.put(child_name, child);

                                //create new property
                                JSONObject child_properties = new JSONObject();
                                child.put("properties", child_properties);

                                processBundleElements(master_elements, child_properties, child_structure); //adds each collection to properties
                            }

                        }
                    }


                }


            }


            System.out.println(esDLL.toString());


        }



        }

    private static Node getReportingBundleElement(Document docReportingRules, String elementName) throws Exception{

        /*
        <MetaData v="5.0.1.0004">
          <bundle_elements>
            <bundle_element id="ua" primary_key="meid">
            <transforms> */
        NodeList rootChildren = docReportingRules.getDocumentElement().getChildNodes();

        for (int i=0; i < rootChildren.getLength(); i++) {

            Node subnode = rootChildren.item(i);

            if (subnode.getNodeName() == "bundle_elements") {

                NodeList bundleElements = subnode.getChildNodes();

                for (int y = 0; y < bundleElements.getLength(); y++) {

                    Node bundleElement = bundleElements.item(y);

                    if ( getSafeAttributeValue(bundleElement,"id").equals(elementName)) {
                    //if (bundleElement.getAttributes().getNamedItem("id").getNodeValue().equals(elementName)) {

                        return bundleElement;
                    }
                }
            }
        }

        return null;



    }

    private static Node getReportingTransformsForElementName(Document docReportingRules, String elementName) throws Exception{

        /*
        <MetaData v="5.0.1.0004">
          <bundle_elements>
            <bundle_element id="ua" primary_key="meid">
            <transforms> */

        Node bundleElement = getReportingBundleElement(docReportingRules,elementName);

        if(bundleElement==null){return null;}

        NodeList bundleElementChildren = bundleElement.getChildNodes();

        for (int z = 0; z < bundleElementChildren.getLength(); z++) {

            Node child = bundleElementChildren.item(z);
            if (child.getNodeName().equals("transforms")) {
                return child;
            }

        }



        return null;



    }

    private static Node getReportingOverridesForElementName(Document docReportingRules, String elementName) throws Exception{
/*
        <MetaData v="5.0.1.0004">
          <bundle_elements>
            <bundle_element id="ua" primary_key="meid">
            <transforms>
            <overrides>
            */

        Node bundleElement = getReportingBundleElement(docReportingRules,elementName);

        if(bundleElement==null){return null;}

        NodeList bundleElementChildren = bundleElement.getChildNodes();

        for (int z = 0; z < bundleElementChildren.getLength(); z++) {

            Node child = bundleElementChildren.item(z);
            if (child.getNodeName().equals("overrides")) {
                return child;
            }

        }



        return null;
    }
    private static String getReportingElementPrimaryKey(Document docReportingRules, String elementName) throws Exception{

        Node bundleElement = getReportingBundleElement(docReportingRules, elementName);
        if(bundleElement==null){return "";}
        return bundleElement.getAttributes().getNamedItem("primary_key").getNodeValue();

    }

    private static String getReportingTypeFromAgentAttribute(Node agentAttributeNode) throws Exception{

        String agent_type = getSafeAttributeValue(agentAttributeNode,"type");
        String agent_desc = getSafeAttributeValue(agentAttributeNode, "desc");
        if(agent_type.equalsIgnoreCase("DGPROP_INT8") && agent_desc.contains("?")){
            return "boolean";
        }
        else if(agent_type.contains("_GUID")){
            return "identifier";
        }
        else if(agent_type.contains("_INT")){
            return "code";
        }
        return "description";
    }

    private static Node getReportingOverrideForAgentAttribute(Node reportingOverrides, String elementName) throws Exception {

        if(reportingOverrides==null){return null;}

        /* <attrib name="mr"  reporting_realm="email_address"></attrib> */
        NodeList overrides = reportingOverrides.getChildNodes();

        for (int i = 0; i < overrides.getLength(); i++) {
            Node child = overrides.item(i);
            if(getSafeAttributeValue(child, "name").equalsIgnoreCase(elementName)){
            //if(child.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(elementName)){
                return child;
            }
        }

        return null;
    }
    private static void  putReportingOverrides(JSONObject reportingAttribute,Node reportingOverrides) throws Exception {

        if(reportingAttribute == null || reportingOverrides == null){
            return;
        }

        NamedNodeMap nnm = reportingOverrides.getAttributes();
        for(int i=0; i< nnm.getLength(); i++){

            Node a = nnm.item(i);
            reportingAttribute.put(a.getNodeName(),a.getNodeValue());

        }



    }

    private static List<Node>  getTransformsForSource(Node reportingTransforms,String attributeName) throws Exception{

        ArrayList<Node> ar = new ArrayList<Node>();
        if(reportingTransforms==null){return ar;}

         /* <<attrib name="ot_desc"  source_attrib="ot" transform_type="desc_lookup"></attrib> */
        NodeList overrides = reportingTransforms.getChildNodes();


        for (int i = 0; i < overrides.getLength(); i++) {
            Node child = overrides.item(i);
            if(getSafeAttributeValue(child,"source_attrib").equalsIgnoreCase(attributeName)){
            //if(child.getAttributes().getNamedItem("source_attrib").getNodeValue().equalsIgnoreCase(attributeName)){
                ar.add(child);
            }
        }

        return ar;

    }

    private static String getSafeAttributeValue(Node node, String attributeName) throws Exception {

        NamedNodeMap nnm = node.getAttributes();
        if(nnm==null){return "";
        }
        Node n = nnm.getNamedItem(attributeName);
        if (n == null){return "";}
        return n.getNodeValue();

    }

    private static void processBundleElements( JSONArray reporting_master, JSONObject properties, Node bundleElements ) throws Exception {

        if(!bundleElements.hasChildNodes()){return;}

        NodeList elements = bundleElements.getChildNodes();

        for (int y = 0; y < elements.getLength(); y++) {

            //  <bundle_element id="ua"/>
            Node bundleElement = elements.item(y);

            String element_id = getSafeAttributeValue(bundleElement, "id");

            if(element_id!=null && element_id.length()>0){

                for (int i = 0; i < reporting_master.length(); i++) {
                    JSONObject item = (JSONObject) reporting_master.get(i);
                    if (item.getString("name").equalsIgnoreCase(element_id)) {

                        //got them, add attibutes providing they dont exist
                        JSONArray attributes = item.getJSONArray("attributes");
                        for (int z = 0; z < attributes.length(); z++) {

                            JSONObject attribute = (JSONObject) attributes.get(z);

                            if (!properties.has((String) attribute.get("name"))) {
                                //create and add
                                JSONObject property = new JSONObject();

                                properties.put((String) attribute.get("name"), property);

                                //figure out data type
                                String reporting_type = attribute.getString("reporting_type");
                                String agent_type = attribute.getString("agent_type");

                                if (reporting_type.equalsIgnoreCase("boolean")) {
                                    property.put("type", "boolean");
                                }
                                else if (reporting_type.equalsIgnoreCase("date")) {
                                    property.put("type", "date");
                                    property.put("format", "dateOptionalTime");
                                }
                                else if (reporting_type.equalsIgnoreCase("time")) {
                                    property.put("type", "string");
                                    property.put("index", "not_analyzed");
                                }else if (agent_type.contains("_INT")) {
                                    property.put("type", "long");
                                } else {
                                    property.put("type", "string");
                                    property.put("index", "not_analyzed");
                                }
                            }

                        }

                    }
                }
            }


            //get this from master

            //add each item (if doesnt exist)


        }
    }

    private static  Node getChildNode(Node parentNode,String nodeName) throws Exception{
        if( !parentNode.hasChildNodes() ){ return null; }
        NodeList nl = parentNode.getChildNodes();
        for(int i=0; i<nl.getLength();i++){
            Node child = nl.item(i);
            if(child.getNodeName().equalsIgnoreCase(nodeName)){
                return child;
            }
        }
        return null;
    }

    //placeholder defaults
    private static JSONObject createESSettingsNode() throws Exception {

        JSONObject settings = new JSONObject();
        settings.put("number_of_shards",1);
        settings.put("number_of_replicas",1);
        JSONObject index = new JSONObject();
        settings.put("index",index);
        JSONObject analysis = new JSONObject();
        index.put("analysis",analysis);
        JSONObject analyzer = new JSONObject();
        analysis.put("analyzer",analyzer);
        JSONObject keylower = new JSONObject();
        analyzer.put("keylower",keylower);
        keylower.put("tokenizer","keyword");
        keylower.put("filter","lowercase");
        return settings;

    }
}
