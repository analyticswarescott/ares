package com.aw.auth.filter;

import com.aw.auth.AuthHTTPHeaderNames;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.system.EnvironmentSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Provider
@PreMatching
public class ResponseAuthFilter implements ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseAuthFilter.class);

	private static final String REFERRER_HEADER = "referer";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        //TODO: move this into a config file since it will need to match the
        // domain and port of the server hosting the DG web api which will change per deployment
        String allowOriginHost = EnvironmentSettings.getAllowedOriginHost();

        if (EnvironmentSettings.isDevelopmentMode()) {
            //This code will allow any referrer so there is NO CORS protection at all

            String refOrigin = "http://localhost:8080";
			if (requestContext.getHeaderString(REFERRER_HEADER) != null) {
                refOrigin = requestContext.getHeaderString(REFERRER_HEADER);  // Yes, with the legendary misspelling.
            }

            refOrigin = refOrigin.replace("[", "");
            refOrigin = refOrigin.replace("]", "");

            try {
                URL referrerURL = new URL(refOrigin);
                allowOriginHost = new URL(referrerURL.getProtocol(), referrerURL.getHost(), referrerURL.getPort(), "").toString();
            } catch (MalformedURLException e) {
				logger.error("Unable to parse allow-origin host", e);
            }
            //end of all any referrer
        }

		// Add Access-Control headers
		final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		headers.add("Access-Control-Allow-Origin", allowOriginHost);
		headers.add("Access-Control-Allow-Methods", "GET, POST, HEAD, DELETE, OPTIONS, PUT, PATCH");
		headers.add("Access-Control-Allow-Credentials", "true");
		headers.add("Access-Control-Allow-Headers", AuthHTTPHeaderNames.accessToken.name() + ", Access-Control-Allow-Origin, Authorization, tenantkey, Origin, Content-Type, Content-Range, Content-Disposition, Content-Description");

        // unset thread context
        ThreadLocalStore.unset();

    }
}