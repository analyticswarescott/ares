package com.aw.document;

/**
 * Created by scott on 04/11/15.
 */
public class TestDocuments {

    public static final String TEST_DOC_DEF_TYPED =
            "{\n" +
                    "  \"display_name\": \"Default Unity Instance \",\n" +
                    "  \"description\": \"Default Unity Instance  \",\n" +
                    "  \"author\": \"aw\",\n" +
                    "  \"body_class\" : \"com.aw.document.body.TestBodyClass\",\n" +
                    "  \"body\": {\n" +
                    "    \"tenant_id\": \"123\",\n" +
                    "    \"default_data_source\" : \"es\",\n" +
                    "\t\"data_sources\" : [\n" +
                    "    \t{\n" +
                    "\t\t    \"source_name\": \"es\",\n" +
                    "\t\t    \"source_class\": \"com.aw.unity.es.UnityDataSourceElastic\",\n" +
                    "\t\t    \"runner_class\": \"com.aw.unity.query.URunElastic\",\n" +
                    "\t\t    \"unity.row.sec.class\": \"com.aw.unity.security.SecRowDG\",\n" +
                    "\t\t    \"unity.col.sec.class\": \"com.aw.unity.security.SecColumnDG\",\n" +
                    "\t\t    \"unity.role.resolver\": \"com.aw.unity.security.DataSecurityResolverDG\",\n" +
                    "\t\t    \"connection\": {\n" +
                    "\t\t      \"conn_type\": \"ES\",\n" +
                    "\t\t      \"conn_name\": \"ES\",\n" +
                    "\t\t      \"conn_def\": {\n" +
                    "\t\t        \"port\": 9200,\n" +
                    "\t\t        \"host\": \"{$UNITY_ES_HOST}\"\n" +
                    "\t\t      }\n" +
                    "\t\t    },\n" +
                    "\t\t    \"index_mapping\" : [\n" +
                    "\t\t    \t{ \n" +
                    "\t\t    \t\t\"data_type\" : \"data_type_name\",\n" +
                    "\t\t    \t\t\"index_name\" : \"index_name\"\n" +
                    "\t\t    \t}\n" +
                    "\t\t    ]\n" +
                    "\t\t}\n" +
                    "    ]\n" +
                    "    \n" +
                    "  }\n" +
                    "}";

    public static final String TEST_DOC_DEF_INITIALIZABLE =
            "{\n" +
                    "  \"display_name\": \"Default Unity Instance \",\n" +
                    "  \"description\": \"Default Unity Instance  \",\n" +
                    "  \"author\": \"aw\",\n" +
                    "  \"body_class\" : \"com.aw.document.body.TestBodyClassInitializable\",\n" +
                    "  \"body\": {\n" +
                    "    \"tenant_id\": \"123\",\n" +
                    "    \"default_data_source\" : \"es\",\n" +
                    "\t\"data_sources\" : [\n" +
                    "    \t{\n" +
                    "\t\t    \"source_name\": \"es\",\n" +
                    "\t\t    \"source_class\": \"com.aw.unity.es.UnityDataSourceElastic\",\n" +
                    "\t\t    \"runner_class\": \"com.aw.unity.query.URunElastic\",\n" +
                    "\t\t    \"unity.row.sec.class\": \"com.aw.unity.security.SecRowDG\",\n" +
                    "\t\t    \"unity.col.sec.class\": \"com.aw.unity.security.SecColumnDG\",\n" +
                    "\t\t    \"unity.role.resolver\": \"com.aw.unity.security.DataSecurityResolverDG\",\n" +
                    "\t\t    \"connection\": {\n" +
                    "\t\t      \"conn_type\": \"ES\",\n" +
                    "\t\t      \"conn_name\": \"ES\",\n" +
                    "\t\t      \"conn_def\": {\n" +
                    "\t\t        \"port\": 9200,\n" +
                    "\t\t        \"host\": \"{$UNITY_ES_HOST}\"\n" +
                    "\t\t      }\n" +
                    "\t\t    },\n" +
                    "\t\t    \"index_mapping\" : [\n" +
                    "\t\t    \t{ \n" +
                    "\t\t    \t\t\"data_type\" : \"data_type_name\",\n" +
                    "\t\t    \t\t\"index_name\" : \"index_name\"\n" +
                    "\t\t    \t}\n" +
                    "\t\t    ]\n" +
                    "\t\t}\n" +
                    "    ]\n" +
                    "    \n" +
                    "  }\n" +
                    "}";


    public static final String TEST_DOC_DEF_GENERIC =
            "{\n" +
                    "  \"display_name\": \"Default Unity Instance \",\n" +
                    "  \"description\": \"Default Unity Instance  \",\n" +
                    "  \"author\": \"aw\",\n" +
                    "  \"body_class\" : \"\",\n" +
                    "  \"body\": {\n" +
                    "    \"tenant_id\": \"123\",\n" +
                    "    \"default_data_source\" : \"es\",\n" +
                    "    \"data_sources\" : [\n" +
                    "      {\n" +
                    "        \"source_name\": \"es\",\n" +
                    "        \"source_class\": \"com.aw.unity.es.UnityDataSourceElastic\",\n" +
                    "        \"unity.row.sec.class\": \"com.aw.unity.security.SecRowDG\",\n" +
                    "        \"unity.col.sec.class\": \"com.aw.unity.security.SecColumnDG\",\n" +
                    "        \"unity.role.resolver\": \"com.aw.unity.security.DataSecurityResolverDG\",\n" +
                    "        \"connection\": {\n" +
                    "          \"conn_type\": \"ES\",\n" +
                    "          \"conn_name\": \"ES\",\n" +
                    "          \"conn_def\": {\n" +
                    "            \"port\": 9200,\n" +
                    "            \"host\": \"{$UNITY_ES_HOST}\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"index_mapping\" : [\n" +
                    "          {\n" +
                    "            \"data_type\" : \"data_type_name\",\n" +
                    "            \"index_name\" : \"index_name\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "\n" +
                    "  }\n" +
                    "}";
}
