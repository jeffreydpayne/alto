package com.frs.alto.cache.memcached;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.cache.AltoCacheStatistics;
import com.frs.alto.cache.AsynchronousCacheSupport;
import com.frs.alto.cache.CacheKeyGenerator;
import com.frs.alto.cache.TenantCacheKeyGenerator;
import com.frs.alto.cache.simple.SimpleStatistics;

public class MemcachedAltoCache extends AsynchronousCacheSupport implements AltoCache, InitializingBean {
		
	private static Log log = LogFactory.getLog(MemcachedAltoCache.class);
	
	private String serverHost = "localhost";
	private int portNumber = 11211;
	private int expiration = 60*60*24*30;
	
	private MemcachedClient client = null;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private CacheKeyGenerator keyGenerator = new TenantCacheKeyGenerator();
	
	private boolean maintainRegions = false;	  
		
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		client = new MemcachedClient(new InetSocketAddress(getServerHost(), getPortNumber()));
		
	}

	@Override
	public Object get(String region, String key) {
		return processValueForRead(client.get(assembleKey(region, key)));
	}

	@Override
	public void put(String region, String key, Object value) {
		
		value = processValueForWrite(value);
		key = assembleKey(region, key);
		
		log.debug("Caching " + key + ": " + value.toString());
				
		while (true) {
			CASValue<Object> cas = client.gets(key);
			if (cas != null) {
				if (client.cas(key, cas.getCas(), value) == CASResponse.OK) {
					break;
				}
			}
			else {
				client.set(key, expiration, value);
				updateRegionKeys(region, key);
				break;
			}
			
		}
		
	}

	@Override
	public boolean isCached(String region, String key) {
		return client.get(assembleKey(region, key)) != null;
	}

	@Override
	public void remove(String region, String key) {
		key = assembleKey(region, key);
		log.debug("Removing: " + key);
		client.delete(key);
	}

	@Override
	public void clear(String region) {
		
		if (maintainRegions) {
			
			log.debug("Clearing Region: " + region);
			
			String regionKey = generateRegionKey(region);
			String keyList = (String)client.get(regionKey);
			
			String[] tokens = StringUtils.split(keyList, "|");
			
			for (String token : tokens) {
				client.delete(token);
			}
		}
	}
	
	protected String generateRegionKey(String region) {
		return keyGenerator.generateRegionKey(region);
	}
	
	protected void updateRegionKeys(String region, String addKey) {
		
		if (maintainRegions) {
		
			String regionKey = generateRegionKey(region);
			
			log.debug("Updating Region Keys: " + regionKey + ": +" + addKey);
				
			CASValue<Object> cas = client.gets(regionKey);
			if (cas != null) {
				client.append(cas.getCas(), regionKey, "|" + addKey);
			}
			else {
				client.set(regionKey, expiration, addKey);
			}
		}
		
		
	}
	
	
	@Override
	public AltoCacheStatistics getStatistics(String region) {
		return getStatistics();
	}

	@Override
	public AltoCacheStatistics getStatistics() {
		Map<String, String> stats = client.getStats().values().iterator().next();
		SimpleStatistics results = new SimpleStatistics();
		results.setDeleteHits(Long.parseLong(stats.get("delete_hits")));
		results.setHitCount(Long.parseLong(stats.get("get_hits")));
		results.setItemCount(Long.parseLong(stats.get("total_items")));
		results.setMissCount(Long.parseLong(stats.get("get_misses")));
		results.setSizeInBytes(Long.parseLong(stats.get("bytes")));
		
		return results;
	}

	protected String assembleKey(String region, String key) {
		return keyGenerator.generateGlobalKey(region, key);
	}
	

	protected Object processValueForRead(Object readValue) {
		
		if (readValue == null) {
			return null;
		}
		
		log.debug("Hit: " + readValue.toString());
		
		if ( (readValue instanceof String) && readValue.toString().startsWith("json:")) {
			String[] tokens = StringUtils.split(readValue.toString(), "\n");
			try {
				Class clazz = Class.forName(StringUtils.split(tokens[0], ":")[1]);
				
				String ser = StringUtils.substring(readValue.toString(), tokens[0].length() + 1);
							
				return mapper.readValue(ser, clazz);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else {
			return readValue;
		}
		
	}
	
	protected Serializable processValueForWrite(Object value) {
		if (value instanceof Serializable){
			return (Serializable)value;
		}
		else {
			try {
				return "json:" + value.getClass().getName() + "\n" + mapper.writeValueAsString(value);
			}
			catch (Exception e) {
				return e;
			}
		}
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public int getExpiration() {
		return expiration;
	}

	public void setExpiration(int expiration) {
		this.expiration = expiration;
	}

	@Override
	public CacheKeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	public boolean isMaintainRegions() {
		return maintainRegions;
	}

	public void setMaintainRegions(boolean maintainRegions) {
		this.maintainRegions = maintainRegions;
	}
	

}
