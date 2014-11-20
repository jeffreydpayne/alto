package com.frs.alto.util;

import java.util.concurrent.ConcurrentHashMap;

import com.frs.alto.core.TenantMetaData;

public class TenantUtils {

	public static final String DEFAULT_TENANT_ID = "default-tenant";
	
	private static ConcurrentHashMap<Long, TenantMetaData> tenantMap = new ConcurrentHashMap<Long, TenantMetaData>();
	
	private static String defaultTenantId = DEFAULT_TENANT_ID;
	
	public static void releaseThreadTenant() {
		
		tenantMap.remove(Thread.currentThread().getId());
		
	}
	
	public static void setThreadHost(TenantMetaData tenant) {
		
		tenantMap.put(Thread.currentThread().getId(), tenant);
		
		
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
		
		
		return tenantMap.get(Thread.currentThread().getId());

		
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
