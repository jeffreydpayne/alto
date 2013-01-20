package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.internal.CacheDataDescriptionImpl;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

import com.frs.alto.cache.AltoCache;

public class AltoNaturalIdRegion extends BaseAltoDataRegion implements NaturalIdRegion {
		

	public AltoNaturalIdRegion(String name, AltoCache altoCache,
			TimestampGenerator gen, HibernateCacheLockProvider lockProvider) {
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
	public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
		return new NaturalIdRegionAccessStrategy() {
			
			@Override
			public void unlockRegion(SoftLock lock) throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void unlockItem(Object key, SoftLock lock) throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void removeAll() throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void remove(Object key) throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean putFromLoad(Object key, Object value, long txTimestamp,
					Object version, boolean minimalPutOverride) throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean putFromLoad(Object key, Object value, long txTimestamp,
					Object version) throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public SoftLock lockRegion() throws CacheException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public SoftLock lockItem(Object key, Object version) throws CacheException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object get(Object key, long txTimestamp) throws CacheException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void evictAll() throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void evict(Object key) throws CacheException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean update(Object key, Object value) throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean insert(Object key, Object value) throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public NaturalIdRegion getRegion() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean afterUpdate(Object key, Object value, SoftLock lock)
					throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean afterInsert(Object key, Object value) throws CacheException {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
	
	
	

}
