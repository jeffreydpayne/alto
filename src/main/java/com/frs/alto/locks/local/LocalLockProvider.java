package com.frs.alto.locks.local;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.frs.alto.locks.LockProvider;

/**
 * Simple lock mechanism backed by java's reentrant locks.  Not recommended in situations where there may be a large number of locks.
 * 
 * @author thelifter
 *
 */


public class LocalLockProvider implements LockProvider {
	
	private ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<String, Lock>();
	
	private Executor executor = Executors.newSingleThreadExecutor();  //used to process lock removals without locking threads

	@Override
	public String lock(String lockId) {
		
		Lock lock = null;
		
		synchronized (lockMap) {
			
			lock = lockMap.get(lockId);
			if (lock == null) {
				lock = new ReentrantLock(true);
				lockMap.put(lockId, lock);
			}
			
		}
		
		
		lock.lock();
		
		return lockId;
		

	}

	@Override
	public boolean unlock(final String unlock) {
		Lock lock = null;
		
			
		lock = lockMap.get(unlock);
		if (lock != null) {
			lock.unlock();
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
	
	protected void doLockPurge(String lockId) {
		
		lockMap.remove(lockId);
		
		
	}

	@Override
	public boolean isLocked(String lock) {
		// TODO Auto-generated method stub
		return false;
	}

}
