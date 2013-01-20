package com.frs.alto.cache.memcached;

import java.net.InetSocketAddress;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.spy.memcached.MemcachedClient;

@Test
public class ConnectivityTest extends Assert {

	@Test
	public void testCacheAvailability() throws Exception {
		
		String key = "test";
		String value = "value";
		MemcachedClient c = new MemcachedClient(new InetSocketAddress("localhost", 11211));
		
		c.set(key, 3600, value);
		// Retrieve a value (synchronously).
		Object myObject=c.get(key);

		assertNotNull(myObject);
		assertEquals(myObject, value);
				
	}
	
}
