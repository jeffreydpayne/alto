package com.frs.alto.security.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpRequestResponseHolder;

public class ClusterSecurityContextLogoutHandler implements LogoutHandler {
	
	private ClusterSecurityContextRepository contextRepository = null;
	private ClusterSessionIdResolver idResolver = new CookieBasedClusterSessionIdResolver();
	private boolean clearClusterSessionId = true;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		
		ClusterSecurityContext context = (ClusterSecurityContext)contextRepository.loadContext(new HttpRequestResponseHolder(request, response));
		if (context != null) {
			contextRepository.expire(request, context.getClusterSessionId());
			if (clearClusterSessionId) {
				idResolver.sendClusterSessionId(request, response, null);
			}
		}
				
	}

	public ClusterSecurityContextRepository getContextRepository() {
		return contextRepository;
	}

	public void setContextRepository(
			ClusterSecurityContextRepository contextRepository) {
		this.contextRepository = contextRepository;
	}

	public ClusterSessionIdResolver getIdResolver() {
		return idResolver;
	}

	public void setIdResolver(ClusterSessionIdResolver idResolver) {
		this.idResolver = idResolver;
	}

	public boolean isClearClusterSessionId() {
		return clearClusterSessionId;
	}

	public void setClearClusterSessionId(boolean clearClusterSessionId) {
		this.clearClusterSessionId = clearClusterSessionId;
	}
	
	
	

}
