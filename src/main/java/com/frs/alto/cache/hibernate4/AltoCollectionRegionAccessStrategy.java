package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;

import com.frs.alto.cache.AltoCache;

public class AltoCollectionRegionAccessStrategy extends	BaseAltoRegionAccessStrategy<AltoCollectionRegion> implements CollectionRegionAccessStrategy {

	public AltoCollectionRegionAccessStrategy(AltoCache cache, AltoCollectionRegion region, HibernateCacheLockProvider lockProvider) {
		super(cache, region, lockProvider);
	}

	@Override
	public CollectionRegion getRegion() {
		return region;
	}
	
	

	
	
}
