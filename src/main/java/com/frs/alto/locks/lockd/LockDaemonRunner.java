package com.frs.alto.locks.lockd;

/**
 * Startup class for standalone deployments of lockd.
 * 
 * @author thelifter
 *
 */

public class LockDaemonRunner {
	
	private static int portNumber = 11311;
	
	private static SimpleLockDaemon daemon = null;
	
	public static void main(String[] args) throws Exception {
		
		
		if (args.length > 0) {
			portNumber = Integer.parseInt(args[0]);
		}
		
		daemon = new SimpleLockDaemon();
		daemon.setPortNumber(portNumber);
		daemon.setNetworkIfNoPeers(true);
		daemon.setAtomicReplication(true);
		daemon.setMaxThreads(5);
		
		daemon.start();
		
		
	}

}
