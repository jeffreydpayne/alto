package com.frs.alto.cache;

import java.util.concurrent.Future;

/**
 * A simple abstraction that can sit in front of various cache implementations.
 * 
 */
public interface AltoCache {

	
	public void startup();
	public void shutdown();
	public void setAutoStartup(boolean autoStartup);
	public Object get(String region, String key);
	public void put(String region, String key, Object value);
	public boolean isCached(String region, String key);
	public void remove(String region, String key);
	public void clear(String region);	
	public void clearAll();
	public AltoCacheStatistics getStatistics(String region);
	public AltoCacheStatistics getStatistics();
	public void get(String region, String key, Future<Object> callback);
	public void put(String region, String key, Object value, boolean async);
	public CacheKeyGenerator getKeyGenerator();
	
	
}
