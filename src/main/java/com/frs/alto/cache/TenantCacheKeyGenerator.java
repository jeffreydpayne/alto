package com.frs.alto.cache;

import com.frs.alto.util.TenantUtils;

public class TenantCacheKeyGenerator implements CacheKeyGenerator {

	@Override
	public String generateGlobalKey(String regionId, String baseKey) {
		if (baseKey.contains(regionId)) {
			return generateRegionKey(baseKey);
		}
		else {
			return TenantUtils.getThreadTenantIdentifier() + "#" + regionId + "#" + baseKey;
		}
	}

	@Override
	public String generateRegionKey(String baseKey) {
		return TenantUtils.getThreadTenantIdentifier() + "#" + baseKey;
	}
	
	
	
	

}
