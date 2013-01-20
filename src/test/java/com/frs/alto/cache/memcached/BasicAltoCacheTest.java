package com.frs.alto.cache.memcached;

import org.apache.commons.id.uuid.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.frs.alto.test.fixture.TestDomain;

@Test
public class BasicAltoCacheTest extends Assert {
	
	@Test
	public void testAltoCacheLifecycle() throws Exception {
		
		MemcachedAltoCache cache = new MemcachedAltoCache();
		cache.afterPropertiesSet();
		
		
		String regionId = "test";
		
		long count = cache.getStatistics().getItemCount();
		
		TestDomain domain = new TestDomain();
		domain.setObjectIdentifier(UUID.randomUUID().toString());
		domain.setName("test");
		
		TestDomain retrieve = (TestDomain)cache.get(regionId, domain.getObjectIdentifier());
		assertNull(retrieve);
		
		cache.put(regionId, domain.getObjectIdentifier(), domain);
		retrieve = (TestDomain)cache.get(regionId, domain.getObjectIdentifier());
		assertNotNull(retrieve);
		
		assertEquals(retrieve.getObjectIdentifier(), domain.getObjectIdentifier());
		assertEquals(retrieve.getName(), domain.getName());
		
		assertTrue(cache.getStatistics().getItemCount() > count);
		
		cache.remove(regionId, domain.getObjectIdentifier());
					
		retrieve = (TestDomain)cache.get(regionId, domain.getObjectIdentifier());
		assertNull(retrieve);
		
		
		
		
	}
	

}
