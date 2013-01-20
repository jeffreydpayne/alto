package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.TimestampsRegion;

import com.frs.alto.cache.AltoCache;

public class AltoTimestampsRegion extends BaseAltoDataRegion implements TimestampsRegion {

	public AltoTimestampsRegion(String name, AltoCache altoCache, TimestampGenerator generator, HibernateCacheLockProvider lockProvider) {
		super(name, altoCache, generator, lockProvider);
	}
	
	
	
	

}
