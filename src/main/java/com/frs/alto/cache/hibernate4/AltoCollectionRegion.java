package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.internal.CacheDataDescriptionImpl;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;

import com.frs.alto.cache.AltoCache;

public class AltoCollectionRegion extends BaseAltoDataRegion implements CollectionRegion {

		
	public AltoCollectionRegion(String name, AltoCache altoCache, TimestampGenerator gen, HibernateCacheLockProvider lockProvider) {
		super(name, altoCache, gen, lockProvider);
	}

	@Override
	public boolean isTransactionAware() {
		return false;
	}

	@Override
	public CacheDataDescription getCacheDataDescription() {
		return new CacheDataDescriptionImpl(true, false, null);
	}

	@Override
	public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
		return new AltoCollectionRegionAccessStrategy(altoCache, this, lockProvider);
	}
	
	

}
