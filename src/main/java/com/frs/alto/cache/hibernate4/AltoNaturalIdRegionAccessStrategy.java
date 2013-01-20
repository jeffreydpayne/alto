package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

import com.frs.alto.cache.AltoCache;

public class AltoNaturalIdRegionAccessStrategy extends BaseAltoRegionAccessStrategy<AltoNaturalIdRegion> implements NaturalIdRegionAccessStrategy {

	public AltoNaturalIdRegionAccessStrategy(AltoCache cache,
			AltoNaturalIdRegion region, HibernateCacheLockProvider lockProvider) {
		super(cache, region, lockProvider);
	}

	@Override
	public NaturalIdRegion getRegion() {
		return region;
	}

	@Override
	public boolean insert(Object key, Object value) throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public boolean afterInsert(Object key, Object value) throws CacheException {
		return true;
	}

	@Override
	public boolean update(Object key, Object value) throws CacheException {
		altoCache.put(region.getName(), key.toString(), value);
		return true;
	}

	@Override
	public boolean afterUpdate(Object key, Object value, SoftLock lock)
			throws CacheException {
		return true;
	}
	
	

	
	
	
	
}
