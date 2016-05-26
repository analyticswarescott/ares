package com.aw.unity.security;

import org.apache.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UnityRequestFilter implements ContainerRequestFilter {
	public static final Logger logger = Logger.getLogger(UnityRequestFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		//print cookies
		requestContext.getCookies().forEach((key, c) -> logger.debug("COOKIE MONSTER-------" + key + " :: " + c.getValue()));

		final UriInfo uriInfo = requestContext.getUriInfo();
		final String requestURI = uriInfo.getAbsolutePath().toString();

		//if (requestURI.contains("odata")) {


		final String baseURI = uriInfo.getBaseUri().toString();

		if (requestURI.endsWith("#")) {
			try {
				logger.info("unslashed URI: " + requestURI);
				Response.temporaryRedirect(new URI(baseURI)).build();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//}


		//try to set the headers correctly

		if (requestURI.contains("odata")) {
			MultivaluedMap<String, String> headers = requestContext.getHeaders();

			String format = (String) requestContext.getProperty("format");
			if (format == null) {
				format = "xml";
			}

			if ("json".equalsIgnoreCase(format)) {
				headers.add("Accept", MediaType.APPLICATION_JSON);
			} else if ("xml".equalsIgnoreCase(format)) {
				//headers.remove("Accept");
				//headers.r("Accept" ,"application/atom+xml");
				headers.add("Accept", "application/atom+xml,application/atomsvc+xml,application/xml");
			}

		}

	}

}
