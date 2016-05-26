package com.aw.tools;

import com.aw.common.system.EnvironmentSettings;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SetupPlatformNode {

    private String confDirectory;
    private String serviceSharedSecret;
    private String hostName;
    private String firstNodeHost;
    private int firstNodePort = -1;
    private JSONObject defaultPlatform;
    private boolean isFirstNode;
    private JSONObject hostPlatform;

    public SetupPlatformNode(String confDirectory, String hostName, String firstNodeHost, String serviceSharedSecret)
            throws Exception {
        this.hostName = hostName;
        this.confDirectory = confDirectory;
        this.serviceSharedSecret = serviceSharedSecret;
        this.firstNodeHost = firstNodeHost;

        File defaultPlatformFile = new File(confDirectory
                + File.separatorChar + "templates"
                + File.separator + "platform.local.json");

        if (!defaultPlatformFile.exists()) {
            throw new RuntimeException("Could not find default local.json at "
                    + defaultPlatformFile.getAbsolutePath());
        }

        String defaultPlatformJsonStr = FileUtils.readFileToString(defaultPlatformFile);
        defaultPlatform = new JSONObject(defaultPlatformJsonStr);
        hostPlatform = new JSONObject(defaultPlatform.toString());
    }

    private String makeFirstNodeURL() {
        if ( firstNodeHost == null ) {
            throw new RuntimeException("First node host is undefined.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("http");
        sb.append("://");
        sb.append(firstNodeHost);
        if ( firstNodePort > 0 ) {
            sb.append(":");
            sb.append(firstNodePort);
        }
        return sb.toString();
    }

    private void exit(String message, int code) throws Exception {
        if (message != null) {
            log(message);
        }
        System.exit(code);
    }

    private void log(String message) {
        System.out.println(message);
    }

    private JSONObject fetchPlatformSpecDocument() throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String uri = makeFirstNodeURL() + com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tenant/0/nodes";
        try {
            HttpGet get = new HttpGet(uri);
            get.addHeader("serviceSharedSecret", this.serviceSharedSecret);
            CloseableHttpResponse response = httpClient.execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            if (!(responseCode >= 200 && responseCode < 300)) {
                exit("Failed to GET " + uri + ": response code " + responseCode, 1);
            }

            String jsonStr = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            try {
                return new JSONObject(jsonStr);
            } catch (Exception e) {
                exit("Failed to GET " + uri + ", failed to parse platform document: " + e.getMessage(), 1);
            }
            log("Successful GET to "+ uri + ": response code " + responseCode + ", received: \n" + this.hostPlatform.toString(4) );
        } catch (Exception e) {
            exit("Failed to GET " + uri + ": " + e.getMessage(), 1);
        }
        return null;
    }

    private void updatePlatformSpec() throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String uri = makeFirstNodeURL()  + com.aw.util.Statics.VERSIONED_REST_PREFIX + "/tenant/0/nodes/" + this.hostName;

        try {
            HttpPut put = new HttpPut(uri);
            put.addHeader("serviceSharedSecret", this.serviceSharedSecret);
            JSONObject hostNode = hostPlatform
                    .getJSONObject("body")
                    .getJSONObject("nodes")
                    .getJSONObject(hostName);

            put.setEntity(new StringEntity(hostNode.toString()));
            CloseableHttpResponse response = httpClient.execute(put);
            int responseCode = response.getStatusLine().getStatusCode();
            if (!(responseCode >= 200 && responseCode < 300)) {
                exit("Failed to PUT " + uri + ": response code " + responseCode, 1);
            }
            log("Successful PUT to "+ uri + ": response code " + responseCode );
        } catch( Exception e) {
            exit("Failed to PUT " + uri + ": " + e.getMessage(), 1);
        }
    }

    private boolean setup() throws Exception {
        String isFirstNode = question("Is this the first node?", "y/n", "y");
        this.isFirstNode = "y".equals(isFirstNode.toLowerCase());
        if (this.isFirstNode) {
            setupPlatform();
        } else {
            String firstNodeRest = null;
            while (firstNodeRest == null) {
                firstNodeRest = question("Hostname and port of the first node REST service (eg. localhost:8080):", null, this.hostName + ":8080");
                if (firstNodeRest == null) {
                    log("Please specify the first node REST service host:port.");
                }
            }
            this.firstNodeHost = firstNodeRest.contains(":") ? firstNodeRest.split(":")[0] : firstNodeRest;
            this.firstNodePort = Integer.valueOf( firstNodeRest.contains(":") ? firstNodeRest.split(":")[1] : "-1" );
            this.hostPlatform = fetchPlatformSpecDocument();
        }

        // setup node roles
        boolean accepted = false;
        while ( !accepted ) {
            updateNodes();
            accepted = review();
        }

        return accepted;
    }

    private void updateNodes() throws Exception {
        List<String> allRoles = listPlatformNodeRoles(defaultPlatform, "localhost");

        JSONObject platformNodes = hostPlatform.getJSONObject("body").getJSONObject("nodes");
        if ( !platformNodes.has(hostName) ) {
            // add the host
            platformNodes.put(hostName, new JSONObject() );
        }

        String roleToAdd = null;
        while (!"done".equals(roleToAdd)) {
            List<String> hostRoles = listPlatformNodeRoles(hostPlatform, hostName);
            String currentRoles = hostRoles.stream().collect(Collectors.joining(", "));
            log("This host '" + hostName + "' has the following roles: " + (StringUtils.isEmpty(currentRoles) ? "<none>" : currentRoles) );

            List<String> availableRoles = allRoles.stream()
                    .filter((role) -> !hostRoles.contains(role))
                    .collect(Collectors.toList());

            String available = availableRoles.stream().collect(Collectors.joining(", "));
            String defaultOption = "done";
            if ( availableRoles.size() > 0 ) {
                defaultOption = availableRoles.get(0);
            }

            roleToAdd = question("Specify a role to edit, or a role to add?",
                    available + (availableRoles.isEmpty() ? "" : ", ") + "done", defaultOption);

            if (!"done".equals(roleToAdd)) {
                if (!allRoles.contains(roleToAdd)) {
                    log("Invalid option");
                    continue;
                }

                JSONObject hostNode = hostPlatform
                        .getJSONObject("body")
                        .getJSONObject("nodes")
                        .getJSONObject(hostName);

                JSONObject defaultNode = defaultPlatform
                        .getJSONObject("body")
                        .getJSONObject("nodes")
                        .getJSONObject("localhost");


                roleConfigurator(roleToAdd, hostNode, defaultNode);
            }
        }
    }

    private boolean review() throws Exception {
        log("\n** Review platform settings **");
        log(hostPlatform.toString(4));
        return "y".equals(question("Save configuration?", "y/n", "y"));
    }

    private void commit() throws Exception {
        if (this.isFirstNode) {
            File hostPlatformFile = new File(confDirectory
                    + File.separatorChar + "defaults"
                    + File.separator + "platform"
                    + File.separator + "local.json");

            log("Saving changes to " + hostPlatformFile.getAbsolutePath());
            FileUtils.writeStringToFile(hostPlatformFile, hostPlatform.toString(4));
        } else {
            log("Posting to " + makeFirstNodeURL()  + "/tenant/0/nodes/" + this.hostName);
            updatePlatformSpec();
        }

        if ( this.firstNodeHost != null ) {
            // modify the conf/custom_env.sh to contain the DG_REPORTING_SERVICE_BASE_URL
            File envFile = new File(this.confDirectory, "custom_env.sh");
            StringBuilder newConfigValue = new StringBuilder();
            if ( envFile.exists() ) {
                String customEnvStr = FileUtils.readFileToString(envFile);
                String[] configLines = customEnvStr.split("\n");
                String reportingServiceHostPort = this.firstNodeHost + ":" + this.firstNodePort;
                boolean updatedReportingServiceURL = false;
                for ( String configLine : configLines ) {
                    String key = configLine.split("=")[0];
                    if ( key.toUpperCase().contains("DG_REPORTING_SERVICE_BASE_URL") ) {
                        newConfigValue.append(key).append("=").append(reportingServiceHostPort);
                        updatedReportingServiceURL = true;
                    } else {
                        // write config back as-is
                        newConfigValue.append(configLine);
                    }
                    newConfigValue.append("\n");
                }
                if ( !updatedReportingServiceURL ) {
                    // append it since it wasn't encountered & updated
                    newConfigValue.append("export DG_REPORTING_SERVICE_BASE_URL").append("=").append(reportingServiceHostPort)
                            .append("\n");
                }
            } else {
                throw new RuntimeException("Could not locate " + envFile.getAbsolutePath() + ", please ensure it exists before running this.");
            }
            FileUtils.writeStringToFile( new File(this.confDirectory, "custom_env.sh"), newConfigValue.toString() );
        }
    }

    private List<String> listPlatformNodeRoles(JSONObject platform, String node) throws Exception {
        // show current platform roles
        JSONObject platformNodes = platform.getJSONObject("body").getJSONObject("nodes");
        if ( platformNodes.has(node) ) {
            JSONObject hostNode = platformNodes.getJSONObject(node);
            Iterator it = hostNode.keys();
            List<String> roles = Lists.newArrayList();
            while (it.hasNext()) {
                roles.add((String) it.next());
            }
            return roles;
        } else {
            return Lists.newArrayList();
        }
    }

    private String question(String label, String options, String defaultValue) {
        StringBuilder questionText = new StringBuilder();
        questionText.append(label);
        if (options != null || defaultValue != null) {
            questionText.append(" (");
            if (options != null) {
                questionText.append(options);
            }

            if (options != null && defaultValue != null) {
                questionText.append("; ");
            }

            if (defaultValue != null) {
                questionText.append("press enter for '" + defaultValue + "'");
            }
        }

        if (options != null || defaultValue != null) {
            questionText.append(")");
        }

        log(questionText.toString());
        Scanner sc = new Scanner(System.in);
        String answer = sc.nextLine();
        return StringUtils.isEmpty(answer) ? defaultValue : answer;
    }

    private void roleConfigurator(String role, JSONObject hostNode, JSONObject defaultNode) throws Exception {
        log("\n** Setting up role '" + role + "' **");
        JSONObject hostNodeRole;
        JSONObject defaultNodeRole;

        if (!defaultNode.has(role)) {
            throw new RuntimeException("Unrecognized role: " + role);
        } else {
            defaultNodeRole = defaultNode.getJSONObject(role);
        }

        if (hostNode.has(role)) {
            hostNodeRole = hostNode.getJSONObject(role);
        } else {
            hostNodeRole = new JSONObject();
            hostNode.put(role, hostNodeRole);
        }

        Iterator it = defaultNode.getJSONObject(role).keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            interrogateNode(hostNodeRole, defaultNodeRole, key, null);
        }
    }

    private void interrogateNode(JSONObject targetNode, JSONObject parentDefaultNode, String key, String parentKey) throws Exception {
        String defaultValue = parentDefaultNode.has(key) ? String.valueOf(parentDefaultNode.get(key)) : null;
        if ( defaultValue == null ) {
            // nothing to do; this branch is closed
            return;
        }

        JSONObject defaultValueNode = null;
        try {
            defaultValueNode = new JSONObject(defaultValue);
        } catch (JSONException e) {
            // its not a JSON object
        }
        if ( defaultValueNode == null ) {
            // its a straight value
            String label = parentKey != null ? (parentKey + "." + key) : key;
            String value = question(label, null, defaultValue);
            targetNode.put(key, value);
        } else {
            // default value is a json object; recursively interrogate the user, providing default values
            JSONObject subNode = new JSONObject();
            targetNode.put(key, subNode);
            Iterator it = defaultValueNode.keys();
            while (it.hasNext()) {
                String subKey = (String) it.next();
                interrogateNode(subNode, defaultValueNode, subKey, parentKey != null ? (parentKey + "." + key) : key);
            }
        }
    }

    private void setupPlatform() throws Exception {
        log("\n** Setting up platform **");

        // platform metadata
        hostPlatform.put("display_name", question("Platform name?", null, defaultPlatform.getString("display_name")));
        hostPlatform.put("description", question("Platform description?", null, defaultPlatform.getString("description")));
        hostPlatform.put("author", question("Author?", null, defaultPlatform.getString("author")));
        hostPlatform.put("body_class", question("Body class?", null, defaultPlatform.getString("body_class")));

        JSONObject hostNode;
        JSONObject hostPlatformNodes = hostPlatform.getJSONObject("body").getJSONObject("nodes");
        // remove the default localhost node
        JSONObject defaultNode = (JSONObject) hostPlatformNodes.remove("localhost");

        if ( "y".equals( question("Setup as development all-in-one platform?", "y/n", "y") ) ) {
            hostNode = new JSONObject( defaultNode.toString() );
            hostPlatformNodes.put(hostName, hostNode);
        } else {
            hostNode = new JSONObject();
            hostPlatformNodes.put(hostName, hostNode);

            // setup rest when platform is first setup
            roleConfigurator("rest", hostNode, defaultNode);
        }

        this.firstNodeHost = this.hostName;
        this.firstNodePort = hostNode.getJSONObject("rest").getInt("port");
    }

    public static void main(String s[]) throws Exception {
        String CONF_DIRECTORY = EnvironmentSettings.getConfDirectory();
        String HOST_NAME = System.getenv("HOST_NAME");
        String FIRST_NODE_HOST = System.getenv("FIRST_NODE_HOST");
        String SERVICE_SHARED_SECRET = EnvironmentSettings.getServiceSharedSecret();

        if (StringUtils.isEmpty(SERVICE_SHARED_SECRET)) {
            throw new RuntimeException("SERVICE_SHARED_SECRET environment variable not specified");
        }

        if (StringUtils.isEmpty(CONF_DIRECTORY)) {
            throw new RuntimeException("CONF_DIRECTORY environment variable not specified");
        }

        if (StringUtils.isEmpty(HOST_NAME)) {
            throw new RuntimeException("HOST_NAME environment variable not specified");
        }

        SetupPlatformNode setupPlatformNode = new SetupPlatformNode(CONF_DIRECTORY, HOST_NAME,
                FIRST_NODE_HOST, SERVICE_SHARED_SECRET);

        boolean accepted = setupPlatformNode.setup();
        if (accepted) {
            setupPlatformNode.commit();
        }
    }

}
