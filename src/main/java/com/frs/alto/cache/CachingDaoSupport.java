package com.frs.alto.cache;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.codec.digest.DigestUtils;

import com.frs.alto.domain.BaseDomainObject;

public abstract class CachingDaoSupport<T extends BaseDomainObject> {
	
	private CacheMode cacheMode = CacheMode.FULL_LOCAL;
	private AltoCache localCache = null;
	private AltoCache remoteCache = null;
	private boolean threadedCacheWrites = true;
	
	private static Executor executor = Executors.newCachedThreadPool();
	
	public CacheMode getCacheMode() {
		return cacheMode;
	}
	public void setCacheMode(CacheMode cacheMode) {
		this.cacheMode = cacheMode;
	}
	public AltoCache getLocalCache() {
		return localCache;
	}
	public void setLocalCache(AltoCache localCache) {
		this.localCache = localCache;
	}
	public AltoCache getRemoteCache() {
		return remoteCache;
	}
	public void setRemoteCache(AltoCache remoteCache) {
		this.remoteCache = remoteCache;
	}
	protected abstract Class<T> getDomainClass();
	
	protected String getRegionName() {
		return getDomainClass().getName();
	}
	
	protected T readFromCache(String id) {
		
		switch (cacheMode) {
			case LOCAL_WITH_REMOTE_VERSION:
				return readFromVersionCache(id);
			case FULL_LOCAL:
				return readFromCache(localCache, id);
			case FULL_REMOTE:
				return readFromCache(remoteCache, id);
		}
		
		return null;
		
	}
	
	protected T readFromCache(AltoCache cache, String id) {
		return (T)cache.get(getRegionName(), id);
	}
	
	protected T readFromVersionCache(final String id) {
		
		FutureTask<String> future = new FutureTask<String>(
                new Callable<String>()
                {
                    public String call()
                    {
                        return (String)remoteCache.get(getRegionName(), id);
                    }
                });
        executor.execute(future);
		
        T result = readFromCache(localCache, id);
        
        while ( (result != null) && !future.isDone()) {
        	try {
        		Thread.sleep(50);
        	}
        	catch (InterruptedException e) {
        		future.cancel(true);
        		throw new RuntimeException(e);
        	}
        }
        
        try {
        	String remoteVersion = future.get();
        	if ( (remoteVersion == null) || !remoteVersion.equals(result.getVersionHash())) {
        		return null;
        	}
        	
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        return result;
        
	}
	
	protected void removeFromCache(String id) {
		
		switch (cacheMode) {
			case FULL_LOCAL:
				localCache.remove(getRegionName(), id);
				break;
			case FULL_REMOTE:
				remoteCache.remove(getRegionName(), id);
				break;
			case LOCAL_WITH_REMOTE_VERSION:
				localCache.remove(getRegionName(), id);
				remoteCache.remove(getRegionName(), id);
				break;
		
		}
		
	}
	
	protected void clearRegion() {
		
		switch (cacheMode) {
			case FULL_LOCAL:
				localCache.clear(getRegionName());
				break;
			case FULL_REMOTE:
				remoteCache.clear(getRegionName());
				break;
			case LOCAL_WITH_REMOTE_VERSION:
				localCache.clear(getRegionName());
				remoteCache.clear(getRegionName());
				break;
		
		}
		
	}
	
	protected String generateVersionHash(T domain) {
		domain.setVersionHash(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
		return domain.getVersionHash();
	}
	
	protected void writeToCache(AltoCache cache, T domain) {
		cache.put(getRegionName(), domain.getObjectIdentifier(), domain);
	}
	
	protected void writeToCache(final T domain) {
		
				
		if (threadedCacheWrites) {
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					doCacheWrite(domain);
					
				}
			});
		}
		else {
			doCacheWrite(domain);
		}

		
	}
	
	
	private void doCacheWrite(final T domain) {
		
		switch (cacheMode) {
			case FULL_REMOTE:
				writeToCache(remoteCache, domain);
				break;
			case FULL_LOCAL:
				writeToCache(localCache, domain);
				break;
			case LOCAL_WITH_REMOTE_VERSION:
				writeToCache(localCache, domain);
				if (domain.getVersionHash() != null) {
					if (!threadedCacheWrites) {
						executor.execute(new Runnable() {
							
							@Override
							public void run() {
								remoteCache.put(getRegionName(), domain.getObjectIdentifier(), domain.getVersionHash());					
							}
						});
					}
					else {
						remoteCache.put(getRegionName(), domain.getObjectIdentifier(), domain.getVersionHash());
					}
				}
		}	
		
	}
	
	public boolean isThreadedCacheWrites() {
		return threadedCacheWrites;
	}
	public void setThreadedCacheWrites(boolean threadedCacheWrites) {
		this.threadedCacheWrites = threadedCacheWrites;
	}
	
	

}
