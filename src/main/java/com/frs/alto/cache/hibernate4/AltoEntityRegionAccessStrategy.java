package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

import com.frs.alto.cache.AltoCache;

public class AltoEntityRegionAccessStrategy extends BaseAltoRegionAccessStrategy<AltoEntityRegion> implements EntityRegionAccessStrategy {
	
	

	public AltoEntityRegionAccessStrategy(AltoCache cache, AltoEntityRegion region,
			HibernateCacheLockProvider lockProvider) {
		super(cache, region, lockProvider);
	}

	@Override
	public EntityRegion getRegion() {
		return region;
	}

	@Override
	public boolean insert(Object key, Object value, Object version)	throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public boolean afterInsert(Object key, Object value, Object version) throws CacheException {
		return true;
	}

	@Override
	public boolean update(Object key, Object value, Object currentVersion, Object previousVersion) throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock) throws CacheException {
		return true;
	}

	
	
}
