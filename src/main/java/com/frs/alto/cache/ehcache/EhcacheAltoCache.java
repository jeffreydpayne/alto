package com.frs.alto.cache.ehcache;

import java.util.logging.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.cache.AltoCacheStatistics;
import com.frs.alto.cache.AsynchronousCacheSupport;
import com.frs.alto.cache.CacheKeyGenerator;
import com.frs.alto.cache.TenantCacheKeyGenerator;

public class EhcacheAltoCache extends AsynchronousCacheSupport implements AltoCache {
	
	private CacheManager cacheManager = null;
	private boolean autoCreate = true;
	
	private Logger logger = Logger.getLogger(EhcacheAltoCache.class.getName());
	
	
	private CacheKeyGenerator keyGenerator = new TenantCacheKeyGenerator();
	
	
	protected Cache getRegionCache(String region) {
		Cache cache = cacheManager.getCache(region);
		if ( (cache == null) && autoCreate) {
			cacheManager.addCache(region);
			cache = cacheManager.getCache(region);
		}
		return cache;
	}

	@Override
	public Object get(String region, String key) {
		key = keyGenerator.generateRegionKey(key);
		Element element =  getRegionCache(region).get(key);
		
		
		
		if (element != null) {
			return element.getObjectValue();
		}
		else {
			return null;
		}
	}

	@Override
	public void put(String region, String key, Object value) {
		key = keyGenerator.generateRegionKey(key);
		getRegionCache(region).put(new Element(key, value));		
	}

	@Override
	public boolean isCached(String region, String key) {
		key = keyGenerator.generateRegionKey(key);
		return getRegionCache(region).isKeyInCache(key);
	}

	@Override
	public void remove(String region, String key) {
		key = keyGenerator.generateRegionKey(key);
		getRegionCache(region).remove(key);
	}

	@Override
	public void clear(String region) {
		getRegionCache(region).removeAll();
		
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

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	@Override
	public void clearAll() {
		
		cacheManager.clearAll();
		
	}

	@Override
	public void startup() {
		
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void setAutoStartup(boolean autoStartup) {
		
	}
	
	

}
