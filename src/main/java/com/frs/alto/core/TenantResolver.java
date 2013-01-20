package com.frs.alto.core;


public interface TenantResolver {
	
	public TenantMetaData resolve(String hostName);

}
