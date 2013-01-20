package com.frs.alto.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.web.ClientType;
import com.frs.alto.web.ClientTypeResolver;
import com.frs.alto.web.NullClientTypeResolver;

public class CacheBasedSecurityContextRepository implements ClusterSecurityContextRepository {
		
	public final static String DEFAULT_USER_SESSION_KEY_PREFIX = "user-sessions";
	
	private static Log logger = LogFactory.getLog(CacheBasedSecurityContextRepository.class); 
	
	private AltoCache altoCache = null;
	private String region = CacheBasedSecurityContextRepository.class.getName();
	private String userPrefix = DEFAULT_USER_SESSION_KEY_PREFIX; 
	private boolean touchless = true;
	
	private ClusterSessionIdResolver clusterSessionIdResolver = new CookieBasedClusterSessionIdResolver(); 
	private ClusterSessionIdGenerator idGenerator = new DefaultClusterSessionIdGenerator();
	private ClientTypeResolver clientTypeResolver = new NullClientTypeResolver();
	
	
	
	public CacheBasedSecurityContextRepository() {
		super();
	}
	@Override
	public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
		
		
		String clusterSessionId = clusterSessionIdResolver.resolve(requestResponseHolder.getRequest());
				
			
		ClusterSecurityContext context = null;
		
		if (clusterSessionId != null) {
			context = loadById(clusterSessionId, !touchless);			
		}
		
		if (context == null) {
			return new ClusterSecurityContext(idGenerator.generate(requestResponseHolder.getRequest()));
		}
		
		return context;
	}
	@Override
	public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
				
		
		if (!(context instanceof ClusterSecurityContext)) {
			return;
		}
		
		ClusterSecurityContext ctx = (ClusterSecurityContext)context;
		ctx.setLastUpdateTimestamp(System.currentTimeMillis());
		
		String clusterSessionId = ctx.getClusterSessionId();
		
		if (!altoCache.isCached(region, clusterSessionId)) {
			altoCache.put(region, clusterSessionId, context);

			if (context.getAuthentication() != null) {
				
				Collection<ClientType> clientTypes = clientTypeResolver.resolveClientType(request);
				
				Collection<String> sessionIds = new ArrayList<String>(getClusterSessionIdsForPrinciple(context.getAuthentication()));
				String key = userPrefix + "#" + context.getAuthentication().getName();
				sessionIds.add(ctx.getClusterSessionId());
				altoCache.put(region, key, StringUtils.join(sessionIds, ','));
				
				if (clientTypes != null) {
					for (ClientType type : clientTypes) {
						sessionIds = new ArrayList<String>(getClusterSessionIdsForPrinciple(context.getAuthentication(), type));
						key = userPrefix = "#" + type.name() + "#" + context.getAuthentication().getName();
						sessionIds.add(ctx.getClusterSessionId());
						altoCache.put(region, key, StringUtils.join(sessionIds, ','));
					}
				}
				
			}
			
		}
		
		
	}
	@Override
	public boolean containsContext(HttpServletRequest request) {
		
		String clusterSessionId = clusterSessionIdResolver.resolve(request);
		
		return altoCache.isCached(region, clusterSessionId);
		
	}
	
	
	

	@Override
	public ClusterSecurityContext loadById(String clusterSessionId,	boolean touch) {
		ClusterSecurityContext context = (ClusterSecurityContext)altoCache.get(region, clusterSessionId);
		if ( (context != null) && touch) {
			context.setLastUpdateTimestamp(System.currentTimeMillis());
			altoCache.put(region, clusterSessionId, context);
		}
		
		return context;
	}
	@Override
	public void expire(String clusterSessionId) {
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
	public void expire(String clusterSessionId,	Collection<ClientType> clientTypes) {
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
		expire(clusterSessionId);
		
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
	

}
