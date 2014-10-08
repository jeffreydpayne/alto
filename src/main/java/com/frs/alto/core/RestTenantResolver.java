package com.frs.alto.core;

import javax.servlet.ServletRequest;


public class RestTenantResolver implements TenantResolver {

	private String apiUrl = null;
	private String apiId = null;
	private String secretKey = null;
	
	
	@Override
	public TenantMetaData resolve(ServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getApiUrl() {
		return apiUrl;
	}


	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}


	public String getApiId() {
		return apiId;
	}


	public void setApiId(String apiId) {
		this.apiId = apiId;
	}


	public String getSecretKey() {
		return secretKey;
	}


	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	
	
	
}
