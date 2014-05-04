package com.frs.alto.security.cluster;

public interface Authenticator {
	
	public ClusterPrincipal authenticate(String userName, String password);

}
