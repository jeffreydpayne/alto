package com.frs.alto.locks.lockd;

import com.frs.alto.locks.LockProvider;

public interface LockDaemon extends LockProvider {
	
	
	public void start() throws Exception;
	public void shutdown() throws Exception;
	
}
