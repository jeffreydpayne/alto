package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.QueryResultsRegion;

import com.frs.alto.cache.AltoCache;

public class AltoQueryResultsRegion extends BaseAltoDataRegion implements QueryResultsRegion {

	public AltoQueryResultsRegion(String name, AltoCache altoCache, TimestampGenerator gen,HibernateCacheLockProvider lockProvider) {
		super(name, altoCache, gen, lockProvider);
	}

	
	
	
}
