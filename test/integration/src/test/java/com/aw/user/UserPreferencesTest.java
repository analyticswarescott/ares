package com.aw.user;

import com.aw.BaseIntegrationTest;
import com.aw.document.Document;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UserPreferencesTest extends BaseIntegrationTest {

    @Test
    public void testPatchUserPrefs() throws Exception {
        String tenantID = "1";
        provisionTenant(tenantID);

        String username = UUID.randomUUID().toString();
        addUser(username, tenantID);

        // get user prefs
        JSONObject prefs = authGet(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username, JSONObject.class);
        Assert.assertNotNull("Expected successful GET user meta request", prefs);
        String currentWorkspaceID = assertWorkspaceID(prefs);
        Assert.assertNotNull("Expected user meta to contain a workspace_id", currentWorkspaceID);
        String newWorkspaceID = UUID.randomUUID().toString();

        // update default workspace ID
        JSONObject changeDefaultWorkspace = new JSONObject();
        JSONObject changeDefaultWorkspaceBody = new JSONObject();
        changeDefaultWorkspace.put("body", changeDefaultWorkspaceBody);
        changeDefaultWorkspaceBody.put("default_workspace_name", newWorkspaceID );
        JSONObject newUserPrefs = authPatch(username, com.aw.util.Statics.VERSIONED_REST_PREFIX + "/user_settings/" + username, changeDefaultWorkspace, JSONObject.class);
        Assert.assertNotNull("Expected successful PATCH user meta request", newUserPrefs);

        // refetch current workspace ID, make sure it matches the offered update
        currentWorkspaceID = assertWorkspaceID(newUserPrefs);
        Assert.assertEquals( newWorkspaceID, currentWorkspaceID);
    }

    private String assertWorkspaceID(JSONObject userMetaDoc) throws Exception {
        // expect a non-null default workspace ID in the user meta
        Document userMeta =  new Document(userMetaDoc.toString());
        Assert.assertNotNull("Expected user meta to be returned", userMeta);

        // expect a workspace ID
        JSONObject userPrefs = new JSONObject( userMeta.getBody().toString());
        return (String) userPrefs.get("default_workspace_name");
    }

}