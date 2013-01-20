package com.frs.alto.locks.lockd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frs.alto.locks.local.SemaphoreLockProvider;

/**
 * Implementation of lockd designed for inclusion in other applications.  This implementation assumes total lock replication and is 
 * not segmented.  It just mirrors locks to other nodes.  
 * 
 * Extends the client in order to support fast lock lookups if the lock is maintained locally.
 * 
 * 
 * @author thelifter
 *
 */

public class SimpleLockDaemon extends SemaphoreLockProvider implements LockDaemon {
	
	private static Log log = LogFactory.getLog(SimpleLockDaemon.class);
	
	private String[] rawPeers = null;
	private Map<String, Peer> peers = null;
	private int portNumber = 11311;
	private boolean atomicReplication = false;  
	private boolean networkIfNoPeers = true;  //determines whether or not networking should be started if there are no peers
	
	
	private PeerDiscoveryService peerDiscoverService = null;
	private ServerSocket socket = null;
	private int maxThreads = 1;
	private boolean peerRegistrationEnabled = false;
	
	
	private Executor serverExecutor = null;
	
	public void replicateLock(String lockId) {
		
		
		
	}
	
	public void replicateUnlock(String lockId) {
		
		
	}

	
	protected boolean isNetworkingEnabled() {
		
		if (networkIfNoPeers) {
			return true;
		}
		else if (peers == null) {
			return false;
		}
		else if (peers.size() == 0) {
			return false;
		}
		
		return true;
		
	}


	@Override
	public void start() throws Exception {
		
		if (peerDiscoverService != null) {
			rawPeers = peerDiscoverService.discoverPeers();
		}
			
	    if (isNetworkingEnabled()) {
	    	startNetworking();
	    }
	
		
	}
	
	protected void startNetworking() throws Exception {
		
		log.info("Starting lockd server on port " + portNumber);
		
		serverExecutor = Executors.newFixedThreadPool(maxThreads);
		
		socket = new ServerSocket(portNumber);
		while (true) {
			final Socket clientSocket = socket.accept();
			serverExecutor.execute(new Runnable() {
				@Override
				public void run() {
					processConnection(clientSocket);					
				}
			});
		}
		
		
	}

	public void processConnection(Socket clientSocket) {
		
		try {
		
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.write("lockd> ");
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String inputLine = null;
			
			while ((inputLine = in.readLine()) != null) {   
				LockdCommand cmd = null;
			
				try {
					cmd = LockdCommand.fromInput(inputLine);
					if (cmd == null) {
				    	writeResponse(out, "Invalid command.");
				    }
				}
				catch (IllegalArgumentException e) {
					writeResponse(out, "Invalid command.");
				}
			    
			    switch (cmd.command) {
			    	case QUIT:
			    		writeResponse(out, "Go in peace...\n");
			    		clientSocket.close();
			    		break;
			    	case TIME:
			    		writeResponse(out, new Date().toString());
			    		break;
			    	case LOCK:
			    		replicatingLock(cmd.lockId);
			    		writeResponse(out, "OK");
			    		break;
			    	case UNLOCK:
			    		if (replicatingUnlock(cmd.lockId)) {
			    			writeResponse(out, "OK");
			    		}
			    		else {
			    			writeResponse(out, "NOOP");
			    		}
			    		break;
			    	case CHECK:
			    		if (isLocked(cmd.lockId)) {
				    		writeResponse(out, "T");
				    	}
				    	else {
				    		writeResponse(out, "F");
				    	}
				    	break;
			    			
			    	case RLOCK:
			    		tryLock(cmd.lockId);
			    		writeResponse(out, "OK");
			    		break;
			    	case RUNLOCK:
			    		unlock(cmd.lockId);
			    		writeResponse(out, "OK");
			    		break;
			    	case COUNT:
			    		writeResponse(out, NumberFormat.getIntegerInstance().format(getLockCount()));
			    		break;
			    	case PEER:
			    		if (peerRegistrationEnabled) {
			    			registerPeer(cmd.lockId);
			    			writeResponse(out, "OK");
			    		}
			    		else {
			    			writeResponse(out, "Peer registration disabled.");
			    		}
			    		break;
			    	case NOPEER:
			    		if (peerRegistrationEnabled) {
			    			deregisterPeer(cmd.lockId);
			    			writeResponse(out, "OK");
			    		}
			    		else {
			    			writeResponse(out, "Peer registration disabled.");
			    		}
			    		break;
			    	case PEERS:
			    		writeResponse(out, "localhost:" + portNumber);
			    		if (peers != null) {
			    			for (String id : peers.keySet()) {
			    				writeResponse(out, id);
			    			}
			    		}
			    		
			    		break;
			    		
			    
			    }
			    
			    out.write("lockd> ");
			    out.flush();
			}
			
		}
		catch (Exception e) {
			log.error("Exception processing remote lockd connection", e);
		}
		
	}
	
	protected void registerPeer(String peerId) {
		
		
	}
	
	protected void deregisterPeer(String peerId) {
		
		
	}
	
	protected void replicatingLock(String lockId) {
		
		lock(lockId);
		if (atomicReplication) {
			
			
		}
		else {
			
		}
		
	}
	
	protected boolean replicatingUnlock(String lockId) {
		
		boolean result = unlock(lockId);
		if (atomicReplication) {
			
		}
		else {
			
		}
		return result;
		
	}
	
	protected void writeResponse(Writer writer, String response) throws Exception {
		
		writer.write(response);
		writer.write("\n");
		writer.flush();
		
	}

	@Override
	public void shutdown() {
		
		if (socket != null) {
			try {
				socket.close();
			}
			catch (Exception e) {}
		}
		
	}
	
	
	public String[] getPeers() {
		return rawPeers;
	}

	public void setPeers(String[] peers) {
		this.rawPeers = peers;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public boolean isAtomicReplication() {
		return atomicReplication;
	}

	public void setAtomicReplication(boolean atomicReplication) {
		this.atomicReplication = atomicReplication;
	}

	public boolean isNetworkIfNoPeers() {
		return networkIfNoPeers;
	}

	public void setNetworkIfNoPeers(boolean networkIfNoPeers) {
		this.networkIfNoPeers = networkIfNoPeers;
	}

	public PeerDiscoveryService getPeerDiscoverService() {
		return peerDiscoverService;
	}

	public void setPeerDiscoverService(PeerDiscoveryService peerDiscoverService) {
		this.peerDiscoverService = peerDiscoverService;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public boolean isPeerRegistrationEnabled() {
		return peerRegistrationEnabled;
	}

	public void setPeerRegistrationEnabled(boolean peerRegistrationEnabled) {
		this.peerRegistrationEnabled = peerRegistrationEnabled;
	}


	private static class LockdCommand {
		
		private Command command = null; 
		private String lockId;
		
		public static LockdCommand fromInput(String line) {
			
			String[] tokens = StringUtils.split(line, " ");
			
			if (tokens.length == 0) {
				return null;
			}
			if (tokens.length > 2) {
				return null;
			}
			Command cmd = Command.valueOf(tokens[0].toUpperCase());
			if (cmd == null) {
				return null;
			}
			LockdCommand result = new LockdCommand();
			result.command = cmd;
			if (tokens.length == 2) {
				result.lockId = tokens[1];
			}
			
			if (  ( (cmd == Command.TIME) || (cmd == Command.QUIT) ) && (result.lockId != null) ) {
				return null;
			}
			
			return result;
			
		}

		public Command getCommand() {
			return command;
		}

		public String getLockId() {
			return lockId;
		}
		
		
		
	}
	
	private static class Peer {
		
		private String host;
		private int portNumber;
		
		public Peer(String host, int portNumber) {
			this.host = host;
			this.portNumber = portNumber;
		}

		public String getHost() {
			return host;
		}

		public int getPortNumber() {
			return portNumber;
		}
		
		public String getPeerIdentity() {
			return host + ":" + portNumber;
		}
		
	}
	
	private enum Command {
		
		TIME,
		RLOCK,
		RUNLOCK,
		LOCK,
		UNLOCK,
		CHECK,
		QUIT,
		COUNT,
		PEERS,
		PEER,
		NOPEER
		
	}
	
}
