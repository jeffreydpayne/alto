package com.frs.alto.core;

import javax.servlet.ServletRequest;


public interface TenantResolver {
	
	public TenantMetaData resolve(ServletRequest request);

}
