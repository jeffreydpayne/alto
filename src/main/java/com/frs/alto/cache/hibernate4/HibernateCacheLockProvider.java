package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.access.SoftLock;

public interface HibernateCacheLockProvider {
	
	public SoftLock lock(String lockId);
	public void unlock(SoftLock lock);

}
