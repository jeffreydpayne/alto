package com.frs.alto.security.cluster;

import java.util.Collection;

public interface ClusterPrincipal {
	
	public Collection<String> getGrantedPermissionCodes();
	public Collection<String> getRememberablePermissionCodes();

}
