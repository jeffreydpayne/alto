package com.frs.alto.cache.hibernate4;

import java.util.Map;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.GeneralDataRegion;

import com.frs.alto.cache.AltoCache;

public abstract class BaseAltoDataRegion implements GeneralDataRegion {

	protected String name = null;
	protected AltoCache altoCache = null;
	protected TimestampGenerator generator = null;
	protected HibernateCacheLockProvider lockProvider = null;
	
	public BaseAltoDataRegion(String name, AltoCache altoCache, TimestampGenerator gen, HibernateCacheLockProvider lockProvider) {
		super();
		this.name = name;
		this.altoCache = altoCache;
		this.generator = gen;
		this.lockProvider = lockProvider;
	}
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void destroy() throws CacheException {

	}
	
	protected String keyToString(Object key) {
		return key.toString();
	}

	@Override
	public boolean contains(Object key) {
		return altoCache.isCached(name, keyToString(key));
	}

	@Override
	public long getSizeInMemory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getElementCountInMemory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getElementCountOnDisk() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map toMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long nextTimestamp() {
		return generator.next();
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object get(Object key) throws CacheException {
		return altoCache.get(name, keyToString(key));
	}

	@Override
	public void put(Object key, Object value) throws CacheException {
		altoCache.put(name, keyToString(key), value);		
	}

	@Override
	public void evict(Object key) throws CacheException {
		altoCache.remove(name, keyToString(key));

	}

	@Override
	public void evictAll() throws CacheException {
		

	}

}
