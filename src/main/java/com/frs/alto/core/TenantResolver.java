package com.frs.alto.core;

import javax.servlet.ServletRequest;


public interface TenantResolver {
	
	public TenantMetaData resolve(ServletRequest request);
	public TenantMetaData byId(String tenantId);

}
