package com.frs.alto.security.cluster;

public interface PrincipalRepository {
	
	public ClusterPrincipal lookupPrincipal(String userName);

}
