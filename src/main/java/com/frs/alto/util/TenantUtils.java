package com.frs.alto.util;

import com.frs.alto.core.TenantMetaData;

public class TenantUtils {

	private static final String DEFAULT_TENANT_ID = "default-tenant";
	
	private static ThreadLocal<TenantMetaData> threadHost = null;
	
	public static void releaseThreadTenant() {
		
		threadHost.remove();
		
	}
	
	public static void setThreadHost(TenantMetaData tenant) {
		
		threadHost = new ThreadLocal<TenantMetaData>();
		threadHost.set(tenant);
		
	}
	
	public static String getThreadTenantIdentifier() {
		TenantMetaData md = getThreadTenant();
		if (md != null) {
			return md.getTenantIdentifier();
		}
		else {
			return DEFAULT_TENANT_ID;
		}
		
	}
	
	public static TenantMetaData getThreadTenant() {
		
		if (threadHost != null) {
			return threadHost.get();
		}
		else {
			return null;
		}
		
	}
	
	public static String generateCacheKey(String baseKey) {
		
		TenantMetaData md = getThreadTenant();
		if (md != null) {
			return md.getTenantIdentifier() + "#" + baseKey;
		}
		else {
			return baseKey;
		}
		
	}
	
}
