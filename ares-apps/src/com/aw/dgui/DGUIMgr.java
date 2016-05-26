package com.aw.dgui;

import javax.inject.Provider;

import org.codehaus.jettison.json.JSONObject;

import com.aw.common.rest.RestMgrBase;
import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.util.JSONUtils;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;
import com.aw.platform.PlatformMgr;

/**
 *
 * public interfaces for UnityServer
 * @deprecated This class will be removed in an upcoming build
 */
public class DGUIMgr extends RestMgrBase {

	Provider<DocumentHandler> docs;
	Provider<PlatformMgr> platformMgr;

	public DGUIMgr(Provider<DocumentHandler> docProvider, Provider<PlatformMgr> platformMgr) {
		this.docs = docProvider;
		this.platformMgr = platformMgr;
	}

    public static int _maxRows = 100000; //TODO: wrap ES query in class that reads config to avoid tight-coupling of query max to any one service
    public String DocDir;
    public String QueryDir;

    public String getUser(String userID) throws Exception {
        if ( userID.equals("me") ) {
            PlatformSecurityContext securityContext = ThreadLocalStore.get();
            String tenantID = securityContext.getTenantID();

            AuthenticatedUser userPrincipal = securityContext.getUser();
            String actualUserID = userPrincipal.getId();
            String userName = userPrincipal.getName();

            //get stored user info
            Document userSettings = this.docs.get().getDocument(DocumentType.USER_SETTINGS, actualUserID);
            JSONObject body = userSettings.getBody();

            JSONObject userMeta = new JSONObject();
            userMeta.put("tid", tenantID);
            userMeta.put("user_name", userName);
            userMeta.put("user_id", actualUserID);

            body.put("userMeta", userMeta);

            return userSettings.toJSON();
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public String updateUserPrefs(String userID, JSONObject preferencesMeta) throws Exception {
        if ( userID.equals("me") ) {
            PlatformSecurityContext securityContext = ThreadLocalStore.get();
            AuthenticatedUser userPrincipal = securityContext.getUser();
            String actualUserID = userPrincipal.getId();

            //get stored user info
            Document userSettings = this.docs.get().getDocument(DocumentType.USER_SETTINGS, actualUserID);
            JSONObject body = userSettings.getBody();

            JSONObject prefsBodyIncoming = (JSONObject) preferencesMeta.get("body");
            // update settings with passed in data
            JSONUtils.overlay(prefsBodyIncoming, body);
            userSettings.setBody(body);

            // store the document back
            this.docs.get().updateDocument(userSettings);

            // return the updated user
            return getUser("me");
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

}
