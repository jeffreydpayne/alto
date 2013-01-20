package com.frs.alto.cache;

public interface CacheKeyGenerator {
	
	public String generateGlobalKey(String regionId, String baseKey);
	public String generateRegionKey(String baseKey);

}
