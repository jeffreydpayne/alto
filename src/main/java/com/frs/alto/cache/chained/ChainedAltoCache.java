package com.frs.alto.cache.chained;

import java.util.List;
import java.util.concurrent.Future;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.cache.AltoCacheStatistics;
import com.frs.alto.cache.CacheKeyGenerator;
import com.frs.alto.cache.TenantCacheKeyGenerator;



public class ChainedAltoCache implements AltoCache {
	
	
	private List<AltoCache> cacheChain = null;
	private CacheKeyGenerator keyGenerator = new TenantCacheKeyGenerator();

	@Override
	public Object get(String region, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(String region, String key, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCached(String region, String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove(String region, String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear(String region) {
		// TODO Auto-generated method stub
		
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
	public void get(String region, String key, Future<Object> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void put(String region, String key, Object value, boolean async) {
		// TODO Auto-generated method stub
		
	}
	
	
	

	@Override
	public void clearAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CacheKeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}
	
	

}
