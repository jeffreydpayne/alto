package com.frs.alto.cache.simple;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.cache.AltoCacheStatistics;
import com.frs.alto.cache.AsynchronousCacheSupport;
import com.frs.alto.cache.CacheKeyGenerator;
import com.frs.alto.cache.TenantCacheKeyGenerator;

public class SimpleAltoCache extends AsynchronousCacheSupport implements AltoCache {
	
	private Map<String, Map<String, Object>> regionMap = Collections.synchronizedMap(new HashMap<String, Map<String, Object>>());
	private int regionSize = 0;
	
	private CacheKeyGenerator keyGenerator = new TenantCacheKeyGenerator();
		
	protected Map<String, Object> getRegion(String regionName) {
		
		Map<String, Object> cache = regionMap.get(regionName);
		
		if (cache == null) {
			if (regionSize < 1) {
				cache = Collections.synchronizedMap(new HashMap<String, Object>());
			}
			else {
				cache = Collections.synchronizedMap(new LRUMap(regionSize));
			}
			regionMap.put(regionName, cache);
		}
		
		return cache;
		
	}
	
	protected String getCacheKey(String key) {
		return keyGenerator.generateRegionKey(key);
	}
	
	@Override
	public Object get(String region, String key) {
		
		return getRegion(region).get(getCacheKey(key));
		
	}

	@Override
	public void put(String region, String key, Object value) {
		
		getRegion(region).put(getCacheKey(key), value);

	}

	@Override
	public boolean isCached(String region, String key) {
		return getRegion(region).containsKey(getCacheKey(key));
	}

	@Override
	public void remove(String region, String key) {
		getRegion(region).remove(getCacheKey(key));

	}

	@Override
	public void clear(String region) {
		getRegion(region).clear();
	}

	public int getRegionSize() {
		return regionSize;
	}

	public void setRegionSize(int regionSize) {
		this.regionSize = regionSize;
	}

	@Override
	public AltoCacheStatistics getStatistics(String region) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AltoCacheStatistics getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public CacheKeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	


}
