package com.aw.unity.odata;

import com.aw.common.rest.Localizer;
import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.DefaultSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.system.EnvironmentSettings;
import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/AuthenticationFilter")
public class AuthenticationFilter implements Filter {

	private ServletContext context;

	public static final Logger logger = Logger.getLogger(AuthenticationFilter.class);

	public void init(FilterConfig fConfig) throws ServletException {
		this.context = fConfig.getServletContext();
		this.context.log("AuthenticationFilter initialized");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String uri = req.getRequestURI();
		logger.info("Requested Resource::"+uri);

		if (EnvironmentSettings.isAuthDisabled() ) {
			logger.info("Bypassing security because DISABLE_AUTH_VALIDATION is set to 'true'!  -- tenant ID will be '0'  ");

			AuthenticatedUser u = new AuthenticatedUser("aw", "example", "admin", Localizer.LOCALE_EN_US ,null, null, null);
			ThreadLocalStore.set(new DefaultSecurityContext(u));
			chain.doFilter(request, response);
		}

/*		HttpSession session = req.getSession(false);

		if(session == null && !(uri.endsWith("html") || uri.endsWith("LoginServlet"))){
			this.context.log("Unauthorized access request");
			res.sendRedirect("login.html");
		}else{
			// pass the request along the filter chain
			chain.doFilter(request, response);
		}*/


	}


	public void destroy() {
		//close any resources here
	}

}
