package com.frs.alto.cache.couchbase;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.couchbase.client.CouchbaseClient;
import com.frs.alto.cache.AltoCache;
import com.frs.alto.cache.AltoCacheStatistics;
import com.frs.alto.cache.AsynchronousCacheSupport;
import com.frs.alto.cache.CacheKeyGenerator;
import com.frs.alto.cache.TenantCacheKeyGenerator;
import com.frs.alto.dao.couchbase.CouchbaseDaoSupport;

public class CouchbaseCache extends AsynchronousCacheSupport implements AltoCache {
	
	private Logger logger = Logger.getLogger(CouchbaseDaoSupport.class.getName());
	
	private CouchbaseClient client;
	
	private CacheKeyGenerator keyGenerator = new TenantCacheKeyGenerator();
	
	

	@Override
	public void startup() {
		// noop
		
	}

	@Override
	public void shutdown() {
		// noop
		
	}

	@Override
	public void setAutoStartup(boolean autoStartup) {
		//noop
		
	}

	@Override
	public Object get(String region, String key) {
		
		String realKey = keyGenerator.generateGlobalKey(region, key);
		return client.get(realKey);

	}

	@Override
	public void put(String region, String key, Object value) {
		
		String realKey = keyGenerator.generateGlobalKey(region, key);
		client.set(realKey, value);
		
	}

	@Override
	public boolean isCached(String region, String key) {
		
		return get(region, key) != null;
		
	}

	@Override
	public void remove(String region, String key) {
		
		String realKey = keyGenerator.generateGlobalKey(region, key);
		client.delete(realKey);
		
	}

	@Override
	public void clear(String region) {
		
	}

	@Override
	public void clearAll() {
		client.flush();
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
		return getKeyGenerator();
	}

	public CouchbaseClient getClient() {
		return client;
	}

	public void setClient(CouchbaseClient client) {
		this.client = client;
	}

}
