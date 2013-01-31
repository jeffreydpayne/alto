package com.frs.alto.nosql.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;


/**
 * Proxies another datasource with a cache.
 * 
 * @author thelifter
 *
 */

public class CachingNoSqlDataSource implements NoSqlDataSource {


	private AltoCache cache = null;
	private NoSqlDataSource proxiedDatasource = null;
	
	
		
	
	@Override
	public String nextId(BaseDomainObject domain) {
		return proxiedDatasource.nextId(domain);
	}

	@Override
	public BaseDomainObject findByKey(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		
		BaseDomainObject value = (BaseDomainObject)getFromCache(clazz, mapper, key.composeUniqueString());
		
		if (value == null) {
			value = proxiedDatasource.findByKey(clazz, mapper, key);
			putToCache(clazz, mapper, key.composeUniqueString(), value);
		}
		
		return value;
	}
	
	protected void putToCache(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String key, Object value) {
		
		if (cache == null) {
			return;
		}
		String region = mapper.getCacheRegion(clazz);
		if (region == null) {
			return;
		}
		cache.put(region, key, value);
		
	}
	
	protected Object getFromCache(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String key) {
		
		if (cache == null) {
			return null;
		}
		String region = mapper.getCacheRegion(clazz);
		if (region == null) {
			return null;
		}
		return cache.get(region, key);
		
		
	}
	

	protected void removeFromCache(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String key) {
		
		if (cache == null) {
			return;
		}
		String region = mapper.getCacheRegion(clazz);
		if (region == null) {
			return;
		}
		cache.remove(region, key);
		
		
	}

	@Override
	public Collection<BaseDomainObject> findByHashKeys(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> hashKeys) {
		
		if (mapper.getCacheRegion(clazz) == null) {
			return proxiedDatasource.findByHashKeys(clazz, mapper, hashKeys);
		}
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		Collection<NoSqlKey> keys = new ArrayList<NoSqlKey>(hashKeys);
		Iterator<NoSqlKey> itr = keys.iterator();
		NoSqlKey key = null;
		BaseDomainObject obj = null;
		while (itr.hasNext()) {
			key = itr.next();
			obj = (BaseDomainObject)getFromCache(clazz, mapper, key.composeUniqueString());
			if (obj != null) {
				results.add(obj);
				itr.remove();
			}
		}
		
		Collection<BaseDomainObject> hardResults = proxiedDatasource.findByHashKeys(clazz, mapper, keys);
		for (BaseDomainObject cand : hardResults) {
			results.add(cand);
			putToCache(clazz, mapper, mapper.getKey(cand).composeUniqueString(), cand);
		}
		
		
		return results;
	}

	@Override
	public Collection<BaseDomainObject> findWholeRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey) {
		return proxiedDatasource.findWholeRange(clazz, mapper, hashKey);
	}

	@Override
	public Collection<BaseDomainObject> findByRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, String startRange, String endRange) {
		return proxiedDatasource.findByRange(clazz, mapper, hashKey, startRange, endRange);
	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, String startRange) {
		return proxiedDatasource.findByUpperRange(clazz, mapper, hashKey, startRange);
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,	String hashKey, String endRange) {
		return proxiedDatasource.findByLowerRange(clazz, mapper, hashKey, endRange);
	}

	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange, Number endRange) {
		
		return proxiedDatasource.findByRange(clazz, mapper, hashKey, startRange, endRange);
		
	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange) {
		
		return findByUpperRange(clazz, mapper, hashKey, startRange);
		
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number endRange) {
		
		return findByLowerRange(clazz, mapper, hashKey, endRange);
		
	}

	@Override
	public void createTable(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper) {
		
		proxiedDatasource.createTable(clazz, mapper);
		
	}

	@Override
	public boolean tableExists(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper) {
		
		return proxiedDatasource.tableExists(clazz, mapper);
		
	}

	@Override
	public boolean isAutoCreateEnabled() {
		
		return proxiedDatasource.isAutoCreateEnabled();
		
	}

	@Override
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		removeFromCache(clazz, mapper, key.composeUniqueString());
		proxiedDatasource.delete(clazz, mapper, key);
	}

	@Override
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> keys) {
		for (NoSqlKey key : keys) {
			removeFromCache(clazz, mapper, key.composeUniqueString());
		}
		proxiedDatasource.delete(clazz, mapper, keys);
	}

	@Override
	public String save(BaseDomainObject domain, NoSqlObjectMapper mapper) {
		
		String key = proxiedDatasource.save(domain, mapper);
		putToCache(domain.getClass(), mapper, key, domain);
		return key;
		
	}

	@Override
	public Collection<String> save(Collection<BaseDomainObject> domains, NoSqlObjectMapper mapper) {
		Collection<String> ids = proxiedDatasource.save(domains, mapper);
		for (BaseDomainObject domain : domains) {
			putToCache(domain.getClass(), mapper, mapper.getKey(domain).composeUniqueString(), domain);
		}
		return ids;
	}

	@Override
	public Collection<String> findAllIds(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		return proxiedDatasource.findAllIds(clazz, mapper);
	}

	@Override
	public Collection<BaseDomainObject> findAll(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		return proxiedDatasource.findAll(clazz, mapper);
	}

	public NoSqlDataSource getProxiedDatasource() {
		return proxiedDatasource;
	}

	public void setProxiedDatasource(NoSqlDataSource proxiedDatasource) {
		this.proxiedDatasource = proxiedDatasource;
	}

	public AltoCache getCache() {
		return cache;
	}

	public void setCache(AltoCache cache) {
		this.cache = cache;
	}
	
	

}
