package com.frs.alto.security.spring;

import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.context.SecurityContextRepository;

import com.frs.alto.web.ClientType;

public interface ClusterSecurityContextRepository extends SecurityContextRepository {

	public void expire(HttpServletRequest request, String clusterSessionId);
	public void expire(HttpServletRequest request, String clusterSessionId, Collection<ClientType> clientTypes);
	public ClusterSecurityContext loadById(String clusterSessionId);
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal);
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal, ClientType clientType);
	
}
