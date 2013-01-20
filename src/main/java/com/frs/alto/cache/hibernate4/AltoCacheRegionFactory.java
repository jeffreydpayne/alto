package com.frs.alto.cache.hibernate4;

import java.util.Properties;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;

import com.frs.alto.cache.AltoCache;

public class AltoCacheRegionFactory implements RegionFactory {
	
	private AltoCache altoCache = null;
	private TimestampGenerator timestampGenerator = new SimpleTimestampGenerator();
	private HibernateCacheLockProvider lockProvider = new NoopCacheLockProvider();

	@Override
	public void start(Settings settings, Properties properties) throws CacheException {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	@Override
	public AccessType getDefaultAccessType() {
		return AccessType.NONSTRICT_READ_WRITE;
	}

	@Override
	public long nextTimestamp() {
		return timestampGenerator.next();
	}

	@Override
	public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
		return new AltoEntityRegion(regionName, altoCache, timestampGenerator, lockProvider);
	}

	@Override
	public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
		return new AltoNaturalIdRegion(regionName, altoCache, timestampGenerator, lockProvider);
	}

	@Override
	public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata)
			throws CacheException {
		return new AltoCollectionRegion(regionName, altoCache, timestampGenerator, lockProvider);
	}

	@Override
	public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {
		return new AltoQueryResultsRegion(regionName, altoCache, timestampGenerator, lockProvider);
	}

	@Override
	public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {
		return new AltoTimestampsRegion(regionName, altoCache, timestampGenerator, lockProvider);
	}

	public AltoCache getAltoCache() {
		return altoCache;
	}

	public void setAltoCache(AltoCache altoCache) {
		this.altoCache = altoCache;
	}

	public TimestampGenerator getTimestampGenerator() {
		return timestampGenerator;
	}

	public void setTimestampGenerator(TimestampGenerator timestampGenerator) {
		this.timestampGenerator = timestampGenerator;
	}

	public HibernateCacheLockProvider getLockProvider() {
		return lockProvider;
	}

	public void setLockProvider(HibernateCacheLockProvider lockProvider) {
		this.lockProvider = lockProvider;
	}

	
	
}
