package com.aw.rest.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Api
@Path(com.aw.util.Statics.REST_VERSION + "/edr")
public class DGUIEDRResource extends RestResourceBase  {
private static final Logger logger = LoggerFactory.getLogger(DGUIEDRResource.class);

	@Context ServletContext _context; //get startup info from web.xml

}
