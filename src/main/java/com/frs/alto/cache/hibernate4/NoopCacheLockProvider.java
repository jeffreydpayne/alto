package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.access.SoftLock;

public class NoopCacheLockProvider implements HibernateCacheLockProvider {

	@Override
	public SoftLock lock(String lockId) {
		return new SoftLock() {};
	}

	@Override
	public void unlock(SoftLock lock) {
		
	}

}
