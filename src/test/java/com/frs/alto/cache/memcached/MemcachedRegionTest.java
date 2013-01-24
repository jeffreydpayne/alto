package com.frs.alto.cache.memcached;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.dao.dynamodb.LocalUUIDGenerator;

@ContextConfiguration(locations={"classpath:spring-env-test-memcached.xml"})
public class MemcachedRegionTest extends AbstractTestNGSpringContextTests {

	
	@Autowired
	private AltoCache altoCache = null;
	private LocalUUIDGenerator generator = new LocalUUIDGenerator();
	
	@Test
	public void testCacheEviction() throws Exception {
		
		
		String region1 = "region1";
		String region2 = "region2";
		
		Set<String> region1ids = new HashSet<String>();
		Set<String> region2ids = new HashSet<String>();
		
		String genKey = null;
		
		for (int i = 0 ; i < 20; i++) {
			genKey = generator.generateStringIdentifier(null);
			region1ids.add(genKey);
			region2ids.add(genKey);
			altoCache.put(region1, genKey, genKey);
			altoCache.put(region2, genKey, genKey);
			
		}
	
		for (String id : region1ids) {
			Assert.assertNotNull(altoCache.get(region1, id));
		}
		
		for (String id : region2ids) {
			Assert.assertNotNull(altoCache.get(region2, id));
		}
		
		altoCache.clear(region1);
		
		for (String id : region1ids) {
			Assert.assertNull(altoCache.get(region1, id));
		}
		
		for (String id : region2ids) {
			Assert.assertNotNull(altoCache.get(region2, id));
		}
		
			
				
	}
	
	
	
	
}
