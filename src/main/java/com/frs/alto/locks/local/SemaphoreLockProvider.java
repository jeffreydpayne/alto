package com.frs.alto.locks.local;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.frs.alto.locks.LockProvider;

/**
 * Simple lock mechanism backed by java's reentrant locks.  Not recommended in situations where there may be a large number of locks.
 * 
 * @author thelifter
 *
 */


public class SemaphoreLockProvider implements LockProvider {
	
	private ConcurrentHashMap<String, Semaphore> lockMap = new ConcurrentHashMap<String, Semaphore>();
	
	private Executor executor = Executors.newSingleThreadExecutor();  //used to process lock removals without locking threads

	@Override
	public String lock(String lockId) {
		
		Semaphore lock = null;
		
		synchronized (lockMap) {
			
			lock = lockMap.get(lockId);
			if (lock == null) {
				lock = new Semaphore(1);
				lockMap.put(lockId, lock);
			}
			
		}
		
		try {
			lock.acquire();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		return lockId;
		

	}
	
	protected boolean tryLock(String lockId) {
		
		if (!isLocked(lockId)) {
			lock(lockId);
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
	@Override
	public boolean unlock(final String unlock) {
		Semaphore lock = null;
		
			
		lock = lockMap.get(unlock);
		if (lock != null) {
			lock.release();
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					doLockPurge(unlock);
					
				}
			});
			return true;
		}
		else {
			return false;
		}
			
		
		

	}
	
	protected int getLockCount() {
		
		return lockMap.size();
		
	}
	
	protected void doLockPurge(String lockId) {
		
		lockMap.remove(lockId);
		
		
	}

	@Override
	public boolean isLocked(String lockId) {
		
		Semaphore lock = lockMap.get(lockId);
		
		if (lock == null) {
			return false;
		}
		else {
			return lock.availablePermits() == 0;
		}
		
	}

}
