package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

import com.frs.alto.cache.AltoCache;

public abstract class BaseAltoRegionAccessStrategy<R extends BaseAltoDataRegion> implements RegionAccessStrategy {
	
	protected AltoCache altoCache = null;
	protected R region;
	protected HibernateCacheLockProvider lockProvider = null;
	
	
	public BaseAltoRegionAccessStrategy(AltoCache cache, R region, HibernateCacheLockProvider lockProvider) {
		this.altoCache = cache;
		this.region = region;
		this.lockProvider = lockProvider;
	}
	
	
	
	@Override
	public Object get(Object key, long txTimestamp) throws CacheException {
		return altoCache.get(region.getName(), key.toString());
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp,	Object version) throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride) throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public SoftLock lockItem(Object key, Object version) throws CacheException {
		return lockProvider.lock(key.toString());
	}

	@Override
	public SoftLock lockRegion() throws CacheException {
		return lockProvider.lock(region.getName());
	}

	@Override
	public void unlockItem(Object key, SoftLock lock) throws CacheException {
		lockProvider.unlock(lock);
		
	}

	@Override
	public void unlockRegion(SoftLock lock) throws CacheException {
		lockProvider.unlock(lock);
		
	}

	@Override
	public void remove(Object key) throws CacheException {
		altoCache.remove(region.getName(), key.toString());
		
	}

	@Override
	public void removeAll() throws CacheException {
		altoCache.clear(region.getName());
	}

	@Override
	public void evict(Object key) throws CacheException {
		altoCache.remove(region.getName(), key.toString());
	}

	@Override
	public void evictAll() throws CacheException {
		altoCache.clear(region.getName());
		
	}


}
