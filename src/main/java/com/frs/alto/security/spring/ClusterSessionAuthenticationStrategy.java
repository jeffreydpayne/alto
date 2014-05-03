package com.frs.alto.security.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;

import com.frs.alto.web.ClientType;
import com.frs.alto.web.ClientTypeResolver;
import com.frs.alto.web.NullClientTypeResolver;

public class ClusterSessionAuthenticationStrategy implements SessionAuthenticationStrategy {
	
	protected final Log logger = LogFactory.getLog(this.getClass());

	private ClusterSecurityContextRepository securityContextRepository = null;
	private boolean sessionFixation = true;
	private int maxSessions = 1;
	private Map<ClientType, Integer> maxSessionsByClientType = null;
	private ClientTypeResolver clientTypeResolver = new NullClientTypeResolver();
	private ClusterSessionIdGenerator clusterSessionIdGenerator = new DefaultClusterSessionIdGenerator();
	private ClusterSessionIdResolver clusterSessionIdResolver = new CookieBasedClusterSessionIdResolver();
	
	@Override
	public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
		
		ClusterSecurityContext context = (ClusterSecurityContext)securityContextRepository.loadContext(new HttpRequestResponseHolder(request, response));
		context.setAuthentication(authentication);
		
		
		//filter total max sessions first
		if (maxSessions > 0) {
			int postAuthSessionCount = 1;  //the login 
			Collection<String> sessionIds = securityContextRepository.getClusterSessionIdsForPrinciple(authentication);
			postAuthSessionCount += sessionIds.size();
			
			if (postAuthSessionCount > maxSessions) {
				purgeSessions(request, sessionIds, postAuthSessionCount - maxSessions);
			}
		}
			
		//filter client type sessions
		if (maxSessionsByClientType != null) {
			Collection<ClientType> clientTypes = clientTypeResolver.resolveClientType(request);
			if (clientTypes != null) {
				int maxClientTypeSessions = 0;
				int postAuthSessionCount = 0;
				Collection<String> sessionIds = null;
				for (ClientType type : clientTypes) {
					if (maxSessionsByClientType.containsKey(type)) {
						postAuthSessionCount = 1;  //the login 
						maxClientTypeSessions = maxSessionsByClientType.get(type);
						sessionIds = securityContextRepository.getClusterSessionIdsForPrinciple(authentication, type);
						postAuthSessionCount += sessionIds.size();
						
						if (postAuthSessionCount > maxClientTypeSessions) {
							purgeSessions(request, sessionIds, postAuthSessionCount - maxClientTypeSessions);
						}
					}
				}
			}
			
		}
			
			
		
		
		//session fixation
		if ( (context.getClusterSessionId() == null) || sessionFixation) {
			if (context.getClusterSessionId() != null) {
				securityContextRepository.expire(request, context.getClusterSessionId());
			}
			context.setClusterSessionId(clusterSessionIdGenerator.generate(request));
			clusterSessionIdResolver.sendClusterSessionId(request, response, context.getClusterSessionId());
			
		}
		context.setAuthTimestamp(System.currentTimeMillis());
		context.setLastUpdateTimestamp(System.currentTimeMillis());
		securityContextRepository.saveContext(context, request, response);
		
	}
	
	private void purgeSessions(HttpServletRequest request, Collection<String> sessionIds, int sessionCount) {
		
		Collection<ClusterSecurityContext> contexts = new ArrayList<ClusterSecurityContext>();
		boolean lastUpdatesComplete = true;
		
		ClusterSecurityContext context = null;
		for (String id : sessionIds) {
			context = securityContextRepository.loadById(id);
			if (context != null) {
				if (context.getLastUpdateTimestamp() == 0) {
					lastUpdatesComplete = false;
				}
				contexts.add(context);
			}
		}
		
		Map<Long, String> sortedSessions = new TreeMap<Long, String>();
		
		for (ClusterSecurityContext ctx : contexts) {
			if (lastUpdatesComplete) {
				sortedSessions.put(ctx.getLastUpdateTimestamp(), ctx.getClusterSessionId());
			}
			else {
				sortedSessions.put(ctx.getAuthTimestamp(), ctx.getClusterSessionId());
			}
		}
		
		Iterator<String> itr = sortedSessions.values().iterator();
		
		int purgedCount = 0;
		while (itr.hasNext() && (purgedCount < sessionCount) ) {
			if (maxSessionsByClientType != null) {
				securityContextRepository.expire(request, itr.next(), maxSessionsByClientType.keySet());
			}
			else {
				securityContextRepository.expire(request, itr.next());
			}
			purgedCount++;
		}
		
		
		
	}
	
	public void setMaxSessions(ClientType type, int max) {
		if (maxSessionsByClientType == null) {
			maxSessionsByClientType = new HashMap<ClientType, Integer>();
		}
		maxSessionsByClientType.put(type, max);
	}
	
	public void setMaxWebSessions(int max) {
		setMaxSessions(ClientType.WEB, max);
	}
	public void setMaxMobileSessions(int max) {
		setMaxSessions(ClientType.MOBILE, max);
	}




	public ClusterSecurityContextRepository getSecurityContextRepository() {
		return securityContextRepository;
	}

	public void setSecurityContextRepository(
			ClusterSecurityContextRepository securityContextRepository) {
		this.securityContextRepository = securityContextRepository;
	}

	public ClusterSessionIdGenerator getClusterSessionIdGenerator() {
		return clusterSessionIdGenerator;
	}

	public void setClusterSessionIdGenerator(
			ClusterSessionIdGenerator clusterSessionIdGenerator) {
		this.clusterSessionIdGenerator = clusterSessionIdGenerator;
	}

	public ClusterSessionIdResolver getClusterSessionIdResolver() {
		return clusterSessionIdResolver;
	}

	public void setClusterSessionIdResolver(
			ClusterSessionIdResolver clusterSessionIdResolver) {
		this.clusterSessionIdResolver = clusterSessionIdResolver;
	}

	public boolean isSessionFixation() {
		return sessionFixation;
	}


	public void setSessionFixation(boolean sessionFixation) {
		this.sessionFixation = sessionFixation;
	}


	public int getMaxSessions() {
		return maxSessions;
	}


	public void setMaxSessions(int maxSessions) {
		this.maxSessions = maxSessions;
	}


	public Map<ClientType, Integer> getMaxSessionsByClientType() {
		return maxSessionsByClientType;
	}


	public void setMaxSessionsByClientType(
			Map<ClientType, Integer> maxSessionsByClientType) {
		this.maxSessionsByClientType = maxSessionsByClientType;
	}


	public ClientTypeResolver getClientTypeResolver() {
		return clientTypeResolver;
	}


	public void setClientTypeResolver(ClientTypeResolver clientTypeResolver) {
		this.clientTypeResolver = clientTypeResolver;
	}
	
	
	
	

}
