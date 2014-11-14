package com.frs.alto.core;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class SimpleCachingTenantResolver implements TenantResolver {

	private TenantResolver resolver = null;
	
	private Map<String, TenantMetaData> hostCache = new HashMap<String, TenantMetaData>();
	
	@Override
	public TenantMetaData resolve(ServletRequest request) {
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		String hostName = httpRequest.getServerName();
		
		TenantMetaData result = cacheLookup(hostName);
		if (result != null) {
			return result;
		}
		result = resolver.resolve(request);
		if (result != null) {
			encache(hostName, result);
		}
		else {
			decache(hostName);
		}
		
		return result;
	}
	
	
	
	@Override
	public TenantMetaData byId(String hostName) {
		
		TenantMetaData result = cacheLookup(hostName);
		if (result != null) {
			return result;
		}
		result = resolver.byId(hostName);
		if (result != null) {
			encache(hostName, result);
		}
		else {
			decache(hostName);
		}
		
		return result;
		
	}



	protected TenantMetaData cacheLookup(String hostName) {
		return hostCache.get(hostName);
	}
	
	protected void encache(String hostName, TenantMetaData host) {
		hostCache.put(hostName, host);
		
	}
	
	protected void decache(String hostName) {
		hostCache.remove(hostName);
	}

	public TenantResolver getResolver() {
		return resolver;
	}

	public void setResolver(TenantResolver resolver) {
		this.resolver = resolver;
	}

	
	
}
