package com.frs.alto.util;

import com.frs.alto.core.TenantMetaData;

public class TenantUtils {

	public static final String DEFAULT_TENANT_ID = "default-tenant";
	
	private static ThreadLocal<TenantMetaData> threadHost = null;
	
	private static String defaultTenantId = DEFAULT_TENANT_ID;
	
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
			return defaultTenantId;
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

	public static String getDefaultTenantId() {
		return defaultTenantId;
	}

	public static void setDefaultTenantId(String defaultTenantId) {
		TenantUtils.defaultTenantId = defaultTenantId;
	}
	
	
	
	
}
