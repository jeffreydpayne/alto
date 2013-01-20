package com.frs.alto.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.web.ClientType;
import com.frs.alto.web.ClientTypeResolver;
import com.frs.alto.web.NullClientTypeResolver;

public class CacheBasedSecurityContextRepository implements ClusterSecurityContextRepository, InitializingBean {
		
	public final static String DEFAULT_USER_SESSION_KEY_PREFIX = "user-sessions";
	public final static String DEFAULT_HTTP_SESSION_CACHE_KEY = "cluster-session";
	
	private static Log logger = LogFactory.getLog(CacheBasedSecurityContextRepository.class); 
	
	private AltoCache altoCache = null;
	private String region = CacheBasedSecurityContextRepository.class.getName();
	private String userPrefix = DEFAULT_USER_SESSION_KEY_PREFIX; 
	private String httpSessionCacheKey = DEFAULT_HTTP_SESSION_CACHE_KEY;
	private boolean touchless = true;
	
	private ClusterSessionIdResolver clusterSessionIdResolver = new CookieBasedClusterSessionIdResolver(); 
	private ClusterSessionIdGenerator idGenerator = new DefaultClusterSessionIdGenerator();
	private ClientTypeResolver clientTypeResolver = new NullClientTypeResolver();
	
	private boolean httpSessionCacheEnabled = false;
	private int clusterSessionTimeout = 20; //in minutes
	private int sessionCacheRefreshInterval = 0; //in minutes
	private boolean fuzzyTimeouts = true;
	
	/*
	 * Non Fuzzy Timeout Support
	 * 
	 */
	
	private Map<String, ScheduledFuture<Long>> timestampUpdaters = null;
	private Executor updateQueuer = null;
	
	
	public CacheBasedSecurityContextRepository() {
		super();
	}
	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		if (!fuzzyTimeouts) {
			timestampUpdaters = new HashMap<String, ScheduledFuture<Long>>();
			updateQueuer = Executors.newSingleThreadExecutor();
			
		}
		
	}



	@Override
	public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
		

		String clusterSessionId = clusterSessionIdResolver.resolve(requestResponseHolder.getRequest());
						
			
		ClusterSecurityContext context = null;
		
		if (httpSessionCacheEnabled) {
			context = loadFromSessionCache(requestResponseHolder.getRequest().getSession(true), clusterSessionId);
		}
				
		if (clusterSessionId != null) {
			context = loadById(clusterSessionId, !touchless);			
		}
		
		if (isTimedOut(context)) {
			expire(requestResponseHolder.getRequest(), clusterSessionId);
			context = null;
		}
		
		
		if (context == null) {
			context = new ClusterSecurityContext(idGenerator.generate(requestResponseHolder.getRequest()));
		}
		
		context.setLastUpdateTimestamp(System.currentTimeMillis());
		
		return context;
	}
	
	protected boolean isTimedOut(ClusterSecurityContext context) {
		
		if (context == null) {
			return false;
		}
		
		long msTimeout = clusterSessionTimeout * 60 * 1000;
		
		if (context.getLastUpdateTimestamp() < (System.currentTimeMillis() - msTimeout) ) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	protected ClusterSecurityContext loadFromSessionCache(HttpSession session, String clusterSessionId) {
		
		return (ClusterSecurityContext)session.getAttribute(httpSessionCacheKey);
		
	}
	
	protected void saveToSessionCache(HttpSession session, ClusterSecurityContext context) {
		session.setAttribute(httpSessionCacheKey, context);
	}
	
	
	protected long doCachePersist(final Collection<ClientType> clientTypes, final ClusterSecurityContext context) {
		
		context.setLastUpdateTimestamp(System.currentTimeMillis());
		context.setPersistent(true);
		altoCache.put(region, context.getClusterSessionId(), context);

		if (context.getAuthentication() != null) {
						
			Collection<String> sessionIds = new ArrayList<String>(getClusterSessionIdsForPrinciple(context.getAuthentication()));
			String key = userPrefix + "#" + context.getAuthentication().getName();
			sessionIds.add(context.getClusterSessionId());
			altoCache.put(region, key, StringUtils.join(sessionIds, ','));
			
			if (clientTypes != null) {
				for (ClientType type : clientTypes) {
					sessionIds = new ArrayList<String>(getClusterSessionIdsForPrinciple(context.getAuthentication(), type));
					key = userPrefix = "#" + type.name() + "#" + context.getAuthentication().getName();
					sessionIds.add(context.getClusterSessionId());
					altoCache.put(region, key, StringUtils.join(sessionIds, ','));
				}
			}
			
		}
		
		if (!fuzzyTimeouts) {
		
			updateQueuer.execute(new Runnable() {
				
				@Override
				public void run() {
					ScheduledFuture<Long> future = timestampUpdaters.get(context.getClusterSessionId());
					if (future != null) {
						future.cancel(false);
						timestampUpdaters.remove(context.getClusterSessionId());		
					}
				}
				
			});
			
		}
		
		return context.getLastUpdateTimestamp();
		
	}
	
	@Override
	public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
				
		
		if (!(context instanceof ClusterSecurityContext)) {
			return;
		}
		
		ClusterSecurityContext ctx = (ClusterSecurityContext)context;
		ctx.setLastUpdateTimestamp(System.currentTimeMillis());
						
		if (httpSessionCacheEnabled) {
			saveToSessionCache(request.getSession(), ctx);
		}		
		
		if (!ctx.isPersistent() || isRefreshEligible(ctx)) {
			doCachePersist(clientTypeResolver.resolveClientType(request), ctx);
		}
		else if (!fuzzyTimeouts){
			updateQueuer.execute(new Runnable() {
				
				@Override
				public void run() {
					
					
					
				}
			});
		}
		
		
	}
	@Override
	public boolean containsContext(HttpServletRequest request) {
		
		String clusterSessionId = clusterSessionIdResolver.resolve(request);
		
		return altoCache.isCached(region, clusterSessionId);
		
	}
	
	protected boolean isRefreshEligible(ClusterSecurityContext context) {
		
		if (sessionCacheRefreshInterval < 1) {
			return true;
		}
		
		long msTimeout = sessionCacheRefreshInterval * 60 * 1000;
		
		if (context.getLastUpdateTimestamp() < (System.currentTimeMillis() - msTimeout) ) {
			return true;
		}
		else {
			return false;
		}
		
		
	}
	

	@Override
	public ClusterSecurityContext loadById(String clusterSessionId,	boolean touch) {
		ClusterSecurityContext context = (ClusterSecurityContext)altoCache.get(region, clusterSessionId);
		if ( (context != null) && touch && isRefreshEligible(context)) {
			context.setLastUpdateTimestamp(System.currentTimeMillis());
			altoCache.put(region, clusterSessionId, context);
		}
		
		return context;
	}
	@Override
	public void expire(HttpServletRequest request, String clusterSessionId) {
		
		//clear session cache
		request.getSession().removeAttribute(httpSessionCacheKey);
		
		ClusterSecurityContext context = loadById(clusterSessionId, false);
		if ( (context != null) && (context.getAuthentication() != null) ) {
			Collection<String> sessionIds = getClusterSessionIdsForPrinciple(context.getAuthentication());
			Collection<String> newIds = new ArrayList<String>();
			for (String id : sessionIds) {
				if (!id.equals(clusterSessionId)) {
					newIds.add(id);
				}
			}
			String key = userPrefix + "#" + context.getAuthentication().getName();
			altoCache.put(region, key, StringUtils.join(newIds, ','));
			
		}
		altoCache.remove(region, clusterSessionId);
	}
	
	
	
	
		
	@Override
	public void expire(HttpServletRequest request, String clusterSessionId, Collection<ClientType> clientTypes) {
		ClusterSecurityContext context = loadById(clusterSessionId, false);
		if ( (context != null) && (context.getAuthentication() != null) ) {
			for (ClientType clientType : clientTypes) {
				Collection<String> sessionIds = getClusterSessionIdsForPrinciple(context.getAuthentication(), clientType);
				Collection<String> newIds = new ArrayList<String>();
				for (String id : sessionIds) {
					if (!id.equals(clusterSessionId)) {
						newIds.add(id);
					}
				}
				String key = userPrefix + "#" + clientType.name() + "#" + context.getAuthentication().getName();
				altoCache.put(region, key, StringUtils.join(newIds, ','));
			}
			
		}
		expire(request, clusterSessionId);
		
	}
	@Override
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal) {
		
		if (principal != null) {
			String key = userPrefix + "#" + principal.getName();
			
			String rawValue = (String)altoCache.get(region, key);
			
			if (rawValue != null) {
				String[] tokens = StringUtils.split(rawValue, ',');
				return Arrays.asList(tokens);
			}
		}
		return new ArrayList<String>();
	}
	@Override
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal, ClientType clientType) {
		
		if (principal != null) {
		
			String key = userPrefix + "#" + clientType.name() + "#" + principal.getName();
			
			String rawValue = (String)altoCache.get(region, key);
			
			if (rawValue != null) {
				String[] tokens = StringUtils.split(rawValue, ',');
				return Arrays.asList(tokens);
			}
		}
		return new ArrayList<String>();
	}
	public AltoCache getAltoCache() {
		return altoCache;
	}
	public void setAltoCache(AltoCache altoCache) {
		this.altoCache = altoCache;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getUserPrefix() {
		return userPrefix;
	}
	public void setUserPrefix(String userPrefix) {
		this.userPrefix = userPrefix;
	}
	public ClusterSessionIdGenerator getIdGenerator() {
		return idGenerator;
	}
	public void setIdGenerator(ClusterSessionIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
	public boolean isTouchless() {
		return touchless;
	}
	public void setTouchless(boolean touchless) {
		this.touchless = touchless;
	}
	public ClusterSessionIdResolver getClusterSessionIdResolver() {
		return clusterSessionIdResolver;
	}
	public void setClusterSessionIdResolver(
			ClusterSessionIdResolver clusterSessionIdResolver) {
		this.clusterSessionIdResolver = clusterSessionIdResolver;
	}
	public ClientTypeResolver getClientTypeResolver() {
		return clientTypeResolver;
	}
	public void setClientTypeResolver(ClientTypeResolver clientTypeResolver) {
		this.clientTypeResolver = clientTypeResolver;
	}
	public boolean isHttpSessionCacheEnabled() {
		return httpSessionCacheEnabled;
	}
	public void setHttpSessionCacheEnabled(boolean httpSessionCacheEnabled) {
		this.httpSessionCacheEnabled = httpSessionCacheEnabled;
	}
	public int getClusterSessionTimeout() {
		return clusterSessionTimeout;
	}
	public void setClusterSessionTimeout(int clusterSessionTimeout) {
		this.clusterSessionTimeout = clusterSessionTimeout;
	}
	public String getHttpSessionCacheKey() {
		return httpSessionCacheKey;
	}
	public void setHttpSessionCacheKey(String httpSessionCacheKey) {
		this.httpSessionCacheKey = httpSessionCacheKey;
	}
	public int getSessionCacheRefreshInterval() {
		return sessionCacheRefreshInterval;
	}
	public void setSessionCacheRefreshInterval(int sessionCacheRefreshInterval) {
		this.sessionCacheRefreshInterval = sessionCacheRefreshInterval;
	}
	public boolean isFuzzyTimeouts() {
		return fuzzyTimeouts;
	}
	public void setFuzzyTimeouts(boolean fuzzyTimeouts) {
		this.fuzzyTimeouts = fuzzyTimeouts;
	}


}
