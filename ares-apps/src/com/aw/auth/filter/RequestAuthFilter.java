package com.aw.auth.filter;

import com.aw.auth.AccessTokenUtil;
import com.aw.auth.AuthHTTPHeaderNames;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.SecurityUtil;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.DocumentMgr;
import com.aw.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;


import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class RequestAuthFilter implements ContainerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(RequestAuthFilter.class);

	private javax.inject.Provider<DocumentMgr> docMgr;

	@javax.inject.Inject
	public RequestAuthFilter(javax.inject.Provider<DocumentMgr> docMgr) {
		this.docMgr = docMgr;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		final String path = requestContext.getUriInfo().getPath();

		if (EnvironmentSettings.isAuthDisabled()) {
			logger.warn(path + " Bypassing security because DISABLE_AUTH_VALIDATION is set to 'true'!  -- tenant ID will be '0'  ");
			SecurityUtil.setThreadSystemAccess();
			return;
		}

		if (path.contains("1.0/dev")) {
			logger.warn(path + " Bypassing security for dev endpoints'  ");
			SecurityUtil.setThreadSystemAccess();
			return;
		}


		if (path.equals(Statics.REST_VERSION + "/ping")) {
			logger.warn(path + " Bypassing security for ping  ");
			return;
		}

		if (path.equals("swagger.json")){
			logger.warn(path + " Bypassing security for swagger endpoints  ");
			return;
		}

		// Check the request to see if the service shared secret was passed
		String sharedSecret = EnvironmentSettings.getServiceSharedSecret();
		if (sharedSecret != null) {
			String requestSharedSecret = requestContext.getHeaderString(AuthHTTPHeaderNames.serviceSharedSecret.name());
			if (requestSharedSecret != null && sharedSecret.equals(requestSharedSecret)) {
				logger.debug(path + " Bypassing security because service shared secret was provided -- tenant ID will be '0'  ");
				SecurityUtil.setThreadSystemAccess();
				return;
			}
		}

		//Check the request to see if it has our security token,
		//if it does then we can trust the claims in the token and
		//set the security context.

		// IMPORTANT!!! First, Acknowledge any pre-flight test from browsers for this case before validating the headers (CORS stuff)

		if (requestContext.getMethod().equals(HttpMethod.OPTIONS)) {
			throw new WebApplicationException(Status.OK);
		}

		AccessTokenUtil userAuthenticator = AccessTokenUtil.getInstance();

		// The request must either be a login request or contain a valid token on the header

		Boolean securityTokenIsValid = false;
		String authToken = "";

		// For any other methods besides login, the authToken must be verified

		//TODO: this check for path containing /Start is very generic, and maybe should be more specific for
		//fully defined paths or exclusions added
		if (!path.contains("1.0/Account/Login") && !path.contains("/Start")) {

			authToken = requestContext.getHeaderString(AuthHTTPHeaderNames.accessToken.name());

			if (authToken == null) {
				// try the authorization header
				String authorization = requestContext.getHeaderString("Authorization");
				if (authorization != null) {
					// try bearer, but make sure it is also long enough to contain a token (was getting a downstream exception if the word bearer was sent without a token.
					if (authorization.contains("Bearer") && authorization.length() > 50) {
						String[] bearerParts = authorization.split(" ");
						authToken = bearerParts[1];
					}
				}
			}

			try {
				if (!userAuthenticator.isAuthTokenValid(authToken)) {
					throw new WebApplicationException(Status.UNAUTHORIZED);
				} else {
					securityTokenIsValid = true;
				}
			} catch (Exception e) {
				throw new WebApplicationException(Status.UNAUTHORIZED);
			}
		}

		//It not valid or is a login attempt then we will not have set any security context below,
		//but will pass on through because it could be a login attempt
		if (securityTokenIsValid) {

			PlatformSecurityContext ctx = userAuthenticator.getSecurityContextFromClaims(authToken);

			try {
				SecurityUtil.setThreadSystemAccess();
				if (!docMgr.get().getSysDocHandler().tenantExists(ctx.getTenantID())) {
					throw new WebApplicationException("Invalid tenant ID " + ctx.getTenantID());
				}
			}
			catch (Exception ex) {
				logger.error("error validating tenant ID", ex);
				throw new WebApplicationException("Error validating tenant ID " + ctx.getTenantID());
			}
			finally {
				SecurityUtil.unsetThreadSystemAccess();
			}

			ThreadLocalStore.set(ctx);
		}

	}

}