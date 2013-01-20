package com.frs.alto.core;

import java.util.HashMap;
import java.util.Map;

public class CachingTenantResolver implements TenantResolver {

	private TenantResolver resolver = null;
	
	private Map<String, TenantMetaData> hostCache = new HashMap<String, TenantMetaData>();
	
	@Override
	public TenantMetaData resolve(String hostName) {
		
		TenantMetaData result = cacheLookup(hostName);
		if (result != null) {
			return result;
		}
		result = resolver.resolve(hostName);
		if (result != null) {
			encache(result);
		}
		else {
			decache(hostName);
		}
		
		return result;
	}
	
	protected TenantMetaData cacheLookup(String hostName) {
		return hostCache.get(hostName);
	}
	
	protected void encache(TenantMetaData host) {
		hostCache.put(host.getTenantName(), host);
		
	}
	
	protected void decache(String hostName) {
		hostCache.remove(hostName);
	}

}
