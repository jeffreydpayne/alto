package com.frs.alto.security;

import java.security.Principal;
import java.util.Collection;

import org.springframework.security.web.context.SecurityContextRepository;

import com.frs.alto.web.ClientType;

public interface ClusterSecurityContextRepository extends SecurityContextRepository {

	public void expire(String clusterSessionId);
	public void expire(String clusterSessionId, Collection<ClientType> clientTypes);
	public ClusterSecurityContext loadById(String clusterSessionId, boolean touch);
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal);
	public Collection<String> getClusterSessionIdsForPrinciple(Principal principal, ClientType clientType);
	
}
