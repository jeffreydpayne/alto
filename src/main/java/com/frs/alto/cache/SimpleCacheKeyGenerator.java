package com.frs.alto.cache;

public class SimpleCacheKeyGenerator implements CacheKeyGenerator {

	@Override
	public String generateGlobalKey(String regionId, String baseKey) {
		return regionId + '#' + baseKey;
	}

	@Override
	public String generateRegionKey(String baseKey) {
		return baseKey;
	}
	
	

}
