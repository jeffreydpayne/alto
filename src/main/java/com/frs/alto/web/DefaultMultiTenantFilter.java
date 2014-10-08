package com.frs.alto.web;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.frs.alto.core.TenantResolver;

public class DefaultMultiTenantFilter extends
		AbstractMultiTenantFilter {
	
	public final static String RESOLVER_CLASS_INIT_PROPERTY_NAME = "resolverClassName";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		if (filterConfig.getInitParameter(RESOLVER_CLASS_INIT_PROPERTY_NAME) != null) {
			try {
				Class resolver = Class.forName(filterConfig.getInitParameter(RESOLVER_CLASS_INIT_PROPERTY_NAME));
				setResolver((TenantResolver)resolver.newInstance());
			}
			catch (Exception e) {
				throw new ServletException(e);
			}
		}

	}

}
