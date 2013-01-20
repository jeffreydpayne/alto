package com.frs.alto.cache.hibernate4;

import java.util.Comparator;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;

import com.frs.alto.cache.AltoCache;

public class AltoEntityRegion extends BaseAltoDataRegion implements EntityRegion {
	

	public AltoEntityRegion(String name, AltoCache altoCache, TimestampGenerator gen, HibernateCacheLockProvider lockProvider) {
		super(name, altoCache, gen, lockProvider);
	}

	@Override
	public boolean isTransactionAware() {
		return false;
	}

	@Override
	public CacheDataDescription getCacheDataDescription() {
		return new CacheDataDescription() {
			
			@Override
			public boolean isVersioned() {
				return false;
			}
			
			@Override
			public boolean isMutable() {
				return true;
			}
			
			@Override
			public Comparator getVersionComparator() {
				return null;
			}
		};
	}

	@Override
	public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
		return new AltoEntityRegionAccessStrategy(altoCache, this, lockProvider);
	}
	
	
	

}
