package com.frs.alto.locks;

public interface LockProvider {
	
	public String lock(String lockId);
	public boolean unlock(String unlock);
	public boolean isLocked(String lock);

}
