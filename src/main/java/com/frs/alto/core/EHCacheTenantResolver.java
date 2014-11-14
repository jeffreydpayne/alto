package com.frs.alto.core;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EHCacheTenantResolver implements TenantResolver {

	private TenantResolver resolver = null;
	
	private CacheManager cacheManager = null;
	
	private static String cacheRegionName = EHCacheTenantResolver.class.getName();
	
	
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
			encache(result);
		}
		else {
			decache(hostName);
		}
		
		return result;
	}
	
	
	
	
	@Override
	public TenantMetaData byId(String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}




	protected TenantMetaData cacheLookup(String hostName) {
		
		Element element = cacheManager.getCache(cacheRegionName).get(hostName);
		
		if (element != null) {
			return (TenantMetaData)element.getValue();
		}
		
		return null;
	}
	
	protected void encache(TenantMetaData host) {
		
		cacheManager.getCache(cacheRegionName).put(new Element(host.getTenantIdentifier(), host));
		
	}
	
	protected void decache(String hostName) {
		
		cacheManager.getCache(cacheRegionName).remove(hostName);
	
	}

}
