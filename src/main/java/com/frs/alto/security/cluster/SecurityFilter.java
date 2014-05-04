package com.frs.alto.security.cluster;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import sun.org.mozilla.javascript.internal.SecurityController;

public class SecurityFilter implements Filter {
	
	private ClusterSecurityController securityController;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		securityController = context.getBean(ClusterSecurityController.class);

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		securityController.visitRequest((HttpServletRequest)request, (HttpServletResponse)response);
		chain.doFilter(request, response);

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
