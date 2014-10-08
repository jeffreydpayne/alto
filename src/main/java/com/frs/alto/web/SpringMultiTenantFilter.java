package com.frs.alto.web;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.frs.alto.core.TenantResolver;

public class SpringMultiTenantFilter extends
		AbstractMultiTenantFilter {
	
	public final static String RESOLVER_BEAN_INIT_PROPERTY_NAME = "resolverBeanBame";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		
		if (filterConfig.getInitParameter(RESOLVER_BEAN_INIT_PROPERTY_NAME) != null) {
			try {
				String resolverBeanId = filterConfig.getInitParameter(RESOLVER_BEAN_INIT_PROPERTY_NAME);
				setResolver(context.getBean(resolverBeanId, TenantResolver.class));
			}
			catch (Exception e) {
				throw new ServletException(e);
			}
		}
		else {
			setResolver(context.getBean(TenantResolver.class));
		}

	}

}
