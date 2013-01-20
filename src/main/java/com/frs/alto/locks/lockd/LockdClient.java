package com.frs.alto.locks.lockd;

import com.frs.alto.locks.LockProvider;

public class LockdClient implements LockProvider {
	
	private String hostName = "localhost";
	private int portNumber = 11311;


	@Override
	public String lock(String lockId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unlock(String unlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocked(String lock) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	
}
