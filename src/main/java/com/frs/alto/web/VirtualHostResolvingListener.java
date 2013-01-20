package com.frs.alto.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.frs.alto.core.TenantMetaData;
import com.frs.alto.core.TenantResolver;
import com.frs.alto.util.TenantUtils;

public abstract class VirtualHostResolvingListener implements Filter {

	
	private TenantResolver resolver = null;
	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String hostName = httpRequest.getServerName();
		
		TenantMetaData metaData = resolver.resolve(hostName);
		TenantUtils.setThreadHost(metaData);
				
		chain.doFilter(request, response);

		TenantUtils.releaseThreadTenant();
		
	}

	@Override
	public void destroy() {
		
	}

	public TenantResolver getResolver() {
		return resolver;
	}

	public void setResolver(TenantResolver resolver) {
		this.resolver = resolver;
	}

}
